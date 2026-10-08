package com.ibabyblue.askeleton.example

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.createBitmap
import com.ibabyblue.askeleton.ShimmerDirection
import com.ibabyblue.askeleton.SkeletonColor
import com.ibabyblue.askeleton.SkeletonConfiguration
import com.ibabyblue.askeleton.SkeletonFillMode
import com.ibabyblue.askeleton.SkeletonImageBaseStyle
import com.ibabyblue.askeleton.SkeletonImageConfiguration
import com.ibabyblue.askeleton.SkeletonShape
import com.ibabyblue.askeleton.compose.SkeletonImage
import com.ibabyblue.askeleton.compose.skeleton
import com.ibabyblue.askeleton.compose.skeletonOverlay
import com.ibabyblue.askeleton.view.SkeletonImageView
import com.ibabyblue.askeleton.view.SkeletonMask
import com.ibabyblue.askeleton.view.skeleton

private val DemoTint = SkeletonColor(0.1f, 0.35f, 1f, 0.25f)
private val DemoHighlight = SkeletonColor(1f, 1f, 1f, 0.6f)
private val PreviewBackground = Color(0xFF202634)

/** Shared controls make the same public image behavior easy to compare between UI toolkits. */
@Composable
internal fun ImageStylesDemo(loading: Boolean, showCompose: Boolean, direction: ShimmerDirection) {
    var portrait by remember { mutableStateOf(false) }
    var tinted by remember { mutableStateOf(false) }
    var staticImage by remember { mutableStateOf(false) }
    val landscapeImage = remember { createDemoImage(240, 120) }
    val portraitImage = remember { createDemoImage(120, 180) }
    val bitmap = if (portrait) portraitImage else landscapeImage
    val original = SkeletonImageConfiguration(
        baseStyle = SkeletonImageBaseStyle.Original,
        highlightColor = DemoHighlight,
        durationMillis = if (staticImage) 0L else 1_400L,
        direction = direction,
    )
    val tint = original.copy(baseStyle = SkeletonImageBaseStyle.Tint(DemoTint))

    LabCard(title = "Image styles · ${if (showCompose) "Compose" else "Views"}") {
        Text("Use the loading and direction controls above for every animated sample.")
        Button(onClick = { portrait = !portrait }) {
            Text("Image: ${if (portrait) "portrait 2:3" else "landscape 2:1"} · Swap")
        }
        Button(onClick = { staticImage = !staticImage }) {
            Text(if (staticImage) "Images: static (duration = 0)" else "Images: animated · Use duration 0")
        }
        Text("Fixed 96 × 96 slots · images fit without cropping or stretching.")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Original")
                ImageSample(bitmap, original, loading, showCompose, Modifier.size(96.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tint · 25% alpha")
                ImageSample(bitmap, tint, loading, showCompose, Modifier.size(96.dp))
            }
        }
        Text("Show content stops the sweep and keeps the original pixels or tinted silhouette.")

        Text("Live updates · width only", style = MaterialTheme.typography.titleSmall)
        Button(onClick = { tinted = !tinted }) {
            Text("Live style: ${if (tinted) "Tint" else "Original"} · Switch")
        }
        Text("Width 96 dp; height follows the image (48 / 144 dp). Swap image or style while loading.")
        ImageSample(bitmap, if (tinted) tint else original, loading, showCompose, Modifier.width(96.dp))

        Text("Color compositing", style = MaterialTheme.typography.titleSmall)
        Text("Same base (25% alpha) and highlight (60% alpha). Overlay stacks them; Gradient uses one fill.")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SkeletonFillMode.entries.forEach { mode ->
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(mode.name)
                    FillSample(bitmap, mode, loading, showCompose, direction)
                }
            }
        }

        if (showCompose) {
            Text("Content overlay · Compose", style = MaterialTheme.typography.titleSmall)
            Text("The blue shape stays visible. Its 50% opacity also masks the highlight.")
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf(false, true).forEach { overlay ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (overlay) "With overlay" else "Content only")
                        Box(Modifier.background(PreviewBackground).padding(8.dp)) {
                            Box(
                                Modifier.size(64.dp)
                                    .skeletonOverlay(loading && overlay, DemoHighlight, direction = direction)
                                    .background(Color.Blue.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                            )
                        }
                    }
                }
            }
        } else {
            Text("Open Compose to try the content-alpha overlay modifier.")
        }
    }
}

