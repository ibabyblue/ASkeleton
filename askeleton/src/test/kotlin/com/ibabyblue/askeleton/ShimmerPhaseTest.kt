package com.ibabyblue.askeleton

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

public class ShimmerPhaseTest {
    @Test
    public fun zeroStartsBeforeLeadingEdge() {
        assertEquals(-0.6f, ShimmerPhase.phase(0L, 1_400L, 0.6f), 0.000_001f)
    }

    @Test
    public fun phaseLoopsByDuration() {
        val first = ShimmerPhase.phase(250_000_000L, 1_400L, 0.6f)
        val second = ShimmerPhase.phase(1_650_000_000L, 1_400L, 0.6f)
        assertEquals(first, second, 0.000_001f)
    }

    @Test
    public fun nonPositiveDurationFreezesBeforeLeadingEdge() {
        assertEquals(-0.4f, ShimmerPhase.phase(1_000L, 0L, 0.4f), 0.000_001f)
    }

    @Test
    public fun phaseRemainsInsideOffscreenBounds() {
        repeat(100) { index ->
            val phase = ShimmerPhase.phase(index * 14_000_000L, 1_400L, 0.6f)
            assertTrue(phase >= -0.6f)
            assertTrue(phase <= 1f)
        }
    }
}
