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

ASkeleton `0.1.1` is distributed from its tagged GitHub source through JitPack. Add JitPack to dependency resolution:

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
    implementation("com.github.ibabyblue:ASkeleton:0.1.1")
}
```

The repository also defines the publication coordinates `io.github.ibabyblue:askeleton:0.1.1` for Maven-compatible release pipelines. Use those coordinates after deploying the artifact to a Maven repository; otherwise use JitPack, a local AAR, or `publishReleasePublicationToMavenLocal`.

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

## Important Sizing Contract

ASkeleton cannot infer future layout from absent data. Use representative strings for data-driven text, placeholder rows for unloaded collections, and explicit sizes for media. A zero-size slot produces a zero-size skeleton.

Compose multiline bars derive their count from the modifier footprint and the supplied `lineHeight`; the final bar uses 60% width by default. Android View text bars derive their real widths and line fragments from the current `TextView` text, font, width, wrapping, and `maxLines` without changing measurement.

## Example

Run the `example` application to switch between live Jetpack Compose and Android View labs. It demonstrates all directions, shape rendering, multiline text, inherited appearance, per-slot overrides, loading toggles, and bitmap masks.

```bash
./gradlew :example:installDebug
```

See [example/README.md](example/README.md) for build and validation commands.

## Build and Publish

```bash
./gradlew clean test lint assembleRelease
./gradlew :askeleton:publishReleasePublicationToMavenLocal
```

The release publication contains the AAR, sources JAR, Gradle Module Metadata, and a POM with license, author, SCM, and project information.

## License

ASkeleton is available under the MIT License. See [LICENSE](LICENSE).

Release history is maintained in [CHANGELOG.md](CHANGELOG.md).

## Image Styles and Color Compositing

`SkeletonImage` (Compose) and `SkeletonImageView` (Views) share an aspect-fit image contract:
`Original` retains source RGB and alpha under a highlight-only overlay; `Tint(color)` renders a single
base-highlight-base gradient and retains a static tinted silhouette when inactive.

```kotlin
val appearance = SkeletonImageConfiguration(
    baseStyle = SkeletonImageBaseStyle.Original,
    highlightColor = SkeletonColor(1f, 1f, 1f, 0.5f),
)
// Import com.ibabyblue.askeleton.compose.SkeletonImage
SkeletonImage(bitmap.asImageBitmap(), appearance, active = isLoading, modifier = Modifier.width(80.dp))

// Import com.ibabyblue.askeleton.view.SkeletonImageView
val slot = SkeletonImageView(context).apply {
    image = bitmap
    configuration = appearance
    isActive = true
}
```

The View component retains bitmap ownership with the caller, treats missing/recycled images as empty,
and unregisters its animation on detachment or window invisibility. Changing configuration or image updates
an active component without manual teardown. A nonpositive duration or band width retains the static base.
Compose animation is disposed with composition and follows its frame clock. Width-only sizing preserves aspect ratio.

Existing `skeleton` calls keep their activation snapshot contract and default layered appearance.
`SkeletonConfiguration.fillMode = SkeletonFillMode.Gradient` selects a single base-highlight-base gradient,
preserving the specified peak alpha. The default `Overlay` draws highlight over the base instead.

For existing colored shapes, `Modifier.skeletonOverlay(active, highlightColor)` keeps content visible and
masks only the highlight with the rendered content alpha, including partial opacity. Place it before
drawing modifiers that belong to the mask, for example `Modifier.skeletonOverlay(...).background(...)`. It does not alter
measurement, semantics, or input handling. Bitmap selection, layout, colors, and loading decisions remain
application responsibilities. No text-specific shimmer component is added.
