package com.ibabyblue.askeleton.view

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.ibabyblue.askeleton.R
import com.ibabyblue.askeleton.SkeletonConfiguration
import com.ibabyblue.askeleton.SkeletonShape

/** Activates or removes an in-place geometric skeleton on any Android View. */
@JvmOverloads
public fun View.skeleton(
    active: Boolean,
    shape: SkeletonShape = SkeletonShape.RoundedRect(),
    appearance: SkeletonConfiguration? = null,
) {
    assertMainThread()
    if (!active) {
        removeSkeleton()
        return
    }
    if (skeletonState != null) return
    installSkeleton(
        drawable = SkeletonDrawable(
            host = this,
            configuration = appearance ?: Skeleton.appearance,
            shape = shape,
        ),
    )
}

/** Activates or removes a skeleton clipped by bitmap alpha. Invalid mask sources are a safe no-op. */
@JvmOverloads
public fun View.skeleton(
    active: Boolean,
    mask: SkeletonMask,
    appearance: SkeletonConfiguration? = null,
) {
    assertMainThread()
    if (!active) {
        removeSkeleton()
        return
    }
    if (skeletonState != null) return
    val bitmap = resolveBitmap(mask) ?: return
    installSkeleton(
        drawable = SkeletonDrawable(
            host = this,
            configuration = appearance ?: Skeleton.appearance,
            shape = SkeletonShape.RoundedRect(),
            maskBitmap = bitmap,
        ),
    )
}

private class SkeletonState(
    val drawable: SkeletonDrawable,
    val originalTextColors: ColorStateList?,
    val layoutListener: View.OnLayoutChangeListener,
    val attachListener: View.OnAttachStateChangeListener,
)

private var View.skeletonState: SkeletonState?
    get() = getTag(R.id.askeleton_state) as? SkeletonState
    set(value) = setTag(R.id.askeleton_state, value)

private fun View.installSkeleton(drawable: SkeletonDrawable) {
    val originalTextColors = (this as? TextView)?.textColors
    if (this is TextView) setTextColor(android.graphics.Color.TRANSPARENT)

    val layoutListener = View.OnLayoutChangeListener { view, left, top, right, bottom, _, _, _, _ ->
        drawable.setBounds(0, 0, right - left, bottom - top)
        view.invalidate()
    }
    val attachListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(view: View) {
            ShimmerClock.register(drawable)
        }

        override fun onViewDetachedFromWindow(view: View) {
            ShimmerClock.unregister(drawable)
        }
    }
    val state = SkeletonState(drawable, originalTextColors, layoutListener, attachListener)
    skeletonState = state
    drawable.setBounds(0, 0, width, height)
    overlay.add(drawable)
    addOnLayoutChangeListener(layoutListener)
    addOnAttachStateChangeListener(attachListener)
    if (isAttachedToWindow) ShimmerClock.register(drawable)
}

private fun View.removeSkeleton() {
    val state = skeletonState ?: return
    ShimmerClock.unregister(state.drawable)
    overlay.remove(state.drawable)
    removeOnLayoutChangeListener(state.layoutListener)
    removeOnAttachStateChangeListener(state.attachListener)
    if (this is TextView && state.originalTextColors != null) setTextColor(state.originalTextColors)
    skeletonState = null
}

private fun View.resolveBitmap(mask: SkeletonMask): Bitmap? = when (mask) {
    is SkeletonMask.Image -> mask.bitmap.takeUnless(Bitmap::isRecycled)
    SkeletonMask.OwnImage -> ((this as? ImageView)?.drawable as? BitmapDrawable)?.bitmap?.takeUnless(Bitmap::isRecycled)
}

private fun assertMainThread() {
    check(Looper.myLooper() == Looper.getMainLooper()) { "ASkeleton View operations must run on the main thread." }
}
