package com.pindou.core.color

import com.pindou.core.model.Rgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorSpaceTest {

    @Test
    fun `rgb space distance is sum of squared channel deltas`() {
        assertEquals(25.0, RgbColorSpace.squaredDistance(Rgb(0, 0, 0), Rgb(3, 4, 0)), 0.0)
    }

    @Test
    fun `srgb linear average of black and white is a light midtone`() {
        val mid = Srgb.fromLinear((Srgb.toLinear(0) + Srgb.toLinear(255)) / 2.0)
        assertTrue("linear midtone should be ~188, was $mid", mid in 186..190)
    }

    @Test
    fun `lab encode round trips through decode`() {
        val colors = listOf(
            Rgb(0, 0, 0), Rgb(255, 255, 255), Rgb(128, 128, 128),
            Rgb(255, 0, 0), Rgb(0, 255, 0), Rgb(0, 0, 255),
            Rgb(250, 245, 205), Rgb(20, 90, 140),
        )
        for (c in colors) {
            val back = LabColorSpace.decode(LabColorSpace.encode(c))
            assertTrue("round trip for $c drifted to $back", closeEnough(c, back, tolerance = 1))
        }
    }

    @Test
    fun `lab lightness matches black and white anchors`() {
        assertEquals(0.0, LabColorSpace.encode(Rgb(0, 0, 0))[0], 1.0)
        assertEquals(100.0, LabColorSpace.encode(Rgb(255, 255, 255))[0], 1.0)
    }

    @Test
    fun `lab picks the perceptually closer candidate`() {
        // A near-gray target is far closer to another gray than to a saturated red,
        // even though their raw sRGB channel deltas are comparable.
        val candidates = listOf(Rgb(255, 0, 0), Rgb(130, 130, 130))
        assertEquals(1, LabColorSpace.nearestIndex(Rgb(128, 128, 128), candidates))
    }

    @Test
    fun `nearest index returns minus one when empty`() {
        assertEquals(-1, LabColorSpace.nearestIndex(Rgb(1, 2, 3), emptyList()))
    }

    private fun closeEnough(a: Rgb, b: Rgb, tolerance: Int): Boolean =
        kotlin.math.abs(a.r - b.r) <= tolerance &&
            kotlin.math.abs(a.g - b.g) <= tolerance &&
            kotlin.math.abs(a.b - b.b) <= tolerance
}
