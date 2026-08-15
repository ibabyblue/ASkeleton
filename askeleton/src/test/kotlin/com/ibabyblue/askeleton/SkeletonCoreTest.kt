package com.ibabyblue.askeleton

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

public class SkeletonCoreTest {
    @Test
    public fun defaultConfigurationMatchesDocumentedContract() {
        val configuration = SkeletonConfiguration.Default
        assertEquals(1_400L, configuration.durationMillis)
        assertEquals(0.6f, configuration.bandWidth, 0f)
        assertEquals(5f, configuration.cornerRadiusDp, 0f)
        assertEquals(ShimmerDirection.LeftToRight, configuration.direction)
        assertEquals(0.8f, configuration.baseColor.alpha, 0f)
    }

    @Test
    public fun shapeRadiiUseExpectedRules() {
        assertEquals(5f, SkeletonShape.RoundedRect().cornerRadius(200f, 20f, 5f), 0f)
        assertEquals(8f, SkeletonShape.RoundedRect(8f).cornerRadius(200f, 20f, 5f), 0f)
        assertEquals(10f, SkeletonShape.Circle.cornerRadius(200f, 20f, 5f), 0f)
        assertEquals(10f, SkeletonShape.Capsule.cornerRadius(200f, 20f, 5f), 0f)
    }

    @Test
    public fun everyDirectionUsesUnitSquareEndpoints() {
        ShimmerDirection.entries.forEach { direction ->
            listOf(direction.start, direction.end).forEach { point ->
                assertTrue(point.x in 0f..1f)
                assertTrue(point.y in 0f..1f)
            }
        }
    }

    @Test
    public fun diagonalGradientInterpolatesAlongItsAxis() {
        val points = ShimmerDirection.TopRightToBottomLeft.gradientPoints(0f, 0.6f)
        assertEquals(NormalizedPoint(1f, 0f), points.start)
        assertEquals(0.4f, points.end.x, 0.000_001f)
        assertEquals(0.6f, points.end.y, 0.000_001f)
    }

    @Test
    public fun lineMetricsRoundAndNeverReturnZeroLines() {
        assertEquals(3, SkeletonLineMetrics.lineCount(69f, 20f))
        assertEquals(4, SkeletonLineMetrics.lineCount(71f, 20f))
        assertEquals(1, SkeletonLineMetrics.lineCount(0f, 20f))
        assertEquals(14f, SkeletonLineMetrics.barHeight(20f), 0f)
        assertEquals(0f, SkeletonLineMetrics.barHeight(-10f), 0f)
    }
}
