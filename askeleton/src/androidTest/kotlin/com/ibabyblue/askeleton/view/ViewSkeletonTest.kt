package com.ibabyblue.askeleton.view

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
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
}
