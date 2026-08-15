package com.ibabyblue.askeleton

/** A normalized point used to describe shimmer gradient geometry. */
public data class NormalizedPoint(public val x: Float, public val y: Float)

/** The normalized start and end points of one shimmer frame. */
public data class GradientPoints(public val start: NormalizedPoint, public val end: NormalizedPoint)

/** A direction for horizontal, vertical, or diagonal shimmer movement. */
public enum class ShimmerDirection(
    public val start: NormalizedPoint,
    public val end: NormalizedPoint,
) {
    LeftToRight(NormalizedPoint(0f, 0.5f), NormalizedPoint(1f, 0.5f)),
    RightToLeft(NormalizedPoint(1f, 0.5f), NormalizedPoint(0f, 0.5f)),
    TopToBottom(NormalizedPoint(0.5f, 0f), NormalizedPoint(0.5f, 1f)),
    BottomToTop(NormalizedPoint(0.5f, 1f), NormalizedPoint(0.5f, 0f)),
    TopLeftToBottomRight(NormalizedPoint(0f, 0f), NormalizedPoint(1f, 1f)),
    TopRightToBottomLeft(NormalizedPoint(1f, 0f), NormalizedPoint(0f, 1f)),
    BottomLeftToTopRight(NormalizedPoint(0f, 1f), NormalizedPoint(1f, 0f)),
    BottomRightToTopLeft(NormalizedPoint(1f, 1f), NormalizedPoint(0f, 0f));

    /** Calculates the gradient endpoints for a leading-edge [phase] and normalized [bandWidth]. */
    public fun gradientPoints(phase: Float, bandWidth: Float): GradientPoints = GradientPoints(
        start = interpolate(phase),
        end = interpolate(phase + bandWidth),
    )

    private fun interpolate(value: Float): NormalizedPoint = NormalizedPoint(
        x = start.x + (end.x - start.x) * value,
        y = start.y + (end.y - start.y) * value,
    )
}
