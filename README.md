# ASkeleton

ASkeleton is a Kotlin Android library for synchronized, slot-level skeleton loading states in Jetpack Compose and Android Views. Each placeholder keeps the real view's footprint, while both renderers derive shimmer phase from the same frame-time calculation.

![Android API 23+](https://img.shields.io/badge/Android-API%2023%2B-brightgreen)
![Kotlin 2.3.10](https://img.shields.io/badge/Kotlin-2.3.10-purple)
![AAR](https://img.shields.io/badge/distribution-AAR-blue)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

## Features

- Slot-level placeholders that preserve the loaded layout
- In-phase shimmer across Jetpack Compose and Android Views
- Rounded rectangle, circle, and capsule shapes
- Eight horizontal, vertical, and diagonal sweep directions
- Footprint-driven Compose text bars and content-driven `TextView` bars
- Bitmap alpha masks for logos and silhouettes
- Original-color and tinted image shimmer with aspect-fit sizing and live updates
- Layered overlay or single-gradient color compositing
- Compose highlight overlays masked by existing content alpha
- Composition-scoped appearance and per-modifier overrides
- Global View appearance and per-activation overrides
- One lifecycle-aware `Choreographer` clock for all attached View skeletons
- Kotlin-only implementation with no third-party runtime dependencies

## Requirements

| Toolchain or platform | Minimum |
| --- | --- |
| Android | API 23 |
| Compile SDK | 36 |
| Kotlin | 2.3.10 |
| Java toolchain | 17 |
| Android Gradle Plugin | 8.13.2 |

## Installation

ASkeleton `0.2.0` uses tagged GitHub source for JitPack builds. Add JitPack to dependency resolution:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

Then add the library to the consuming module:

```kotlin
dependencies {
    implementation("com.github.ibabyblue:ASkeleton:0.2.0")
}
```

The repository also defines the publication coordinates `io.github.ibabyblue:askeleton:0.2.0` for Maven-compatible release pipelines. Use those coordinates after deploying the artifact to a Maven repository; otherwise use JitPack, a local AAR, or `publishReleasePublicationToMavenLocal`.

To consume a locally built AAR, run `./gradlew :askeleton:assembleRelease`; the artifact is written to `askeleton/build/outputs/aar/askeleton-release.aar`.

## Jetpack Compose Quick Start

Feed representative content while data is unavailable so the hidden content reserves its final size:

```kotlin
import com.ibabyblue.askeleton.compose.skeleton

Text(
    text = title ?: "Loading product name",
    modifier = Modifier.skeleton(active = title == null),
)
```

For multiline text, provide the same resolved line height and an explicit footprint:

```kotlin
import com.ibabyblue.askeleton.compose.skeletonText

Text(
    text = summary ?: representativeSummary,
    lineHeight = 20.sp,
    modifier = Modifier
        .fillMaxWidth()
        .height(60.dp)
        .skeletonText(active = summary == null, lineHeight = 20.dp),
)
```

Use `SkeletonAppearance` for a subtree override or pass `appearance` to one modifier:

```kotlin
SkeletonAppearance(customAppearance) {
    ProfileCard(modifier = Modifier.skeleton(isLoading))
}
```

## Android View Quick Start

View activation snapshots the current appearance and `TextView` layout. Deactivate before changing content or configuration:

```kotlin
import com.ibabyblue.askeleton.view.skeleton

fun bindTitle(title: String?) {
    titleView.skeleton(false)
    titleView.text = title ?: "Loading product name"
    titleView.skeleton(title == null)
}
```

Set a process-wide default with `Skeleton.appearance`, or pass `appearance` for one activation. All View operations must run on the main thread. Repeated activation and deactivation are idempotent.

## Shapes, Directions, and Image Masks

```kotlin
avatarModifier.skeleton(isLoading, shape = SkeletonShape.Circle)

logoModifier.skeleton(
    active = isLoading,
    mask = logoBitmap.asImageBitmap(),
)

logoImageView.skeleton(
    active = true,
    mask = SkeletonMask.OwnImage,
)
```

`SkeletonMask.OwnImage` requires an `ImageView` whose current drawable is a `BitmapDrawable`. An unavailable or recycled bitmap is a safe no-op. Transparent-background bitmaps create silhouettes; opaque bitmaps create rectangles.

Image masks are aspect-fitted and centered without changing the host footprint. Pixels outside the fitted mask destination remain transparent in both Compose and Android Views.

Geometric and image-mask activation share one View overlay. Deactivate before switching rendering modes.

All eight directions are available through `ShimmerDirection`: left/right, top/bottom, and the four diagonals. Compose and Views use the same absolute frame timestamp, so equal configurations remain visually in phase.

## Image Styles

Use `SkeletonImage` (Compose) or `SkeletonImageView` (Views) when an image should remain visible beneath a sweep or retain a tint after animation stops. Both center and aspect-fit the image without cropping or stretching.

| Base style | While active | While inactive |
| --- | --- | --- |
| `SkeletonImageBaseStyle.Original` | Original pixels beneath an image-alpha-masked highlight | Original pixels |
| `SkeletonImageBaseStyle.Tint(color)` | A base-highlight-base gradient masked by image alpha | A static silhouette in the tint color |

Image appearance is configured explicitly, independently of `SkeletonAppearance` and `Skeleton.appearance`:

```kotlin
import com.ibabyblue.askeleton.SkeletonColor
import com.ibabyblue.askeleton.SkeletonImageBaseStyle
import com.ibabyblue.askeleton.SkeletonImageConfiguration

val imageAppearance = SkeletonImageConfiguration(
    baseStyle = SkeletonImageBaseStyle.Original,
    highlightColor = SkeletonColor(1f, 1f, 1f, 0.6f),
)
val tintedAppearance = imageAppearance.copy(
    baseStyle = SkeletonImageBaseStyle.Tint(SkeletonColor(0.1f, 0.35f, 1f, 0.25f)),
)
```

### Compose

```kotlin
import androidx.compose.ui.graphics.asImageBitmap
import com.ibabyblue.askeleton.compose.SkeletonImage

SkeletonImage(
    image = bitmap.asImageBitmap(),
    configuration = imageAppearance, // Or tintedAppearance
    active = isLoading,
    modifier = Modifier.width(96.dp),
    contentDescription = "Product illustration", // Null for a decorative image
)
```

Width-only sizing derives height from the image ratio: a 2:1 image is 48 dp tall at 96 dp wide. Use `Modifier.size(96.dp)` for a fixed square slot with centered letterboxing instead. Changing `image`, `configuration`, or `active` updates the component through recomposition; frame observation ends when animation is disabled or the component leaves composition.

### Android Views

```kotlin
import android.view.ViewGroup
import com.ibabyblue.askeleton.view.SkeletonImageView

val widthPx = (96 * context.resources.displayMetrics.density).toInt()
val slot = SkeletonImageView(context).apply {
    layoutParams = ViewGroup.LayoutParams(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT)
    image = bitmap
    configuration = imageAppearance
    isActive = isLoading
}

// Update on the main thread, including while active:
slot.image = replacementBitmap
slot.configuration = tintedAppearance
slot.isActive = false // Keeps the static tinted silhouette
```

The caller owns the bitmap. Null or recycled bitmaps render empty. `SkeletonImageView` unregisters animation when detached or when its window becomes invisible. Unlike the existing `View.skeleton` extension, its image and configuration setters update an active component without manual deactivation.

`SkeletonImageConfiguration` defaults to a 1,400 ms duration, band width `1f`, and `LeftToRight`. For both image components, nonpositive duration or nonpositive/nonfinite band width keeps the static base even when `active` is true. Loading decisions and bitmap acquisition belong to the application.

## Color Compositing

`SkeletonConfiguration.fillMode` applies to geometric, text-bar, and bitmap-mask skeletons in both toolkits:

| Fill mode | Rendering |
| --- | --- |
| `SkeletonFillMode.Overlay` (default) | Draw a static base, then composite a transparent-highlight-transparent sweep over it |
| `SkeletonFillMode.Gradient` | Draw a single base-highlight-base gradient, keeping the configured alpha at the highlight peak |

For example, a base alpha of `0.25` and highlight alpha of `0.6` combine to `0.7` at the peak in Overlay mode; Gradient uses `0.6` before the image mask and background are applied.

```kotlin
import com.ibabyblue.askeleton.SkeletonConfiguration
import com.ibabyblue.askeleton.SkeletonFillMode

val gradientAppearance = SkeletonConfiguration.Default.copy(
    fillMode = SkeletonFillMode.Gradient,
)
// Compose: Modifier.skeleton(isLoading, appearance = gradientAppearance)
// Views: view.skeleton(isLoading, appearance = gradientAppearance)
```

Existing calls retain the default Overlay appearance. When changing an active `View.skeleton` configuration, deactivate and activate again to refresh its snapshot.

## Content-Alpha Overlay (Compose)

`Modifier.skeletonOverlay` keeps existing content visible and masks only the highlight using the rendered content alpha, including partial opacity. Place it **before** drawing modifiers that should be included in the mask:

```kotlin
import com.ibabyblue.askeleton.compose.skeletonOverlay

Box(
    Modifier.size(64.dp)
        .skeletonOverlay(
            active = isLoading,
            highlightColor = SkeletonColor(1f, 1f, 1f, 0.6f),
        )
        .background(Color.Blue.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
)
```

The modifier does not change measurement, semantics, or input handling. Disabling it removes the sweep while leaving content visible. It defaults to a 1,400 ms duration, band width `0.6f`, and `LeftToRight`; pass `direction` explicitly when sharing a direction control with other components.

## Important Sizing Contract

ASkeleton cannot infer future layout from absent data. Use representative strings for data-driven text, placeholder rows for unloaded collections, and explicit sizes for media. A zero-size slot produces a zero-size skeleton.

Compose multiline bars derive their count from the modifier footprint and the supplied `lineHeight`; the final bar uses 60% width by default. Android View text bars derive their real widths and line fragments from the current `TextView` text, font, width, wrapping, and `maxLines` without changing measurement.

## Example

Run the `example` application to switch between live Jetpack Compose and Android View labs. It demonstrates all directions, shape rendering, multiline text, inherited appearance, per-slot overrides, loading toggles, and bitmap masks.

The **Image styles** section adds Original/Tint and Overlay/Gradient comparisons, live landscape/portrait and style switching, width-only image sizing, zero-duration static rendering, and a Compose content-alpha overlay. Its controls retain their state when switching renderers.

```bash
./gradlew :example:installDebug
```

See [example/README.md](example/README.md) for build and validation commands.

## Build and Publish

```bash
./gradlew test lint :askeleton:assembleRelease :example:assembleDebug
./gradlew :askeleton:connectedDebugAndroidTest # With a device or emulator
./gradlew :askeleton:publishReleasePublicationToMavenLocal
```

The release publication contains the AAR, sources JAR, Gradle Module Metadata, and a POM with license, author, SCM, and project information.

## License

ASkeleton is available under the MIT License. See [LICENSE](LICENSE).

Release history is maintained in [CHANGELOG.md](CHANGELOG.md).
