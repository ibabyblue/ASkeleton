package com.ibabyblue.askeleton

import kotlin.math.min

/** A geometric shape used to clip a skeleton placeholder. */
public sealed interface SkeletonShape {
    /** A rounded rectangle. `null` uses [SkeletonConfiguration.cornerRadiusDp]. */
    public data class RoundedRect(public val cornerRadiusDp: Float? = null) : SkeletonShape

    /** A circle for square slots and a shortest-side rounded shape for rectangular slots. */
    public data object Circle : SkeletonShape

    /** A capsule whose radius is half its shorter dimension. */
    public data object Capsule : SkeletonShape

    /** Resolves a radius when all values use the same concrete unit. */
    public fun cornerRadius(width: Float, height: Float, defaultRadius: Float): Float = when (this) {
        is RoundedRect -> cornerRadiusDp ?: defaultRadius
        Circle, Capsule -> min(width, height) / 2f
    }
}
