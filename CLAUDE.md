# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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

Run a single unit test class:
```bash
./gradlew test --tests "io.celox.xclip.ExampleUnitTest"
```

APK output: `app/build/outputs/apk/{variant}/XClip-v{versionName}-{variant}.apk`

## Releasing

Push a version tag to trigger the GitHub Actions release workflow (`.github/workflows/release.yml`):
```bash
git tag v1.x.x
git push origin v1.x.x
```
Secrets (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) are configured on the GitHub repo. Local signing uses `keystore/xclip-release.jks` (gitignored).

## Architecture

**MVVM + Repository**, single `app` module, Kotlin 2.1.10, Jetpack Compose, no dependency injection.

### Data Flow

```
Clipboard change -> ClipboardService.insertSync() -> Room DB
Room DB -> Flow -> ClipboardViewModel.flatMapLatest(searchQuery) -> StateFlow
StateFlow -> Compose UI (ClipboardScreen / DetailScreen)
```

### Layers

- **`data/`** -- Room database layer. `ClipboardDao` returns `Flow<List>` for the UI and has synchronous `insertSync()`/`getCountInternal()` for the service. `ClipboardDatabase` is a singleton at version 2 with an explicit `Migration(1, 2)` that adds the `is_pinned` column. `ClipboardRepository` wraps both async (suspend/Flow) and sync (executor) access patterns.

- **`ui/`** -- Compose UI. `ClipboardViewModel` uses `flatMapLatest` on a `MutableStateFlow<String>` search query to switch between `getAllClipboards()` and `searchClipboards()`. Navigation between `ClipboardScreen` and `DetailScreen` is state-driven via `AnimatedContent` in `MainActivity` (no Navigation library).

- **`service/ClipboardService`** -- Foreground service (`START_STICKY`) with a `ClipboardManager` listener. Runs on the main thread but uses `Repository.insertSync()` which dispatches to a single-thread executor. Notification strings come from `R.string.*` and `R.plurals.*`.

- **`receiver/BootReceiver`** -- Restarts `ClipboardService` on `ACTION_BOOT_COMPLETED`.

## Key Technical Details

- **Kotlin 2.1.10**, **Compose BOM 2025.04.01**, **Room 2.6.1** with **KSP** (not kapt)
- **Min SDK 24 / Target+Compile SDK 36**, Java 11 bytecode target
- **Gradle 8.13** with Kotlin DSL and version catalog (`gradle/libs.versions.toml`)
- **R8 + resource shrinking** enabled for release; ProGuard keeps Room entities and coroutines
- UI strings are **German** (`values/strings.xml`) with `<plurals>` for notification counts
- `themes.xml` is minimal (launch theme only) -- Compose `XClipTheme` handles everything
- `ClipboardEntity` is a `data class` with `id: Long = 0` + `@PrimaryKey(autoGenerate = true)`

## Gotchas

- `ClipboardService` must use `insertSync()`/`getCount()` (not suspend) because it's called from the clipboard listener callback, not a coroutine scope
- Room migration must be kept in sync with entity changes -- never use `fallbackToDestructiveMigration()` as users have existing data from v0.0.1
- The `signingConfig` block in `app/build.gradle.kts` falls back to local keystore paths if env vars aren't set, so local release builds work without CI
- When adding new Room fields, remember to: (1) add to entity, (2) add migration in `ClipboardDatabase`, (3) bump DB version, (4) update ProGuard if needed
