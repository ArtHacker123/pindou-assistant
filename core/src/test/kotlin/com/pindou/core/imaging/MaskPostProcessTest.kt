package com.pindou.core.imaging

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class MaskPostProcessTest {

    @Test
    fun `keeps only the largest connected region`() {
        // A single-pixel region at x=0 and a three-pixel region at x=2..4.
        val mask = FloatMask(5, 1, floatArrayOf(1f, 0f, 1f, 1f, 1f))
        val kept = MaskPostProcess.keepLargestComponent(mask)
        assertArrayEquals(floatArrayOf(0f, 0f, 1f, 1f, 1f), kept.values, 1e-6f)
    }

    @Test
    fun `values below the threshold are dropped`() {
        val mask = FloatMask(3, 1, floatArrayOf(0.4f, 0.9f, 0.4f))
        val kept = MaskPostProcess.keepLargestComponent(mask)
        assertArrayEquals(floatArrayOf(0f, 0.9f, 0f), kept.values, 1e-6f)
    }

    @Test
    fun `a single full region is preserved`() {
        val mask = FloatMask.filled(2, 2, 0.8f)
        val kept = MaskPostProcess.keepLargestComponent(mask)
        assertArrayEquals(mask.values, kept.values, 1e-6f)
    }
}
