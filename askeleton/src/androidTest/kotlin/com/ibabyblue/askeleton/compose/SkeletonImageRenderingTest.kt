package com.ibabyblue.askeleton.compose

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ibabyblue.askeleton.SkeletonColor
import com.ibabyblue.askeleton.SkeletonConfiguration
import com.ibabyblue.askeleton.SkeletonFillMode
import com.ibabyblue.askeleton.SkeletonImageBaseStyle
import com.ibabyblue.askeleton.SkeletonImageConfiguration
import com.ibabyblue.askeleton.view.SkeletonImageView
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
public class SkeletonImageRenderingTest {
    @get:Rule public val compose = createComposeRule()

    @Test
    public fun originalAndTintModesKeepAspectAndUpdateWhileActive() {
        compose.mainClock.autoAdvance = false
        val image = Bitmap.createBitmap(10, 20, Bitmap.Config.ARGB_8888).apply {
            eraseColor(android.graphics.Color.BLUE)
        }.asImageBitmap()
        val style = mutableStateOf<SkeletonImageBaseStyle>(SkeletonImageBaseStyle.Original)
        val active = mutableStateOf(true)
        compose.setContent {
            Box(Modifier.background(Color.Black)) {
                SkeletonImage(image, SkeletonImageConfiguration(style.value, SkeletonColor(1f, 1f, 1f, 0f)),
                    active.value, Modifier.width(40.dp).testTag("image"))
            }
        }
        compose.mainClock.advanceTimeByFrame()
        fun center(): Color {
            val pixels = compose.onNodeWithTag("image").captureToImage().toPixelMap()
            assertEquals(pixels.width * 2, pixels.height)
            return pixels[pixels.width / 2, pixels.height / 2]
        }
        assertEquals(Color.Blue, center())
        compose.runOnIdle { style.value = SkeletonImageBaseStyle.Tint(SkeletonColor(1f, 0f, 0f, 0.2f)) }
        compose.mainClock.advanceTimeByFrame()
        // A transparent highlight does not provide a stable tint throughout a gradient sweep;
        // stop animation to verify the exact static appearance restoration.
        compose.runOnIdle { active.value = false }
        compose.mainClock.advanceTimeByFrame()
        assertEquals(0.2f, center().red, 0.02f)
    }

    @Test
    public fun singleGradientPeakAlphaMatchesViewRenderer() {
        val mask = Bitmap.createBitmap(100, 40, Bitmap.Config.ARGB_8888).apply {
            eraseColor(android.graphics.Color.WHITE)
        }.asImageBitmap()
        val config = SkeletonConfiguration(SkeletonColor(0.8f, 0.8f, 0.8f, 0.13f),
            SkeletonColor(1f, 1f, 1f, 0.46f), durationMillis = 1_000, bandWidth = 1f,
            fillMode = SkeletonFillMode.Gradient)
        compose.setContent {
            Column(Modifier.background(Color.Black)) {
                Box(Modifier.size(100.dp, 40.dp).testTag("gradient").drawWithContent {
                    drawImageMaskedSkeleton(mask, config, 500_000_000L)
                })
                Box(Modifier.size(100.dp, 40.dp).testTag("overlay").drawWithContent {
                    drawImageMaskedSkeleton(mask, config.copy(fillMode = SkeletonFillMode.Overlay), 500_000_000L)
                })
            }
        }
        fun red(tag: String): Float {
            val pixels = compose.onNodeWithTag(tag).captureToImage().toPixelMap()
            return pixels[pixels.width / 2, pixels.height / 2].red
        }
        assertEquals(0.46f, red("gradient"), 0.02f)
        assertEquals(0.516f, red("overlay"), 0.02f)
    }

    @Test
    public fun contentOverlayRetainsPartialAlphaInsteadOfAnOpaqueShapeMask() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            Box(Modifier.background(Color.Black)) {
                Box(Modifier.size(80.dp).testTag("shape")
                    .skeletonOverlay(true, SkeletonColor(1f, 1f, 1f), durationMillis = 1_000, bandWidth = 10f)
                    .background(Color.Red.copy(alpha = 0.4f)))
            }
        }
        compose.mainClock.advanceTimeBy(500)
        val pixels = compose.onNodeWithTag("shape").captureToImage().toPixelMap()
        val color = pixels[pixels.width / 2, pixels.height / 2]
        org.junit.Assert.assertTrue(color.red in 0.5f..0.66f)
        org.junit.Assert.assertTrue(color.green in 0.15f..0.42f)
    }

    @Test
    public fun nativeImageSlotAnimatesAndRestoresTintOnDisable() {
        compose.mainClock.autoAdvance = false
        val bitmap = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888).apply {
            eraseColor(android.graphics.Color.BLUE)
        }
        var slot: SkeletonImageView? = null
        compose.setContent {
            Box(Modifier.background(Color.Black)) {
                AndroidView(factory = { context ->
                    SkeletonImageView(context).also {
                        slot = it
                        it.image = bitmap
                        it.configuration = SkeletonImageConfiguration(
                            SkeletonImageBaseStyle.Tint(SkeletonColor(1f, 0f, 0f, 0.2f)),
                            SkeletonColor(1f, 1f, 1f, 0.8f))
                        it.isActive = true
                    }
                }, modifier = Modifier.size(40.dp).testTag("native"))
            }
        }
        compose.mainClock.advanceTimeByFrame()
        compose.runOnIdle { slot!!.isActive = false }
        val pixels = compose.onNodeWithTag("native").captureToImage().toPixelMap()
        assertEquals(0.2f, pixels[pixels.width / 2, pixels.height / 2].red, 0.02f)
        assertEquals(0f, pixels[pixels.width / 2, pixels.height / 2].blue, 0.02f)
    }
}
