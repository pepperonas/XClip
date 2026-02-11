# CLAUDE.md

This file provides guidance to Claude Code when working with this repository.

## Project Overview

XClip is an Android clipboard history manager (package: `io.celox.xclip`). It runs a foreground service to monitor clipboard changes, stores entries in a Room database, and presents a full Compose UI with search, pin, undo-delete, and a detail view. Written in 100% Kotlin with Jetpack Compose (no XML layouts).

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build signed release APK (R8 enabled)
./gradlew installDebug           # Build and install on connected device
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumentation tests on device
./gradlew lint                   # Run Android lint
```

APK output uses the naming pattern `XClip-v{versionName}-{variant}.apk`.

## Releasing

Push a version tag to trigger the GitHub Actions release workflow:
```bash
git tag v1.x.x
git push origin v1.x.x
```
Secrets (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) are configured on the GitHub repo.

## Architecture

**MVVM + Repository**, single `app` module, Kotlin 2.1.10, Jetpack Compose, no dependency injection.

### Layers

- **`data/`** -- Room database layer:
  - `ClipboardEntity` (table: `clipboard_history`, fields: id, text, timestamp, is_pinned)
  - `ClipboardDao` (returns `Flow<List>`, supports search with LIKE, setPinned, sync methods for service)
  - `ClipboardDatabase` (singleton, version 2, explicit Migration v1->v2 for is_pinned column)
  - `ClipboardRepository` (exposes Flow, sync wrappers for ClipboardService)

- **`ui/`** -- Compose UI:
  - `ClipboardViewModel` (StateFlow with `flatMapLatest` search, undo-delete, pin toggle)
  - `theme/` -- Color.kt, Type.kt, Theme.kt (Material You + dynamic colors), Animation.kt
  - `components/` -- SearchBar (animated), ClipboardItem (swipe-delete, glassmorphism, pin), EmptyState (pulsing)
  - `screen/` -- ClipboardScreen (main list with pinned sections, snackbar undo), DetailScreen (fulltext + copy FAB)

- **`service/`** -- `ClipboardService` (foreground, `START_STICKY`, clipboard listener, uses `insertSync()`/`getCount()`)

- **`receiver/`** -- `BootReceiver` (auto-starts service on boot)

- **`MainActivity`** -- `ComponentActivity`, `enableEdgeToEdge()`, `setContent { XClipTheme }`, `AnimatedContent` navigation, permission launcher

### Data Flow

```
Clipboard change -> ClipboardService.insertSync() -> Room DB
Room DB -> Flow -> ClipboardViewModel.flatMapLatest(searchQuery) -> StateFlow
StateFlow -> Compose UI (ClipboardScreen / DetailScreen)
```

## Key Technical Details

- **Kotlin 2.1.10** with **Compose BOM 2025.04.01**
- **Min SDK 24 / Target+Compile SDK 36**
- **Gradle 8.13** with Kotlin DSL (`.kts`) and version catalog (`gradle/libs.versions.toml`)
- Room uses **KSP** (not kapt or annotationProcessor)
- **R8 minification + resource shrinking** enabled for release builds
- ProGuard rules keep Room entities and Kotlin coroutines
- Signing config reads env vars (`KEYSTORE_FILE`, etc.) with local keystore fallback
- UI strings are in **German** (`values/strings.xml`), with `<plurals>` for notification counts
- Room DB version 2 with explicit `Migration(1, 2)` -- adds `is_pinned` column
- `ClipboardService` uses synchronous `insertSync()`/`getCount()` (not suspend) because it runs on its own executor
- No XML layouts -- everything is Compose
- GitHub Actions workflow (`.github/workflows/release.yml`) auto-builds signed APK on tag push
