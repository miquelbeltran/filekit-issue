# FileKit iOS UI Freeze Repro

Minimal Kotlin Multiplatform project to reproduce [FileKit #502](https://github.com/vinceglb/FileKit/issues/502) — iOS UI freeze when reading files via `PlatformFile.source()`.

## Prerequisites

- macOS with Xcode 16+ installed
- JDK 17+
- A large file (50MB+) available on the iOS Simulator or device to make the freeze noticeable

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