@Composable
private fun ImageSample(
    bitmap: Bitmap,
    configuration: SkeletonImageConfiguration,
    active: Boolean,
    showCompose: Boolean,
    modifier: Modifier,
) {
    val preview = modifier.background(PreviewBackground).border(1.dp, Color.Gray)
    if (showCompose) {
        SkeletonImage(
            image = remember(bitmap) { bitmap.asImageBitmap() },
            configuration = configuration,
            active = active,
            modifier = preview,
            contentDescription = "Colored sample with opaque and translucent shapes",
        )
    } else {
        AndroidView(
            factory = { context ->
                SkeletonImageView(context).apply {
                    contentDescription = "Colored sample with opaque and translucent shapes"
                }
            },
            modifier = preview,
            update = {
                // These setters update an active component; no deactivate/reactivate sequence is needed.
                it.image = bitmap
                it.configuration = configuration
                it.isActive = active
            },
        )
    }
}

@Composable
private fun FillSample(
    bitmap: Bitmap,
    mode: SkeletonFillMode,
    active: Boolean,
    showCompose: Boolean,
    direction: ShimmerDirection,
) {
    val appearance = SkeletonConfiguration(
        baseColor = DemoTint,
        highlightColor = DemoHighlight,
        direction = direction,
        fillMode = mode,
    )
    Column(
        modifier = Modifier.fillMaxWidth().background(PreviewBackground).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val bar = Modifier.fillMaxWidth().height(24.dp)
        val image = Modifier.fillMaxWidth().height(72.dp)
        if (showCompose) {
            Box(bar.skeleton(active, shape = SkeletonShape.Capsule, appearance = appearance)
                .background(Color(0xFF427EEA), RoundedCornerShape(12.dp)))
            val mask = remember(bitmap) { bitmap.asImageBitmap() }
            // Supplies representative content so Show content also works for the old mask API.
            SkeletonImage(
                mask,
                SkeletonImageConfiguration(SkeletonImageBaseStyle.Original, DemoHighlight),
                active = false,
                modifier = image.skeleton(active, mask, appearance),
            )
        } else {
            AndroidView(
                factory = { context ->
                    View(context).apply {
                        background = GradientDrawable().apply {
                            setColor(0xFF427EEA.toInt())
                            cornerRadius = 12f * resources.displayMetrics.density
                        }
                    }
                },
                modifier = bar,
                update = {
                    it.skeleton(false)
                    it.background.alpha = if (active) 0 else 255
                    it.skeleton(active, shape = SkeletonShape.Capsule, appearance = appearance)
                },
            )
            AndroidView(
                factory = { context -> ImageView(context).apply { scaleType = ImageView.ScaleType.FIT_CENTER } },
                modifier = image,
                update = {
                    // The existing View modifier snapshots image/configuration at activation.
                    it.skeleton(false, SkeletonMask.Image(bitmap))
                    it.setImageBitmap(if (active) null else bitmap)
                    it.skeleton(active, SkeletonMask.Image(bitmap), appearance)
                },
            )
        }
    }
}

/** Transparent margins and a translucent circle make both RGB preservation and alpha masking visible. */
private fun createDemoImage(width: Int, height: Int): Bitmap = createBitmap(width, height).also { bitmap ->
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = 0xFFFFB547.toInt()
    canvas.drawRoundRect(width * 0.08f, height * 0.12f, width * 0.55f, height * 0.88f, 12f, 12f, paint)
    paint.color = 0xFF43C9BE.toInt()
    canvas.drawCircle(width * 0.7f, height * 0.38f, minOf(width, height) * 0.2f, paint)
    paint.color = 0x804F83FF.toInt()
    canvas.drawCircle(width * 0.72f, height * 0.75f, minOf(width, height) * 0.18f, paint)
}
