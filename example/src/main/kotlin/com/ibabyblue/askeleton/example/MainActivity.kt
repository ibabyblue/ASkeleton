package com.ibabyblue.askeleton.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import com.ibabyblue.askeleton.ShimmerDirection
import com.ibabyblue.askeleton.SkeletonColor
import com.ibabyblue.askeleton.SkeletonConfiguration
import com.ibabyblue.askeleton.SkeletonShape
import com.ibabyblue.askeleton.compose.SkeletonAppearance
import com.ibabyblue.askeleton.compose.skeleton
import com.ibabyblue.askeleton.compose.skeletonText
import com.ibabyblue.askeleton.view.SkeletonMask
import com.ibabyblue.askeleton.view.skeleton
import kotlin.math.roundToInt

/** Runs the Compose and Android View integration labs without XML layout code. */
public class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SkeletonCatalog()
                }
            }
        }
    }
}

@Composable
private fun SkeletonCatalog() {
    var loading by remember { mutableStateOf(true) }
    var showCompose by remember { mutableStateOf(true) }
    var directionIndex by remember { mutableIntStateOf(0) }
    val direction = ShimmerDirection.entries[directionIndex]
    val appearance = remember(direction) {
        SkeletonConfiguration.Default.copy(direction = direction)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("ASkeleton", style = MaterialTheme.typography.headlineMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { loading = !loading }) {
                Text(if (loading) "Show content" else "Show skeleton")
            }
            Button(onClick = { showCompose = !showCompose }) {
                Text(if (showCompose) "Open Views" else "Open Compose")
            }
        }
        Button(onClick = { directionIndex = (directionIndex + 1) % ShimmerDirection.entries.size }) {
            Text("Direction: ${direction.name}")
        }

        ImageStylesDemo(loading = loading, showCompose = showCompose, direction = direction)

        if (showCompose) {
            SkeletonAppearance(appearance) {
                ComposeLab(loading)
            }
        } else {
            ViewLab(loading = loading, appearance = appearance)
        }
    }
}

@Composable
private fun ComposeLab(loading: Boolean) {
    val mask = remember { createLogoBitmap(160).asImageBitmap() }
    LabCard(title = "Jetpack Compose") {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(androidx.compose.ui.graphics.Color(0xFFE5E5E5), RoundedCornerShape(28.dp))
                    .skeleton(loading, shape = SkeletonShape.Circle),
            )
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Mira Cat",
                    modifier = Modifier
                        .width(160.dp)
                        .skeleton(loading),
                    fontSize = 18.sp,
                )
                Text(
                    text = "A representative multiline biography keeps the loaded footprint stable.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .skeletonText(loading, lineHeight = 20.dp),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            }
        }
        Box(
            modifier = Modifier
                .size(80.dp)
                .skeleton(active = loading, mask = mask),
        )
        Text(
            text = "Local warm appearance",
            modifier = Modifier
                .fillMaxWidth()
                .skeleton(
                    active = loading,
                    appearance = SkeletonConfiguration(
                        baseColor = SkeletonColor(0.96f, 0.86f, 0.86f, 0.9f),
                        highlightColor = SkeletonColor(1f, 0.95f, 0.95f, 0.95f),
                        durationMillis = 1_000L,
                        bandWidth = 0.5f,
                        direction = ShimmerDirection.TopLeftToBottomRight,
                    ),
                ),
        )
    }
}

@Composable
private fun ViewLab(loading: Boolean, appearance: SkeletonConfiguration) {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context -> SkeletonViewLab(context) },
        update = { it.render(loading, appearance) },
    )
}

@Composable
internal fun LabCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

/** Programmatic View lab proving that the same AAR works without Compose rendering. */
private class SkeletonViewLab(context: Context) : LinearLayout(context) {
    private val avatar = View(context)
    private val title = TextView(context)
    private val biography = TextView(context)
    private val logo = ImageView(context)
    private val localOverride = TextView(context)

    init {
        orientation = VERTICAL
        gravity = Gravity.START
        setPadding(16.dp, 16.dp, 16.dp, 16.dp)
        setBackgroundColor(0xFFF2F2F2.toInt())

        addView(TextView(context).apply {
            text = context.getString(R.string.view_lab_title)
            textSize = 18f
            setTextColor(Color.BLACK)
        })
        addView(avatar, LayoutParams(56.dp, 56.dp).apply { topMargin = 12.dp })
        avatar.setBackgroundColor(0xFFE5E5E5.toInt())

        title.text = context.getString(R.string.profile_name)
        title.textSize = 18f
        title.setTextColor(Color.BLACK)
        addView(title, LayoutParams(180.dp, LayoutParams.WRAP_CONTENT).apply { topMargin = 12.dp })

        biography.text = context.getString(R.string.profile_biography)
        biography.textSize = 14f
        biography.setTextColor(Color.DKGRAY)
        biography.maxLines = 3
        addView(biography, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply { topMargin = 8.dp })

        logo.scaleType = ImageView.ScaleType.CENTER_INSIDE
        logo.setImageDrawable(createLogoBitmap(160).toDrawable(resources))
        addView(logo, LayoutParams(80.dp, 80.dp).apply { topMargin = 12.dp })

        localOverride.text = context.getString(R.string.local_override)
        localOverride.textSize = 14f
        localOverride.setTextColor(Color.DKGRAY)
        addView(localOverride, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply { topMargin = 12.dp })
    }

    fun render(loading: Boolean, appearance: SkeletonConfiguration) {
        avatar.skeleton(false)
        title.skeleton(false)
        biography.skeleton(false)
        logo.skeleton(false, SkeletonMask.OwnImage)
        localOverride.skeleton(false)
        if (!loading) return

        avatar.skeleton(true, shape = SkeletonShape.Circle, appearance = appearance)
        title.skeleton(true, appearance = appearance)
        biography.skeleton(true, appearance = appearance)
        logo.skeleton(true, mask = SkeletonMask.OwnImage, appearance = appearance)
        localOverride.skeleton(
            active = true,
            appearance = SkeletonConfiguration(
                baseColor = SkeletonColor(0.96f, 0.86f, 0.86f, 0.9f),
                highlightColor = SkeletonColor(1f, 0.95f, 0.95f, 0.95f),
                direction = ShimmerDirection.TopLeftToBottomRight,
            ),
        )
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).roundToInt()
}

private fun createLogoBitmap(size: Int): Bitmap {
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK }
    val path = Path()
    val center = size / 2f
    val outer = size * 0.46f
    val inner = size * 0.2f
    repeat(10) { index ->
        val angle = -Math.PI / 2 + index * Math.PI / 5
        val radius = if (index % 2 == 0) outer else inner
        val x = center + kotlin.math.cos(angle).toFloat() * radius
        val y = center + kotlin.math.sin(angle).toFloat() * radius
        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    canvas.drawPath(path, paint)
    return bitmap
}
