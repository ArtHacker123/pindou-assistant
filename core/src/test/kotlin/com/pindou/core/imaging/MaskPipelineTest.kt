package com.pindou.core.imaging

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class MaskPipelineTest {

    @Test
    fun `defaults equal keepLargestComponent`() {
        val mask = FloatMask(5, 1, floatArrayOf(1f, 0f, 1f, 1f, 1f))
        assertArrayEquals(
            MaskPostProcess.keepLargestComponent(mask).values,
            MaskPostProcess.process(mask, MaskOptions()).values,
            1e-6f,
        )
    }

    @Test
    fun `all subject mode keeps every region`() {
        val mask = FloatMask(5, 1, floatArrayOf(1f, 0f, 1f, 1f, 1f))
        val kept = MaskPostProcess.process(mask, MaskOptions(subjectMode = SubjectMode.ALL))
        assertArrayEquals(floatArrayOf(1f, 0f, 1f, 1f, 1f), kept.values, 1e-6f)
    }

    @Test
    fun `eight connectivity merges a diagonal region`() {
        val mask = FloatMask(2, 2, floatArrayOf(1f, 0f, 0f, 1f))
        val four = MaskPostProcess.process(mask, MaskOptions(connectivity = MaskConnectivity.FOUR))
        assertArrayEquals(floatArrayOf(1f, 0f, 0f, 0f), four.values, 1e-6f)
        val eight = MaskPostProcess.process(mask, MaskOptions(connectivity = MaskConnectivity.EIGHT))
        assertArrayEquals(floatArrayOf(1f, 0f, 0f, 1f), eight.values, 1e-6f)
    }

    @Test
    fun `opening removes a background speck`() {
        val mask = FloatMask(9, 1, floatArrayOf(1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 1f))
        val kept = MaskPostProcess.process(mask, MaskOptions(openRadius = 1))
        assertArrayEquals(floatArrayOf(1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f), kept.values, 1e-6f)
    }

    @Test
    fun `closing fills a subject hole`() {
        val mask = FloatMask(5, 1, floatArrayOf(1f, 1f, 0f, 1f, 1f))
        val kept = MaskPostProcess.process(mask, MaskOptions(closeRadius = 1))
        assertArrayEquals(floatArrayOf(1f, 1f, 1f, 1f, 1f), kept.values, 1e-6f)
    }

    @Test
    fun `higher foreground threshold drops weak pixels`() {
        val mask = FloatMask(3, 1, floatArrayOf(0.4f, 0.6f, 0.9f))
        val kept = MaskPostProcess.process(mask, MaskOptions(foregroundThreshold = 0.5f))
        assertArrayEquals(floatArrayOf(0f, 0.6f, 0.9f), kept.values, 1e-6f)
    }
}
