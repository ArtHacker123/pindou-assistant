package com.pindou.core.sampler

import com.pindou.core.model.PixelImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UniformSamplerTest {

    private val sampler = UniformSampler()

    @Test
    fun `bounds the sample count`() {
        val image = PixelImage.filled(200, 200, 0xFF00FF00.toInt())
        val samples = sampler.sample(image, maxSamples = 500)
        assertTrue("expected a bounded count, got ${samples.size}", samples.size <= 1000)
        assertTrue(samples.isNotEmpty())
    }

    @Test
    fun `small image returns every pixel`() {
        val image = PixelImage.filled(4, 4, 0xFF112233.toInt())
        val samples = sampler.sample(image, maxSamples = 1000)
        assertEquals(16, samples.size)
    }

    @Test
    fun `skips fully transparent pixels`() {
        val pixels = intArrayOf(0x00FFFFFF.toInt(), 0xFF000000.toInt())
        val image = PixelImage(2, 1, pixels)
        val samples = sampler.sample(image, maxSamples = 100)
        assertEquals(1, samples.size)
    }
}
