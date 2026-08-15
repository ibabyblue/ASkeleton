package com.ibabyblue.askeleton.view

import com.ibabyblue.askeleton.SkeletonConfiguration

/** Holds the process-wide appearance snapshotted by Android View activations. */
public object Skeleton {
    /**
     * Appearance used when a View activation does not supply an override.
     *
     * Read and write this property from the main thread, before activating affected views.
     */
    @JvmStatic
    public var appearance: SkeletonConfiguration = SkeletonConfiguration.Default
}
