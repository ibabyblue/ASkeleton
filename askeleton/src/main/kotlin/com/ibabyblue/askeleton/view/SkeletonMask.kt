package com.ibabyblue.askeleton.view

import android.graphics.Bitmap

/** A bitmap alpha source that clips an Android View skeleton. */
public sealed interface SkeletonMask {
    /** Uses the supplied bitmap's alpha channel. */
    public data class Image(public val bitmap: Bitmap) : SkeletonMask

    /** Uses the receiver's bitmap when the receiver is an `ImageView` backed by `BitmapDrawable`. */
    public data object OwnImage : SkeletonMask
}
