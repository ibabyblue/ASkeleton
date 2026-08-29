package com.ibabyblue.askeleton.view

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.widget.TextView
import com.ibabyblue.askeleton.ShimmerPhase
import com.ibabyblue.askeleton.SkeletonConfiguration
import com.ibabyblue.askeleton.SkeletonShape
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.math.min

/** Draws one activation without participating in host measurement or layout. */
internal class SkeletonDrawable(
    host: android.view.View,
    val configuration: SkeletonConfiguration,
    private val shape: SkeletonShape,
    private val maskBitmap: Bitmap? = null,
) : Drawable() {
    private data class Bar(val rect: RectF, val radius: Float)

    private val hostReference = WeakReference(host)
    private val density = host.resources.displayMetrics.density
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shimmerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }
    private var phase: Float = -configuration.bandWidth

    override fun draw(canvas: Canvas) {
        if (bounds.isEmpty) return
        if (maskBitmap != null) {
            drawImageMasked(canvas, maskBitmap)
        } else {
            drawBars(canvas, bars())
        }
    }

    fun applyFrameTime(frameTimeNanos: Long) {
        phase = ShimmerPhase.phase(frameTimeNanos, configuration.durationMillis, configuration.bandWidth)
        invalidateSelf()
    }

    override fun setAlpha(alpha: Int) {
        fillPaint.alpha = alpha
        shimmerPaint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        fillPaint.colorFilter = colorFilter
        shimmerPaint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in the Android framework")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    private fun drawBars(canvas: Canvas, bars: List<Bar>) {
        if (bars.isEmpty()) return
        fillPaint.color = configuration.baseColor.toArgb()
        bars.forEach { canvas.drawRoundRect(it.rect, it.radius, it.radius, fillPaint) }

        val renderingFrame = bars.drop(1).fold(RectF(bars.first().rect)) { result, bar ->
            result.apply { union(bar.rect) }
        }
        shimmerPaint.shader = gradient(renderingFrame) ?: return
        val path = Path()
        bars.forEach { path.addRoundRect(it.rect, it.radius, it.radius, Path.Direction.CW) }
        val checkpoint = canvas.save()
        canvas.clipPath(path)
        canvas.drawRect(renderingFrame, shimmerPaint)
        canvas.restoreToCount(checkpoint)
        shimmerPaint.shader = null
    }

    private fun drawImageMasked(canvas: Canvas, bitmap: Bitmap) {
        val frame = RectF(bounds)
        val source = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        val destination = RectF(source)
        Matrix().apply { setRectToRect(source, frame, Matrix.ScaleToFit.CENTER) }.mapRect(destination)

        val checkpoint = canvas.saveLayer(frame, null)
        fillPaint.color = configuration.baseColor.toArgb()
        canvas.drawRect(destination, fillPaint)
        shimmerPaint.shader = gradient(frame)
        if (shimmerPaint.shader != null) canvas.drawRect(destination, shimmerPaint)
        shimmerPaint.shader = null

        canvas.drawBitmap(bitmap, null, destination, maskPaint)
        canvas.restoreToCount(checkpoint)
    }

    private fun gradient(frame: RectF): LinearGradient? {
        if (configuration.bandWidth <= 0f || frame.isEmpty) return null
        val points = configuration.direction.gradientPoints(phase, configuration.bandWidth)
        val startX = frame.left + frame.width() * points.start.x
        val startY = frame.top + frame.height() * points.start.y
        val endX = frame.left + frame.width() * points.end.x
        val endY = frame.top + frame.height() * points.end.y
        if (startX == endX && startY == endY) return null
        return LinearGradient(
            startX,
            startY,
            endX,
            endY,
            intArrayOf(0x00000000, configuration.highlightColor.toArgb(), 0x00000000),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP,
        )
    }

    private fun bars(): List<Bar> {
        val host = hostReference.get()
        if (host is TextView) {
            textBars(host).takeIf { it.isNotEmpty() }?.let { return it }
        }
        val frame = RectF(bounds)
        val defaultRadius = configuration.cornerRadiusDp * density
        val explicitRadius = (shape as? SkeletonShape.RoundedRect)?.cornerRadiusDp?.times(density)
        val radius = when (shape) {
            is SkeletonShape.RoundedRect -> explicitRadius ?: defaultRadius
            SkeletonShape.Circle, SkeletonShape.Capsule -> min(frame.width(), frame.height()) / 2f
        }
        return listOf(Bar(frame, max(0f, radius)))
    }

    private fun textBars(textView: TextView): List<Bar> {
        val layout = textView.layout ?: return emptyList()
        if (textView.text.isNullOrEmpty() || layout.lineCount == 0) return emptyList()
        val horizontalOffset = textView.totalPaddingLeft.toFloat() - textView.scrollX
        val verticalOffset = textView.extendedPaddingTop.toFloat() - textView.scrollY
        val limit = if (textView.maxLines > 0) min(layout.lineCount, textView.maxLines) else layout.lineCount
        return buildList {
            for (line in 0 until limit) {
                val lineHeight = (layout.getLineBottom(line) - layout.getLineTop(line)).toFloat()
                val barHeight = lineHeight * 0.62f
                val lineLeft = horizontalOffset + layout.getLineLeft(line)
                val lineWidth = min(layout.getLineWidth(line), bounds.width().toFloat() - lineLeft).coerceAtLeast(0f)
                if (lineWidth <= 0f || barHeight <= 0f) continue
                val top = verticalOffset + layout.getLineTop(line) + (lineHeight - barHeight) / 2f
                val rect = RectF(lineLeft, top, lineLeft + lineWidth, top + barHeight)
                val radius = min(configuration.cornerRadiusDp * density, barHeight / 2f)
                add(Bar(rect, radius))
            }
        }
    }
}
