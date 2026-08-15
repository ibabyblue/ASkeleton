package com.ibabyblue.askeleton

import kotlin.math.max
import kotlin.math.roundToInt

/** Deterministic multiline bar metrics shared by both Android renderers. */
public object SkeletonLineMetrics {
    /** Estimates line count from a resolved footprint and line height, returning at least one. */
    @JvmStatic
    public fun lineCount(height: Float, lineHeight: Float): Int {
        if (height <= 0f || lineHeight <= 0f) return 1
        return max(1, (height / lineHeight).roundToInt())
    }

    /** Returns the nonnegative bar height within one line box. */
    @JvmStatic
    @JvmOverloads
    public fun barHeight(lineHeight: Float, ratio: Float = 0.7f): Float = max(0f, lineHeight * ratio)
}
