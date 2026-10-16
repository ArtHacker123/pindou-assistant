package com.pindou.core.imaging

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class MaskFiltersTest {

    @Test
    fun `open removes an isolated speck`() {
        // A width-one spike cannot survive an erosion of radius 1, so opening drops it.
        val mask = FloatMask(5, 1, floatArrayOf(1f, 0f, 0f, 0f, 0f))
        val opened = MaskFilters.open(mask, 1)
        assertArrayEquals(floatArrayOf(0f, 0f, 0f, 0f, 0f), opened.values, 1e-6f)
    }

    @Test
    fun `open keeps a solid region`() {
        // A width-three block survives erosion (to width one) and is restored.
        val mask = FloatMask(5, 1, floatArrayOf(1f, 1f, 1f, 0f, 0f))
        val opened = MaskFilters.open(mask, 1)
        assertArrayEquals(floatArrayOf(1f, 1f, 1f, 0f, 0f), opened.values, 1e-6f)
    }

    @Test
    fun `close fills a one-pixel hole`() {
        val mask = FloatMask(5, 1, floatArrayOf(1f, 1f, 0f, 1f, 1f))
        val closed = MaskFilters.close(mask, 1)
        assertArrayEquals(floatArrayOf(1f, 1f, 1f, 1f, 1f), closed.values, 1e-6f)
    }

    @Test
    fun `erode shrinks and dilate grows`() {
        val mask = FloatMask(3, 1, floatArrayOf(1f, 1f, 1f))
        assertArrayEquals(floatArrayOf(1f, 1f, 1f), MaskFilters.erode(mask, 1).values, 1e-6f)
        assertArrayEquals(floatArrayOf(1f, 1f, 1f), MaskFilters.dilate(mask, 1).values, 1e-6f)
    }
}
