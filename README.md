# AppAndroidKotlin

## Description

Native Android application developed in Kotlin with Jetpack Compose for the user interface.

## Prerequisites

- **Android Studio** (version Hedgehog or higher)
- **Java Development Kit (JDK)** 11 or higher
- **Git**
- Android SDK compile version 36

## Installation and Configuration

### 1. Clone the Repository

```bash
git clone <repository-url>
cd AppAndroidKotlin
```

### 2. Sync Dependencies

Open the project in Android Studio. Gradle synchronization will run automatically. If needed, force it manually:

- Menu: `File > Sync Now`

### 3. Configure Android SDK

1. Go to `File > Project Structure > SDK Location`
2. Set the Android SDK path (if not configured automatically)
3. Ensure compile version 36 is installed

## Launching the Application

### From Android Studio

1. Select a device or emulator from the top toolbar
2. Press the **Run** button (Ctrl + R) or go to `Run > Run 'app'`
3. Select the execution target

### From Terminal

```bash
./gradlew assembleDebug    # Build debug APK
./gradlew installDebug     # Install on device/emulator
```

## Project Specifications

| Feature | Value |
|---|---|
| Minimum Android Version | API 24 (Android 7.0) |
| Target Android Version | API 36 |
| Kotlin Version | Compatible with libs.versions.toml |
| Java Compatibility | 11+ |
| UI Framework | Jetpack Compose |

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/gn41/appandroidkotlin/
│   │   └── res/
│   ├── androidTest/
│   └── test/
└── build.gradle.kts
```

## Main Dependencies

- Jetpack Compose
- AndroidX Core
- AndroidX Lifecycle
- Material Design 3

## Build and Compilation

```bash
# Build debug
./gradlew assembleDebug

# Build release
./gradlew assembleRelease

# Run tests
./gradlew test
```

## License

To be defined.

## Support

For inquiries, contact the development team.

