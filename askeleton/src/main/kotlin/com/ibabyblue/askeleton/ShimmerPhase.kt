package com.ibabyblue.askeleton

/** Converts a shared frame timestamp into a normalized shimmer leading-edge position. */
public object ShimmerPhase {
    /**
     * Returns a phase in `-bandWidth..<1`, looping every [durationMillis].
     *
     * Non-positive durations freeze the band immediately before the leading edge.
     */
    @JvmStatic
    public fun phase(timeNanos: Long, durationMillis: Long, bandWidth: Float): Float {
        if (durationMillis <= 0L) return -bandWidth
        val durationNanos = durationMillis * 1_000_000.0
        val rawRemainder = timeNanos.toDouble() % durationNanos
        val remainder = if (rawRemainder < 0.0) rawRemainder + durationNanos else rawRemainder
        val progress = (remainder / durationNanos).toFloat()
        return -bandWidth + progress * (1f + bandWidth)
    }
}
