package com.ibabyblue.askeleton.view

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ibabyblue.askeleton.SkeletonColor
import com.ibabyblue.askeleton.SkeletonConfiguration
import com.ibabyblue.askeleton.SkeletonFillMode
import com.ibabyblue.askeleton.SkeletonImageBaseStyle
import com.ibabyblue.askeleton.SkeletonImageConfiguration
import com.ibabyblue.askeleton.SkeletonShape
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
public class ImageRenderingTest {
    @Test
    public fun gradientRetainsPeakAlphaAndAppliesImageAlphaOnce() {
        val host = View(ApplicationProvider.getApplicationContext())
        val config = SkeletonConfiguration(SkeletonColor(0.8f, 0.8f, 0.8f, 0.13f),
            SkeletonColor(1f, 1f, 1f, 0.46f), durationMillis = 1_000, bandWidth = 1f,
            fillMode = SkeletonFillMode.Gradient)
        val mask = Bitmap.createBitmap(100, 40, Bitmap.Config.ARGB_8888).apply { eraseColor(0x80FFFFFF.toInt()) }
        fun alpha(configuration: SkeletonConfiguration, bitmap: Bitmap? = null): Int {
            val drawable = SkeletonDrawable(host, configuration, SkeletonShape.RoundedRect(0f), bitmap)
            drawable.setBounds(0, 0, 100, 40)
            drawable.applyFrameTime(500_000_000L)
            val output = Bitmap.createBitmap(100, 40, Bitmap.Config.ARGB_8888)
            drawable.draw(Canvas(output))
            return Color.alpha(output.getPixel(50, 20))
        }
        assertEquals(117.0, alpha(config).toDouble(), 3.0)
        assertEquals(135.0, alpha(config.copy(fillMode = SkeletonFillMode.Overlay)).toDouble(), 3.0)
        assertEquals(59.0, alpha(config, mask).toDouble(), 3.0)
    }

    @Test
    public fun imageViewPreservesOriginalAndStaticTintWithAspectFit() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val image = Bitmap.createBitmap(10, 20, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.BLUE) }
            val view = SkeletonImageView(ApplicationProvider.getApplicationContext()).apply {
                this.image = image
                layout(0, 0, 40, 40)
            }
            fun pixel(x: Int): Int {
                val output = Bitmap.createBitmap(40, 40, Bitmap.Config.ARGB_8888)
                view.draw(Canvas(output))
                return output.getPixel(x, 20)
            }
            assertEquals(Color.BLUE, pixel(20))
            assertEquals(0, Color.alpha(pixel(0)))
            view.configuration = SkeletonImageConfiguration(SkeletonImageBaseStyle.Tint(SkeletonColor(1f, 0f, 0f, 0.2f)),
                SkeletonColor(1f, 1f, 1f))
            assertEquals(51.0, Color.alpha(pixel(20)).toDouble(), 1.0)
            assertEquals(255, Color.red(pixel(20)))
            view.image = null
            assertEquals(0, Color.alpha(pixel(20)))
        }
    }
}
