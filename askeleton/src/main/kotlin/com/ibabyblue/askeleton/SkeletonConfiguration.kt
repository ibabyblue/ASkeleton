package com.ibabyblue.askeleton

/**
 * Platform-neutral appearance and animation values for one skeleton activation.
 *
 * Use [copy] to derive local variants. Durations are milliseconds and dimensions are density-independent pixels.
 */
public data class SkeletonConfiguration(
    public val baseColor: SkeletonColor,
    public val highlightColor: SkeletonColor,
    public val durationMillis: Long = 1_400L,
    public val bandWidth: Float = 0.6f,
    public val cornerRadiusDp: Float = 5f,
    public val direction: ShimmerDirection = ShimmerDirection.LeftToRight,
    public val fillMode: SkeletonFillMode = SkeletonFillMode.Overlay,
) {
    /** Default neutral-gray fill and near-white highlight appearance. */
    public companion object {
        @JvmField
        public val Default: SkeletonConfiguration = SkeletonConfiguration(
            baseColor = SkeletonColor(red = 0.91f, green = 0.85f, blue = 0.85f, alpha = 0.8f),
            highlightColor = SkeletonColor(red = 0.99f, green = 0.98f, blue = 0.98f, alpha = 0.8f),
        )
    }
}
