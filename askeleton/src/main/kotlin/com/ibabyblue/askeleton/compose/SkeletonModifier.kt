package com.ibabyblue.askeleton.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.ibabyblue.askeleton.ShimmerPhase
import com.ibabyblue.askeleton.SkeletonColor
import com.ibabyblue.askeleton.SkeletonConfiguration
import com.ibabyblue.askeleton.SkeletonLineMetrics
import com.ibabyblue.askeleton.SkeletonShape
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Replaces visible Compose content with an in-place geometric skeleton while [active]. */
@Composable
public fun Modifier.skeleton(
    active: Boolean,
    shape: SkeletonShape = SkeletonShape.RoundedRect(),
    appearance: SkeletonConfiguration? = null,
): Modifier {
    val configuration = appearance ?: LocalSkeletonAppearance.current
    val frameTimeNanos = skeletonFrameTime(active)
    return drawWithContent {
        if (!active) {
            drawContent()
        } else {
            drawGeometricSkeleton(shape, configuration, frameTimeNanos)
        }
    }
}

/**
 * Replaces visible Compose content with top-aligned multiline bars.
 *
 * [lineHeight] must match the representative text's resolved line height. The final bar uses
 * [lastLineWidthFraction] when the footprint contains more than one line.
 */
@Composable
public fun Modifier.skeletonText(
    active: Boolean,
    lineHeight: Dp,
    lastLineWidthFraction: Float = 0.6f,
    appearance: SkeletonConfiguration? = null,
): Modifier {
    val configuration = appearance ?: LocalSkeletonAppearance.current
    val frameTimeNanos = skeletonFrameTime(active)
    return drawWithContent {
        if (!active) {
            drawContent()
        } else {
            drawTextSkeleton(lineHeight, lastLineWidthFraction, configuration, frameTimeNanos)
        }
    }
}

/** Replaces visible Compose content with a shimmer clipped by [mask]'s alpha channel. */
@Composable
public fun Modifier.skeleton(
    active: Boolean,
    mask: ImageBitmap,
    appearance: SkeletonConfiguration? = null,
): Modifier {
    val configuration = appearance ?: LocalSkeletonAppearance.current
    val frameTimeNanos = skeletonFrameTime(active)
    return drawWithContent {
        if (!active) {
            drawContent()
        } else {
            drawImageMaskedSkeleton(mask, configuration, frameTimeNanos)
        }
    }
}

@Composable
private fun skeletonFrameTime(active: Boolean): Long {
    val frameTime by produceState(initialValue = 0L, key1 = active) {
        if (active) {
            while (true) value = withFrameNanos { it }
        }
    }
    return frameTime
}

private fun DrawScope.drawGeometricSkeleton(
    shape: SkeletonShape,
    configuration: SkeletonConfiguration,
    frameTimeNanos: Long,
) {
    val phase = ShimmerPhase.phase(frameTimeNanos, configuration.durationMillis, configuration.bandWidth)
    val base = configuration.baseColor.composeColor
    val brush = shimmerBrush(configuration, phase, size)
    when (shape) {
        SkeletonShape.Circle -> {
            val radius = min(size.width, size.height) / 2f
            drawCircle(color = base, radius = radius)
            if (brush != null) drawCircle(brush = brush, radius = radius)
        }
        SkeletonShape.Capsule -> {
            val radius = min(size.width, size.height) / 2f
            drawRoundRect(color = base, cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius))
            if (brush != null) drawRoundRect(brush = brush, cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius))
        }
        is SkeletonShape.RoundedRect -> {
            val radius = (shape.cornerRadiusDp ?: configuration.cornerRadiusDp) * density
            val corner = androidx.compose.ui.geometry.CornerRadius(max(0f, radius))
            drawRoundRect(color = base, cornerRadius = corner)
            if (brush != null) drawRoundRect(brush = brush, cornerRadius = corner)
        }
    }
}

private fun DrawScope.drawTextSkeleton(
    lineHeight: Dp,
    lastLineWidthFraction: Float,
    configuration: SkeletonConfiguration,
    frameTimeNanos: Long,
) {
    val lineHeightPx = lineHeight.toPx()
    val lineCount = SkeletonLineMetrics.lineCount(size.height, lineHeightPx)
    val barHeight = SkeletonLineMetrics.barHeight(lineHeightPx)
    val radius = min(configuration.cornerRadiusDp * density, barHeight / 2f)
    val phase = ShimmerPhase.phase(frameTimeNanos, configuration.durationMillis, configuration.bandWidth)
    for (line in 0 until lineCount) {
        val fraction = if (lineCount > 1 && line == lineCount - 1) {
            lastLineWidthFraction.coerceIn(0f, 1f)
        } else {
            1f
        }
        val top = line * lineHeightPx + (lineHeightPx - barHeight) / 2f
        val barSize = Size(size.width * fraction, barHeight)
        val offset = Offset(0f, top)
        drawRoundRect(
            color = configuration.baseColor.composeColor,
            topLeft = offset,
            size = barSize,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius),
        )
        shimmerBrush(configuration, phase, barSize, offset)?.let { brush ->
            drawRoundRect(
                brush = brush,
                topLeft = offset,
                size = barSize,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius),
            )
        }
    }
}

private fun DrawScope.drawImageMaskedSkeleton(
    mask: ImageBitmap,
    configuration: SkeletonConfiguration,
    frameTimeNanos: Long,
) {
    val scale = min(size.width / mask.width, size.height / mask.height)
    val destinationWidth = (mask.width * scale).roundToInt()
    val destinationHeight = (mask.height * scale).roundToInt()
    val destinationOffset = IntOffset(
        x = ((size.width - destinationWidth) / 2f).roundToInt(),
        y = ((size.height - destinationHeight) / 2f).roundToInt(),
    )
    val destinationTopLeft = Offset(destinationOffset.x.toFloat(), destinationOffset.y.toFloat())
    val destinationSize = Size(destinationWidth.toFloat(), destinationHeight.toFloat())

    drawContext.canvas.saveLayer(Rect(Offset.Zero, size), Paint())
    drawRect(
        color = configuration.baseColor.composeColor,
        topLeft = destinationTopLeft,
        size = destinationSize,
    )
    val phase = ShimmerPhase.phase(frameTimeNanos, configuration.durationMillis, configuration.bandWidth)
    shimmerBrush(configuration, phase, size)?.let { brush ->
        drawRect(brush = brush, topLeft = destinationTopLeft, size = destinationSize)
    }
    drawImage(
        image = mask,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(mask.width, mask.height),
        dstOffset = destinationOffset,
        dstSize = IntSize(destinationWidth, destinationHeight),
        blendMode = BlendMode.DstIn,
    )
    drawContext.canvas.restore()
}

private fun DrawScope.shimmerBrush(
    configuration: SkeletonConfiguration,
    phase: Float,
    drawingSize: Size,
    origin: Offset = Offset.Zero,
): Brush? {
    if (configuration.bandWidth <= 0f || drawingSize.width <= 0f || drawingSize.height <= 0f) return null
    val points = configuration.direction.gradientPoints(phase, configuration.bandWidth)
    val start = origin + Offset(drawingSize.width * points.start.x, drawingSize.height * points.start.y)
    val end = origin + Offset(drawingSize.width * points.end.x, drawingSize.height * points.end.y)
    if (start == end) return null
    return Brush.linearGradient(
        colors = listOf(Color.Transparent, configuration.highlightColor.composeColor, Color.Transparent),
        start = start,
        end = end,
    )
}

private val SkeletonColor.composeColor: Color
    get() = Color(red = red, green = green, blue = blue, alpha = alpha)
