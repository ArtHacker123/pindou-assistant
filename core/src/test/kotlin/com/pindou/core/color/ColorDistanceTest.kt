package com.pindou.core.color

import com.pindou.core.model.Rgb
import org.junit.Assert.assertEquals
import org.junit.Test

class ColorDistanceTest {

    @Test
    fun `identical colors have zero distance`() {
        assertEquals(0L, RgbEuclideanDistance.squaredDistance(Rgb(10, 20, 30), Rgb(10, 20, 30)))
    }

    @Test
    fun `squared distance is sum of squared channel deltas`() {
        val d = RgbEuclideanDistance.squaredDistance(Rgb(0, 0, 0), Rgb(3, 4, 0))
        assertEquals(25L, d)
    }

    @Test
    fun `nearest index picks closest candidate`() {
        val candidates = listOf(Rgb(0, 0, 0), Rgb(255, 0, 0), Rgb(0, 255, 0))
        assertEquals(1, NearestColor.indexOf(Rgb(250, 5, 5), candidates))
        assertEquals(2, NearestColor.indexOf(Rgb(5, 240, 5), candidates))
    }

    @Test
    fun `nearest index returns minus one when empty`() {
        assertEquals(-1, NearestColor.indexOf(Rgb(1, 2, 3), emptyList()))
    }
}
