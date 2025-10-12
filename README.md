# XClip

XClip is an Android clipboard history manager that runs as a foreground service to monitor and store your clipboard entries.

## Features

- **Background Clipboard Monitoring**: Runs as a foreground service to track all clipboard changes
- **Persistent Storage**: Stores clipboard history using Room database
- **Boot Persistence**: Automatically starts monitoring after device reboot
- **Notification Access**: Quick access to clipboard history through notification action
- **Modern Architecture**: Built with Android Architecture Components (ViewModel, LiveData, Room)

## Requirements

- **Min SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14+ (API 36)
- **Compile SDK**: 36

## Permissions

The app requires the following permissions:

- `FOREGROUND_SERVICE` - To run clipboard monitoring in background
- `POST_NOTIFICATIONS` - To display persistent notification (Android 13+)
- `RECEIVE_BOOT_COMPLETED` - To auto-start service after device reboot
- `FOREGROUND_SERVICE_SPECIAL_USE` - For clipboard monitoring service type

## Architecture

### Components

1. **MainActivity** (`MainActivity.java`)
   - Entry point of the application
   - Handles notification permission requests
   - Starts the clipboard monitoring service
   - Opens clipboard history dialog

2. **ClipboardService** (`service/ClipboardService.java`)
   - Foreground service that monitors clipboard changes
   - Stores clipboard entries to database
   - Displays persistent notification

3. **ClipboardDialog** (`ui/ClipboardDialog.java`)
   - Bottom sheet dialog displaying clipboard history
   - Allows viewing and managing clipboard entries

4. **BootReceiver** (`receiver/BootReceiver.java`)
   - Broadcast receiver that restarts service after device reboot

### Data Layer

- **Room Database** implementation with:
  - `ClipboardEntity` - Data model for clipboard entries
  - `ClipboardDao` - Data access object
  - `ClipboardDatabase` - Database singleton
  - `ClipboardRepository` - Repository pattern implementation

### UI Layer

- **MVVM Architecture** with:
  - `ClipboardViewModel` - ViewModel for managing clipboard data
  - `ClipboardAdapter` - RecyclerView adapter for displaying clipboard items

## Project Structure

```
app/src/main/java/io/celox/xclip/
├── MainActivity.java                  # Main activity
├── data/                              # Data layer
│   ├── ClipboardDao.java             # Room DAO
│   ├── ClipboardDatabase.java        # Database configuration
│   ├── ClipboardEntity.java          # Data model
│   └── ClipboardRepository.java      # Repository pattern
├── receiver/                          # Broadcast receivers
│   └── BootReceiver.java             # Boot completion handler
├── service/                           # Background services
│   └── ClipboardService.java         # Clipboard monitoring service
└── ui/                                # UI components
    ├── ClipboardAdapter.java         # RecyclerView adapter
    ├── ClipboardDialog.java          # History dialog
    └── ClipboardViewModel.java       # ViewModel
```

## Dependencies

- **AndroidX Libraries**:
  - AppCompat
  - Material Components
  - ConstraintLayout

- **Architecture Components**:
  - Room Database (runtime + compiler)
  - Lifecycle (ViewModel + LiveData)

- **Testing**:
  - JUnit
  - Espresso

## Building the Project

### Prerequisites

- Android Studio Hedgehog or later
- JDK 11 or later
- Android SDK with API 36

### Build Steps

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Build and run:

```bash
./gradlew assembleDebug
```

Or use Android Studio's build tools.

## Installation

### From Source

```bash
./gradlew installDebug
```

### APK Installation

Build a release APK and install manually:

```bash
./gradlew assembleRelease
adb install app/build/outputs/apk/release/app-release.apk
```

## Usage

1. Launch the app and grant notification permissions (Android 13+)
2. The clipboard monitoring service starts automatically
3. Copy any text to your clipboard - it will be saved automatically
4. Tap the notification to view clipboard history
5. The service persists across reboots

## Configuration

### Version Information

- **Version Code**: 1
- **Version Name**: 0.0.1
- **Package**: `io.celox.xclip`

### Build Configuration

The project uses:
- Java 11 compatibility
- ProGuard optimization disabled in debug builds
- Room annotation processing

## License

[Add your license information here]

## Contributing

[Add contribution guidelines here]

## Author

Celox.io

---

**Note**: This is an early version (0.0.1) under active development.
