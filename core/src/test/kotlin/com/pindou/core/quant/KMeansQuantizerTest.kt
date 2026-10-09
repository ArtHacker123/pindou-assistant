package com.pindou.core.quant

import com.pindou.core.model.Rgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KMeansQuantizerTest {

    private val quantizer = KMeansQuantizer()

    @Test
    fun `returns at most k centers`() {
        val samples = List(100) { Rgb(it, it, it) }
        val centers = quantizer.quantize(samples, k = 4, config = QuantConfig(seed = 42))
        assertEquals(4, centers.size)
    }

    @Test
    fun `clamps k to sample count`() {
        val samples = listOf(Rgb(1, 1, 1), Rgb(2, 2, 2))
        val centers = quantizer.quantize(samples, k = 10, config = QuantConfig(seed = 1))
        assertEquals(2, centers.size)
    }

    @Test
    fun `separates two well spaced clusters`() {
        val samples = ArrayList<Rgb>()
        repeat(50) { samples.add(Rgb(10, 10, 10)) }
        repeat(50) { samples.add(Rgb(240, 240, 240)) }
        val centers = quantizer.quantize(samples, k = 2, config = QuantConfig(seed = 7))
        val sorted = centers.sortedBy { it.r }
        assertTrue("dark cluster center should be near 10", kotlin.math.abs(sorted[0].r - 10) <= 5)
        assertTrue("light cluster center should be near 240", kotlin.math.abs(sorted[1].r - 240) <= 5)
    }

    @Test
    fun `is deterministic for a fixed seed`() {
        val samples = List(200) { Rgb(it * 37 % 256, it * 53 % 256, it * 71 % 256) }
        val a = quantizer.quantize(samples, k = 8, config = QuantConfig(seed = 123))
        val b = quantizer.quantize(samples, k = 8, config = QuantConfig(seed = 123))
        assertEquals(a, b)
    }

    @Test
    fun `empty input yields no centers`() {
        assertTrue(quantizer.quantize(emptyList(), k = 4, config = QuantConfig()).isEmpty())
    }
}
