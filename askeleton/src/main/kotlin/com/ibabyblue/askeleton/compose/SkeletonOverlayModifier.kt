package com.ibabyblue.askeleton.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Paint
import com.ibabyblue.askeleton.ShimmerDirection
import com.ibabyblue.askeleton.ShimmerPhase
import com.ibabyblue.askeleton.SkeletonColor
import com.ibabyblue.askeleton.SkeletonConfiguration

/**
 * Keeps content visible and clips a highlight with its rendered alpha, including partial opacity.
 * Place before drawing modifiers to include them in the mask, e.g. `skeletonOverlay(...).background(...)`.
 * The overlay does not change measurement, semantics, or input handling.
 */
@Composable
public fun Modifier.skeletonOverlay(
    active: Boolean,
    highlightColor: SkeletonColor,
    durationMillis: Long = 1_400L,
    bandWidth: Float = 0.6f,
    direction: ShimmerDirection = ShimmerDirection.LeftToRight,
): Modifier {
    val animating = active && durationMillis > 0 && bandWidth.isFinite() && bandWidth > 0f
    val frame = skeletonFrameTime(animating)
    val configuration = SkeletonConfiguration(SkeletonColor(0f, 0f, 0f, 0f), highlightColor,
        durationMillis = durationMillis, bandWidth = bandWidth, direction = direction)
    return drawWithContent {
        drawContent()
        if (animating) {
            val phase = ShimmerPhase.phase(frame.value, durationMillis, bandWidth)
            shimmerBrush(configuration, phase, size)?.let { brush ->
                drawContext.canvas.saveLayer(Rect(Offset.Zero, size), Paint())
                drawContent()
                drawRect(brush = brush, blendMode = BlendMode.SrcIn)
                drawContext.canvas.restore()
            }
        }
    }
}
