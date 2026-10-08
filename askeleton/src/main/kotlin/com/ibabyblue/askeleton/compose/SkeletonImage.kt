package com.ibabyblue.askeleton.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.constrainHeight
import com.ibabyblue.askeleton.SkeletonImageBaseStyle
import com.ibabyblue.askeleton.SkeletonImageConfiguration
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Draws one aspect-fit image slot, retaining original pixels or a tinted silhouette when inactive.
 * [contentDescription] is null for a decorative image. Only the highlight layer animates.
 */
@Composable
public fun SkeletonImage(
    image: ImageBitmap,
    configuration: SkeletonImageConfiguration,
    active: Boolean,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val animating = active && configuration.durationMillis > 0 && configuration.bandWidth.isFinite()
        && configuration.bandWidth > 0f
    val frameTime = skeletonFrameTime(animating)
    val semantics = if (contentDescription == null) Modifier else Modifier.semantics {
        this.contentDescription = contentDescription
    }
    Layout(content = {}, modifier = modifier.then(semantics).drawWithContent {
        if (size.width <= 0f || size.height <= 0f) return@drawWithContent
        val scale = min(size.width / image.width, size.height / image.height)
        val target = IntSize((image.width * scale).roundToInt(), (image.height * scale).roundToInt())
        val offset = IntOffset(((size.width - target.width) / 2).roundToInt(),
            ((size.height - target.height) / 2).roundToInt())
        val tint = configuration.baseStyle as? SkeletonImageBaseStyle.Tint
        if (tint == null || !animating) {
            drawImage(image, dstOffset = offset, dstSize = target,
                colorFilter = tint?.let { ColorFilter.tint(it.color.composeColor) })
        }
        if (animating) drawImageMaskedSkeleton(image, configuration.fillConfiguration, frameTime.value)
    }) { _, constraints ->
        val ratio = image.width.toFloat() / image.height
        var width = constraints.constrainWidth(image.width)
        var height = constraints.constrainHeight((width / ratio).roundToInt())
        if (constraints.hasFixedHeight && !constraints.hasFixedWidth) {
            width = constraints.constrainWidth((height * ratio).roundToInt())
        } else if (!constraints.hasFixedWidth) {
            width = constraints.constrainWidth((height * ratio).roundToInt())
        }
        height = constraints.constrainHeight((width / ratio).roundToInt())
        layout(width, height) {}
    }
}
