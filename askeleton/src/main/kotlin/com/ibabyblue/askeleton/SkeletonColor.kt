package com.ibabyblue.askeleton

import android.graphics.Color
import kotlin.math.roundToInt

/**
 * An sRGB color whose components are expressed in the inclusive range `0f..1f`.
 */
public data class SkeletonColor(
    public val red: Float,
    public val green: Float,
    public val blue: Float,
    public val alpha: Float = 1f,
) {
    init {
        require(red in 0f..1f) { "red must be in 0f..1f" }
        require(green in 0f..1f) { "green must be in 0f..1f" }
        require(blue in 0f..1f) { "blue must be in 0f..1f" }
        require(alpha in 0f..1f) { "alpha must be in 0f..1f" }
    }

    /** Converts this value to an Android ARGB color integer. */
    public fun toArgb(): Int = Color.argb(
        (alpha * 255f).roundToInt(),
        (red * 255f).roundToInt(),
        (green * 255f).roundToInt(),
        (blue * 255f).roundToInt(),
    )
}
