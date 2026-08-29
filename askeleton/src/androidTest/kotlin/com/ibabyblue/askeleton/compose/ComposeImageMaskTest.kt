package com.ibabyblue.askeleton.compose

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ibabyblue.askeleton.SkeletonColor
import com.ibabyblue.askeleton.SkeletonConfiguration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
public class ComposeImageMaskTest {
    @get:Rule
    public val composeRule = createComposeRule()

    @Test
    public fun imageMaskClearsPixelsOutsideCenteredDestination() {
        composeRule.mainClock.autoAdvance = false
        val mask = Bitmap.createBitmap(10, 20, Bitmap.Config.ARGB_8888).apply {
            eraseColor(AndroidColor.WHITE)
        }.asImageBitmap()

        composeRule.setContent {
            Box(modifier = Modifier.background(Color.Black)) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .testTag(MaskTag)
                        .skeleton(
                            active = true,
                            mask = mask,
                            appearance = opaqueRedAppearance,
                        ),
                )
            }
        }
        composeRule.mainClock.advanceTimeByFrame()

        val pixels = composeRule.onNodeWithTag(MaskTag).captureToImage().toPixelMap()

        assertEquals(Color.Black, pixels[0, pixels.height / 2])
        assertTrue(pixels[pixels.width / 2, pixels.height / 2].red > 0.9f)
    }

    private companion object {
        const val MaskTag = "image_mask"
        val opaqueRedAppearance = SkeletonConfiguration(
            baseColor = SkeletonColor(1f, 0f, 0f, 1f),
            highlightColor = SkeletonColor(0f, 0f, 0f, 0f),
            bandWidth = 0f,
        )
    }
}
