# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

XClip is an Android clipboard history manager (package: `io.celox.xclip`). It runs a foreground service to monitor clipboard changes, stores entries in a Room database, and presents history via a bottom-sheet dialog accessible from the notification. Written in Java with XML layouts (no Compose).

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew installDebug           # Build and install on connected device
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumentation tests on device
./gradlew lint                   # Run Android lint
```

APK output uses the naming pattern `XClip-v{versionName}-{variant}.apk`.

## Architecture

**MVVM + Repository pattern**, single `app` module, Java 11, no dependency injection.

### Layers

- **`data/`** — Room database layer: `ClipboardEntity` (table: `clipboard_history`), `ClipboardDao` (returns LiveData), `ClipboardDatabase` (singleton), `ClipboardRepository` (uses ExecutorService for background ops)
- **`ui/`** — `ClipboardViewModel` (AndroidViewModel exposing LiveData), `ClipboardAdapter` (RecyclerView), `ClipboardDialog` (DialogFragment, bottom-sheet style with swipe-to-delete)
- **`service/`** — `ClipboardService` (foreground service, `START_STICKY`, monitors clipboard, deduplicates entries)
- **`receiver/`** — `BootReceiver` (auto-starts service on boot)
- **`MainActivity`** — Entry point; handles notification permission (Android 13+), starts service, opens dialog via notification intent

### Data Flow

Clipboard change → `ClipboardService` → `ClipboardRepository.insert()` → Room DB → LiveData → `ClipboardViewModel` → `ClipboardDialog`/`ClipboardAdapter`

## Key Technical Details

- **Min SDK 24 / Target+Compile SDK 36**
- **Gradle 8.13** with Kotlin DSL (`.kts`) and version catalog (`gradle/libs.versions.toml`)
- Room annotation processor configured via `annotationProcessor` (not kapt/ksp)
- ProGuard/R8 minification is **disabled** for all build types
- UI strings and code comments are in **German**
- Material 3 theming (`Theme.MaterialComponents.DayNight.NoActionBar`)
