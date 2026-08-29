package com.ibabyblue.askeleton.view

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ibabyblue.askeleton.SkeletonColor
import com.ibabyblue.askeleton.SkeletonConfiguration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
public class ViewSkeletonTest {
    @Test
    public fun activationIsIdempotentAndRestoresTextColor() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val label = TextView(ApplicationProvider.getApplicationContext()).apply {
                text = "Representative title"
                setTextColor(Color.BLUE)
                layout(0, 0, 240, 48)
            }

            label.skeleton(true)
            label.skeleton(true)
            assertEquals(Color.TRANSPARENT, label.currentTextColor)

            label.skeleton(false)
            label.skeleton(false)
            assertEquals(Color.BLUE, label.currentTextColor)
        }
    }

    @Test
    public fun ownImageOnNonImageViewIsSafeNoOp() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val view = View(ApplicationProvider.getApplicationContext())
            view.skeleton(true, SkeletonMask.OwnImage)
            view.skeleton(false, SkeletonMask.OwnImage)
        }
    }

    @Test
    public fun imageMaskClearsPixelsOutsideCenteredDestination() {
        val mask = Bitmap.createBitmap(10, 20, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.WHITE)
        }
        val output = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888)
        val drawable = SkeletonDrawable(
            host = View(ApplicationProvider.getApplicationContext()),
            configuration = opaqueRedAppearance,
            shape = com.ibabyblue.askeleton.SkeletonShape.RoundedRect(),
            maskBitmap = mask,
        ).apply {
            setBounds(0, 0, output.width, output.height)
        }

        drawable.draw(Canvas(output))

        assertEquals(0, Color.alpha(output.getPixel(0, output.height / 2)))
        assertTrue(Color.alpha(output.getPixel(output.width / 2, output.height / 2)) > 0)
    }

    private companion object {
        val opaqueRedAppearance = SkeletonConfiguration(
            baseColor = SkeletonColor(1f, 0f, 0f, 1f),
            highlightColor = SkeletonColor(0f, 0f, 0f, 0f),
            bandWidth = 0f,
        )
    }
}
