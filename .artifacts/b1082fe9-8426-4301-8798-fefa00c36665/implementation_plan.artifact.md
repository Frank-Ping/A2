# Implementation Plan - Part A: Welcome and Sensor Screens

Implement the first stage of the Wear OS application with a welcome screen and a sensor activity.

## User Review Required

> [!IMPORTANT]
> `SensorActivity` will be created in the `presentation` package as per instructions.

## Proposed Changes

### Resources

#### [MODIFY] [strings.xml](file:///C:/00Waikato26B/551/A2/app/src/main/res/values/strings.xml)
Add strings for:
- Welcome title: "Yuanhao's App"
- Open sensors button text: "Open Sensors"
- Sensor screen heading: "Sensor Data"
- Waiting message: "Waiting for sensor data"
- Back button text: "Back"

### Manifest

#### [MODIFY] [AndroidManifest.xml](file:///C:/00Waikato26B/551/A2/app/src/main/AndroidManifest.xml)
- Register `.presentation.SensorActivity` with `android:exported="false"`.

### Presentation

#### [NEW] [SensorActivity.kt](file:///C:/00Waikato26B/551/A2/app/src/main/java/com/example/a2/presentation/SensorActivity.kt)
- Create `SensorActivity` class.
- Implement UI using Wear Material 3: `AppScaffold`, `ScreenScaffold`, `TransformingLazyColumn`.
- Components:
    - Heading (Test tag: `SensorHeading`)
    - Status message (Test tag: `SensorStatusText`)
    - Back button calling `finish()` (Test tag: `BackButton`)

#### [MODIFY] [MainActivity.kt](file:///C:/00Waikato26B/551/A2/app/src/main/java/com/example/a2/presentation/MainActivity.kt)
- Replace template content with Welcome Screen.
- Components:
    - Title (Test tag: `WelcomeTitle`)
    - Button to open `SensorActivity` (Test tag: `OpenSensorsButton`)

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to verify compilation.
- Run `./gradlew test` (if any tests exist, though none are required for this stage beyond infrastructure).

### Manual Verification
- Deploy to Wear OS emulator.
- Verify "Yuanhao's App" title is visible.
- Click "Open Sensors" and verify it navigates to `SensorActivity`.
- Verify `SensorActivity` shows "Sensor Data" and "Waiting for sensor data".
- Click "Back" and verify it returns to `MainActivity`.
