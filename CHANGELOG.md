# Changelog

All notable changes to ASkeleton are documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project uses semantic versioning.

## [Unreleased]

### Added

- Added original-pixel and tinted-silhouette image components with aspect-fit sizing and static restoration.
- Added single-gradient fill mode alongside the existing layered color rendering.
- Added highlight-only content-alpha overlays for existing shapes.
- Added rendering regression coverage for alpha compositing, image sizing, and updates.

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

[0.1.1]: https://github.com/ibabyblue/ASkeleton/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/ibabyblue/ASkeleton/releases/tag/0.1.0
