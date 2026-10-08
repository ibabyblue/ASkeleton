package com.ibabyblue.askeleton

/** The static image style retained when animation is inactive. */
public sealed interface SkeletonImageBaseStyle {
    /** Keeps the source's original RGB and alpha beneath highlight-only rendering. */
    public data object Original : SkeletonImageBaseStyle
    /** Fills the source alpha silhouette with [color]. */
    public data class Tint(public val color: SkeletonColor) : SkeletonImageBaseStyle
}

/** Image appearance independent of application loading state; duration is in milliseconds. */
public data class SkeletonImageConfiguration(
    public val baseStyle: SkeletonImageBaseStyle,
    public val highlightColor: SkeletonColor,
    public val durationMillis: Long = 1_400L,
    public val bandWidth: Float = 1f,
    public val direction: ShimmerDirection = ShimmerDirection.LeftToRight,
) {
    /** Resolves original or tinted semantics into the shared framework fill renderer. */
    public val fillConfiguration: SkeletonConfiguration
        get() = SkeletonConfiguration(
            baseColor = (baseStyle as? SkeletonImageBaseStyle.Tint)?.color ?: highlightColor.copy(alpha = 0f),
            highlightColor = highlightColor,
            durationMillis = durationMillis,
            bandWidth = bandWidth,
            direction = direction,
            fillMode = SkeletonFillMode.Gradient,
        )
}
