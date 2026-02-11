# XClip

A modern Android clipboard history manager built with Kotlin and Jetpack Compose. Monitors your clipboard in the background, stores entries locally, and lets you search, pin, and manage your clipboard history with a polished Material You interface.

[![Release](https://img.shields.io/github/v/release/pepperonas/XClip)](https://github.com/pepperonas/XClip/releases/latest)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://developer.android.com/about/versions/nougat)

## Download

Grab the latest signed APK from [GitHub Releases](https://github.com/pepperonas/XClip/releases/latest) and install it on your device.

## Features

- **Background Clipboard Monitoring** -- Foreground service tracks all clipboard changes and stores them automatically
- **Search** -- Real-time full-text search across all entries
- **Pin** -- Pin important entries so they stay at the top
- **Undo Delete** -- Swipe to delete with snackbar undo action
- **Detail View** -- Full-text view with selectable text for long entries
- **Long-Press to Copy** -- Haptic feedback confirms the action
- **Material You** -- Dynamic colors on Android 12+, dark-first theme on older devices
- **Boot Persistence** -- Service restarts automatically after reboot
- **Room Migration** -- Seamless upgrade from v0.0.1 without data loss

## Screenshots

| Clipboard History | Search | Detail View |
|---|---|---|
| Dark-first Material You list with glassmorphism cards | Animated search bar with real-time filtering | Full-text view with selectable text and copy FAB |

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.1.10 |
| UI | Jetpack Compose (BOM 2025.04.01) + Material 3 |
| Architecture | MVVM + Repository |
| Reactive | StateFlow + Kotlin Flow |
| Database | Room 2.6.1 + KSP |
| Async | Kotlin Coroutines |
| Build | Gradle 8.13, Kotlin DSL, Version Catalog |
| CI/CD | GitHub Actions (auto-release on tag) |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 |

## Architecture

```
Clipboard Change
      |
ClipboardService (foreground, START_STICKY)
      |
ClipboardRepository.insertSync()
      |
Room Database (v2, is_pinned migration)
      |
Flow<List<ClipboardEntity>>
      |
ClipboardViewModel (StateFlow + flatMapLatest search)
      |
Compose UI (ClipboardScreen / DetailScreen)
```

### Project Structure

```
app/src/main/java/io/celox/xclip/
├── MainActivity.kt                     # ComponentActivity + Compose entry point
├── data/
│   ├── ClipboardEntity.kt             # Room entity (id, text, timestamp, isPinned)
│   ├── ClipboardDao.kt                # DAO with Flow, search, pin queries
│   ├── ClipboardDatabase.kt           # Singleton + Migration v1→v2
│   └── ClipboardRepository.kt         # Flow-based + sync methods for service
├── service/
│   └── ClipboardService.kt            # Foreground service, clipboard listener
├── receiver/
│   └── BootReceiver.kt                # Auto-start on boot
└── ui/
    ├── ClipboardViewModel.kt          # StateFlow, search, pin, undo-delete
    ├── theme/
    │   ├── Color.kt                   # Dark/Light palettes + glassmorphism tokens
    │   ├── Type.kt                    # Custom typography
    │   ├── Theme.kt                   # Material You with dynamic colors
    │   └── Animation.kt              # Shared animation constants
    ├── components/
    │   ├── SearchBar.kt               # Animated search with auto-focus
    │   ├── ClipboardItem.kt           # Swipe-to-delete, pin, glassmorphism card
    │   └── EmptyState.kt             # Pulsing icon empty state
    └── screen/
        ├── ClipboardScreen.kt         # Main list with pinned section + snackbar
        └── DetailScreen.kt            # Full-text view with copy FAB
```

## Building

### Prerequisites

- JDK 17+
- Android SDK with API 36

### Commands

```bash
./gradlew assembleDebug       # Debug APK
./gradlew assembleRelease     # Signed release APK (requires keystore)
./gradlew installDebug        # Build + install on connected device
./gradlew test                # Unit tests
./gradlew lint                # Android lint
```

### Release Build

The release build uses R8 minification and resource shrinking. Signing is configured via environment variables with local keystore fallback:

| Variable | Description |
|---|---|
| `KEYSTORE_FILE` | Path to .jks keystore |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |

## Releasing

Push a version tag to trigger the GitHub Actions workflow:

```bash
git tag v1.1.0
git push origin v1.1.0
```

This automatically builds a signed APK and publishes it as a GitHub Release.

## Permissions

| Permission | Reason |
|---|---|
| `FOREGROUND_SERVICE` | Background clipboard monitoring |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Clipboard monitoring service type |
| `POST_NOTIFICATIONS` | Persistent notification (Android 13+) |
| `RECEIVE_BOOT_COMPLETED` | Auto-start after reboot |

## Version History

| Version | Changes |
|---|---|
| **1.0.0** | Complete rewrite: Kotlin + Compose, Material You, search, pin, undo, new icon |
| 0.0.1 | Initial release: Java/XML, basic clipboard history |

## License

MIT License -- see [LICENSE](LICENSE) for details.

## Author

[celox.io](https://celox.io) / [@pepperonas](https://github.com/pepperonas)
