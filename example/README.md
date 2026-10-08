# ASkeleton Example

The Example application is an integration catalog outside the published AAR. Its UI and layouts are written entirely in Kotlin.

The Compose lab demonstrates inherited appearance, shape clipping, multiline bars, per-modifier overrides, direction changes, and bitmap-alpha masks. The Android View lab demonstrates the equivalent `ViewOverlay` lifecycle, content-driven `TextView` bars, per-activation appearance, circle slots, and `ImageView` own-image masks.

The **Image styles** section uses the selected renderer and retains its controls when switching between Compose and Views:

- Compare **Original** and **Tint** in outlined 96 × 96 dp slots. The colored sample includes transparent margins and a translucent shape.
- Tap **Image: … · Swap** to switch between 2:1 landscape and 2:3 portrait images while loading. The **Live updates** slot has only a 96 dp width, so its height changes between 48 and 144 dp.
- Tap **Live style: … · Switch** to update that slot's Original/Tint style without stopping loading.
- Tap the image animation button to use a zero duration and inspect static rendering while loading remains enabled.
- Compare **Overlay** and **Gradient** side by side for capsule and image-mask fills, using the same partially transparent colors and dark background.
- In Compose, compare a half-transparent blue shape with and without `skeletonOverlay`.

The top-level **Direction** button controls all animated samples in this section. **Show content** restores original pixels or a static tint for image components, removes shape/mask placeholders, and removes the content overlay. The older profile labs retain their explicitly configured local direction overrides.

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
