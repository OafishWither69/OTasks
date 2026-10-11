# OTasks

Offline Android meal planning, built from the root [SPEC.md](SPEC.md). Phase 1 is in progress. No user data or networking is included in the foundation shell. The archived Bulk Tracker prototype is available at the `prototype-final` tag; it is a separate package and its data is not migrated.

## Build

Use JDK 17 and Android SDK platform 37. Put `sdk.dir` in untracked `local.properties` or set ANDROID_HOME. The checked-in Gradle wrapper downloads the pinned distribution and checks its SHA-256.

On Windows use `gradlew.bat`; on Linux/macOS use `./gradlew`:

```
./gradlew :domain:test :app:testDebugUnitTest :app:checkVersioning lintDebug assembleDebug spotlessCheck
./gradlew recordRoborazziDebug
./gradlew verifyRoborazziDebug
```

Screenshot recording changes baselines deliberately; verification compares against committed images. Connected-device checks and baseline generation require an emulator or device.

## Installation and updates

Debug APK: app/build/outputs/apk/debug/app-debug.apk (`com.otasks.app.debug`). Enable installation from your file manager, then open the APK. Release APKs use `com.otasks.app` and a separate permanent release key; install updates over the existing app using the same signing certificate. Keep a JSON backup before updates once backup support is implemented. Signing is deliberately required for a release build; it never falls back to the debug key.

## Status

See [PROGRESS.md](PROGRESS.md) for actual checks and remaining release gates. No phase is complete until its independent review and real-device acceptance checks pass. Code is GPL-3.0-or-later; bundled fonts use the SIL Open Font Licence.

## Verify a download

Compare the APK SHA-256 with its `.sha256` file (`Get-FileHash` on Windows, `sha256sum` on Linux). Use the Android SDK's `apksigner verify --print-certs` to check the signing certificate. Every release must have the same certificate, also listed in the release notes. Debug builds use a different certificate and package.

## Local Windows build

This workspace is in OneDrive. OneDrive marks generated directories read-only, which can stop Gradle from replacing its intermediate files. Local verification uses an untracked Gradle init script to keep temporary build output in `%LOCALAPPDATA%/OTasksTools/build`. Normal checkouts and CI use the standard `app/build` and `domain/build` paths. The portable JDK and SDK are outside the repository; no tool paths or credentials are committed.
