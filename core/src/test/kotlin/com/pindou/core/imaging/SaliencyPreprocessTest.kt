package com.pindou.core.imaging

import com.pindou.core.model.PixelImage
import org.junit.Assert.assertEquals
import org.junit.Test

class SaliencyPreprocessTest {

    private fun assertArrayEquals(expected: FloatArray, actual: FloatArray, delta: Float) {
        assertEquals("size", expected.size, actual.size)
        for (i in expected.indices) {
            assertEquals("index $i", expected[i], actual[i], delta)
        }
    }

    @Test
    fun `model input is channel-major and ImageNet-normalized`() {
        val size = 4
        val image = PixelImage.filled(size, size, 0xFFFF0000.toInt())
        val input = SaliencyPreprocess.toModelInput(image, size)
        val n = size * size
        assertEquals(3 * n, input.size)
        val rExpected = (1f - SaliencyPreprocess.MEAN[0]) / SaliencyPreprocess.STD[0]
        val gExpected = (0f - SaliencyPreprocess.MEAN[1]) / SaliencyPreprocess.STD[1]
        val bExpected = (0f - SaliencyPreprocess.MEAN[2]) / SaliencyPreprocess.STD[2]
        for (i in 0 until n) {
            assertEquals(rExpected, input[i], 1e-3f)
            assertEquals(gExpected, input[n + i], 1e-3f)
            assertEquals(bExpected, input[2 * n + i], 1e-3f)
        }
    }

    @Test
    fun `normalization divides by the image maximum`() {
        val size = 2
        val pixels = intArrayOf(
            0xFF808080.toInt(), 0xFFFFFFFF.toInt(),
            0xFF000000.toInt(), 0xFF808080.toInt(),
        )
        val image = PixelImage(size, size, pixels)
        val input = SaliencyPreprocess.toModelInput(image, size)
        val gray = (0x80 / 255f) / (0xFF / 255f)
        val grayExpected = (gray - SaliencyPreprocess.MEAN[0]) / SaliencyPreprocess.STD[0]
        val whiteExpected = (1f - SaliencyPreprocess.MEAN[0]) / SaliencyPreprocess.STD[0]
        val blackExpected = (0f - SaliencyPreprocess.MEAN[0]) / SaliencyPreprocess.STD[0]
        assertEquals(grayExpected, input[0], 1e-3f)
        assertEquals(whiteExpected, input[1], 1e-3f)
        assertEquals(blackExpected, input[2], 1e-3f)
        assertEquals(grayExpected, input[3], 1e-3f)
    }

    @Test
    fun `working mask is identity at matching resolution`() {
        val saliency = floatArrayOf(0f, 0.25f, 0.75f, 1f)
        val mask = SaliencyPreprocess.toWorkingMask(saliency, side = 2, width = 2, height = 2)
        assertArrayEquals(saliency, mask.values, 1e-5f)
    }

    @Test
    fun `working mask upsamples bilinearly`() {
        val saliency = floatArrayOf(0f, 1f, 0f, 1f)
        val mask = SaliencyPreprocess.toWorkingMask(saliency, side = 2, width = 4, height = 4)
        assertEquals(4 * 4, mask.values.size)
        assertEquals(0f, mask[0, 0], 1e-4f)
        assertEquals(1f, mask[3, 0], 1e-4f)
        assertEquals(1f, mask[3, 3], 1e-4f)
        assertEquals(0.25f, mask[1, 0], 1e-4f)
    }

    @Test
    fun `constant saliency is preserved rather than flattened to zero`() {
        val saliency = FloatArray(4) { 0.8f }
        val mask = SaliencyPreprocess.toWorkingMask(saliency, side = 2, width = 2, height = 2)
        assertArrayEquals(saliency, mask.values, 1e-5f)
    }
}
