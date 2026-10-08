package com.ibabyblue.askeleton

/** Color compositing performed before the placeholder's alpha mask is applied. */
public enum class SkeletonFillMode {
    /** Draws a transparent highlight over a static base; retains existing appearance. */
    Overlay,
    /** Draws a single base-highlight-base gradient, retaining the configured peak alpha. */
    Gradient,
}
