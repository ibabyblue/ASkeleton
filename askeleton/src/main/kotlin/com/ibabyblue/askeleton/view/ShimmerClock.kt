package com.ibabyblue.askeleton.view

import android.view.Choreographer
import java.util.Collections
import java.util.WeakHashMap

/** Drives all attached View skeletons from one choreographer timestamp. */
internal object ShimmerClock {
    private val drawables = Collections.newSetFromMap(WeakHashMap<SkeletonDrawable, Boolean>())
    private var frameScheduled = false

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            frameScheduled = false
            val live = drawables.toList()
            live.forEach { it.applyFrameTime(frameTimeNanos) }
            if (drawables.isNotEmpty()) scheduleFrame()
        }
    }

    fun register(drawable: SkeletonDrawable) {
        drawables += drawable
        scheduleFrame()
    }

    fun unregister(drawable: SkeletonDrawable) {
        drawables -= drawable
        if (drawables.isEmpty() && frameScheduled) {
            Choreographer.getInstance().removeFrameCallback(frameCallback)
            frameScheduled = false
        }
    }

    private fun scheduleFrame() {
        if (frameScheduled || drawables.isEmpty()) return
        frameScheduled = true
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }
}
