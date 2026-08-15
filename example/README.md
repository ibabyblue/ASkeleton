# ASkeleton Example

The Example application is an integration catalog outside the published AAR. Its UI and layouts are written entirely in Kotlin.

The Compose lab demonstrates inherited appearance, shape clipping, multiline bars, per-modifier overrides, direction changes, and bitmap-alpha masks. The Android View lab demonstrates the equivalent `ViewOverlay` lifecycle, content-driven `TextView` bars, per-activation appearance, circle slots, and `ImageView` own-image masks.

Build the application:

```bash
./gradlew :example:assembleDebug
```

Install it on a connected device or emulator:

```bash
./gradlew :example:installDebug
```

Run library unit tests and Android instrumentation tests:

```bash
./gradlew :askeleton:testDebugUnitTest
./gradlew :askeleton:connectedDebugAndroidTest
```

The application uses only local representative data and does not require network access.
