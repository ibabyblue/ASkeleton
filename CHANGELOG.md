# Changelog

All notable changes to ASkeleton are documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project uses semantic versioning.

## [0.2.0] - 2026-10-08

### Added

- Added `SkeletonImage` for Compose and `SkeletonImageView` for Android Views, with `Original` and `Tint` styles, aspect-fit sizing, and static rendering when inactive.
- Added `SkeletonImageConfiguration` with live image/style updates, sweep direction, duration, and band-width configuration.
- Added `SkeletonFillMode.Gradient` for a single base-highlight-base gradient; `Overlay` remains the default for existing skeleton calls.
- Added Compose `Modifier.skeletonOverlay` for highlight-only rendering masked by existing content alpha, including partial opacity.
- Added rendering regression coverage for alpha compositing, image sizing, and updates.
- Expanded the Demo with Original/Tint and Overlay/Gradient comparisons, landscape/portrait image switching, width-only sizing, live style changes, and zero-duration rendering.

### Changed

- Read Compose frame state during drawing to avoid recomposing skeleton modifiers on every animation frame.
- Enabled bitmap filtering for Android View image masks.

## [0.1.1] - 2026-08-29

### Fixed

- Fixed Compose and Android View image masks leaving skeleton-colored strips outside an aspect-fitted bitmap destination.

## [0.1.0] - 2026-08-17

### Added

- Added the Kotlin `askeleton` Android library and release AAR publication.
- Added shared configuration, RGBA colors, shapes, eight directions, line metrics, and absolute-time shimmer phase.
- Added Jetpack Compose geometric, multiline-text, and bitmap-mask modifiers with composition-scoped appearance.
- Added Android View overlays, content-driven `TextView` bars, bitmap masks, global and per-activation appearance, and a shared lifecycle-aware clock.
- Added an offline Example application covering both Android UI toolkits.
- Added unit, instrumentation, lint, CI, Maven publication, license, and repository documentation.

[0.2.0]: https://github.com/ibabyblue/ASkeleton/compare/0.1.1...0.2.0
[0.1.1]: https://github.com/ibabyblue/ASkeleton/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/ibabyblue/ASkeleton/releases/tag/0.1.0
