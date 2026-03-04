# FileKit iOS UI Freeze Repro

Minimal Kotlin Multiplatform project to reproduce [FileKit #502](https://github.com/vinceglb/FileKit/issues/502) — iOS UI freeze when reading files via `PlatformFile.source()`.

## The Issue

FileKit's `source()` freezes the iOS UI because `startAccessingSecurityScopedResource()` and `stopAccessingSecurityScopedResource()` are called on the main thread, blocking UI updates.

This app picks a file and uploads it to [httpbin.org/post](https://httpbin.org/post) using Ktor's `submitFormWithBinaryData` with `InputProvider { file.source().buffered() }` — the same pattern used in real apps. A spinning `CircularProgressIndicator` and heartbeat counter make any UI freeze visible.

## Prerequisites

- macOS with Xcode 16+ installed
- JDK 17+
- A large file (50MB+) available on the iOS Simulator or device

## Setup

1. Open the Xcode project:

   ```
   open iosApp/iosApp.xcodeproj
   ```

2. Select an iOS Simulator or device target in Xcode.

3. Build and run (⌘R). The first build will take a few minutes as Gradle compiles the Kotlin framework.

## Preparing a Test File

The file picker needs a large file to make the UI freeze visible. Copy a file to the simulator:

### Option A: Drag and drop to Simulator

1. Find or create a large file (e.g., a 50MB+ video or zip).
2. Open the **Files** app on the simulator.
3. Drag the file from Finder directly onto the simulator window — it will be saved to the Files app's "On My iPhone" location.

### Option B: Use iCloud Drive

If signed into iCloud on the simulator or device, upload a large file to iCloud Drive from your Mac. It will be accessible via the Files app picker.

## Reproducing the Freeze

1. Launch the app.
2. Observe the spinning indicator and heartbeat counter incrementing smoothly.
3. Tap **"Pick a file"** and select a large file from the Files app.
4. Watch the spinner — if it **freezes or stutters**, the bug is reproduced. The heartbeat counter will also pause.

The freeze occurs when `file.source()` is called inside the `InputProvider` lambda during the Ktor upload, which internally invokes `startAccessingSecurityScopedResource()` on the main thread.

## Versions

| Dependency | Version |
|---|---|
| Kotlin | 2.3.10 |
| Compose Multiplatform | 1.10.1 |
| FileKit | 0.13.0 |
| Ktor | 3.4.0 |
| kotlinx-coroutines | 1.10.2 |
| kotlinx-io | 0.9.0 |
