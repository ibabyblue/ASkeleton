package com.ibabyblue.askeleton.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.ibabyblue.askeleton.SkeletonColor
import com.ibabyblue.askeleton.SkeletonImageBaseStyle
import com.ibabyblue.askeleton.SkeletonImageConfiguration
import com.ibabyblue.askeleton.SkeletonShape
import kotlin.math.roundToInt

/**
 * An aspect-fit bitmap slot with original-pixel or tinted-silhouette shimmer.
 * Configure on the main thread. Bitmap ownership stays with the caller; recycled images render empty.
 */
public class SkeletonImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    /** Image pixels used by both the static rendering and its alpha mask. */
    public var image: Bitmap? = null
        set(value) {
            if (field === value) return
            field = value
            requestLayout()
            updateRendering()
        }

    /** Mutable image appearance; changed values replace an active overlay. */
    public var configuration: SkeletonImageConfiguration = SkeletonImageConfiguration(
        SkeletonImageBaseStyle.Original, SkeletonColor(1f, 1f, 1f, 0.8f),
    )
        set(value) {
            if (field == value) return
            field = value
            updateRendering()
        }

    /** Requested animation state. Attachment and window visibility gate actual frame work. */
    public var isActive: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            updateRendering()
        }

    private val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private var shimmer: SkeletonDrawable? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val bitmap = image?.takeUnless(Bitmap::isRecycled)
        val naturalWidth = bitmap?.getScaledWidth(resources.displayMetrics) ?: 0
        val naturalHeight = bitmap?.getScaledHeight(resources.displayMetrics) ?: 0
        val horizontalPadding = paddingLeft + paddingRight
        val verticalPadding = paddingTop + paddingBottom
        var width = resolveSize(naturalWidth + horizontalPadding, widthMeasureSpec)
        var height = resolveSize(naturalHeight + verticalPadding, heightMeasureSpec)
        if (naturalWidth > 0 && naturalHeight > 0) {
            if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
                height = resolveSize(((width - horizontalPadding).coerceAtLeast(0) *
                    naturalHeight.toFloat() / naturalWidth).roundToInt() + verticalPadding, heightMeasureSpec)
            }
            if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
                width = resolveSize(((height - verticalPadding).coerceAtLeast(0) *
                    naturalWidth.toFloat() / naturalHeight).roundToInt() + horizontalPadding, widthMeasureSpec)
            }
        }
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bitmap = image?.takeUnless(Bitmap::isRecycled) ?: return
        val source = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        val slot = RectF(paddingLeft.toFloat(), paddingTop.toFloat(),
            (width - paddingRight).toFloat(), (height - paddingBottom).toFloat())
        if (slot.isEmpty) return
        val destination = RectF(source)
        Matrix().apply { setRectToRect(source, slot, Matrix.ScaleToFit.CENTER) }.mapRect(destination)
        val tint = configuration.baseStyle as? SkeletonImageBaseStyle.Tint
        if (tint == null || shimmer == null) {
            imagePaint.colorFilter = tint?.let { PorterDuffColorFilter(it.color.toArgb(), PorterDuff.Mode.SRC_IN) }
            canvas.drawBitmap(bitmap, null, destination, imagePaint)
        }
        shimmer?.draw(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateRendering()
    }

    override fun onAttachedToWindow() { super.onAttachedToWindow(); updateRendering() }
    override fun onDetachedFromWindow() { stopShimmer(); super.onDetachedFromWindow() }
    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        updateRendering()
    }

    override fun verifyDrawable(who: Drawable): Boolean = who === shimmer || super.verifyDrawable(who)

    private fun stopShimmer() {
        shimmer?.let { ShimmerClock.unregister(it); it.callback = null }
        shimmer = null
    }

    private fun updateRendering() {
        stopShimmer()
        val bitmap = image?.takeUnless(Bitmap::isRecycled)
        if (isActive && isAttachedToWindow && windowVisibility == VISIBLE && width > paddingLeft + paddingRight
            && height > paddingTop + paddingBottom && bitmap != null && configuration.durationMillis > 0
            && configuration.bandWidth.isFinite() && configuration.bandWidth > 0f) {
            shimmer = SkeletonDrawable(this, configuration.fillConfiguration,
                SkeletonShape.RoundedRect(0f), bitmap).also {
                it.setBounds(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom)
                it.callback = this
                ShimmerClock.register(it)
            }
        }
        invalidate()
    }
}
