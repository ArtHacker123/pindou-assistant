package com.pindou.core.imaging

import com.pindou.core.TestPalettes
import com.pindou.core.model.PixelImage
import com.pindou.core.model.Rgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GridSamplerTest {

    /** One cell covering an equal mix of black and white. */
    private val mixed = PixelImage(2, 1, intArrayOf(0xFF000000.toInt(), 0xFFFFFFFF.toInt()))

    @Test
    fun `average sampling leaves representatives unset`() {
        val samples = GridSampler.sample(mixed, 1, 1)
        assertNull(samples.repRgb)
        assertEquals(samples.avgRgb[0], samples.rep(0))
    }

    @Test
    fun `mode sampling fills representatives but keeps the average`() {
        val palette = TestPalettes.basic() // C1 black, C2 white, ...
        val average = GridSampler.sample(mixed, 1, 1)
        val mode = GridSampler.sample(mixed, 1, 1, CellReduction.MODE, palette)

        // The reference backing average is untouched: still a light midtone.
        assertEquals(average.avgRgb[0], mode.avgRgb[0])
        assertTrue("average stays ~188, was ${mode.avgRgb[0]}", mode.avgRgb[0].r in 186..190)

        // The representative is a real palette color, never the synthesized gray.
        // The tie breaks to the smallest natural code (C1 black).
        assertEquals(Rgb(0, 0, 0), mode.repRgb!![0])
    }

    @Test
    fun `mode sampling without a palette degrades to average`() {
        val samples = GridSampler.sample(mixed, 1, 1, CellReduction.MODE, palette = null)
        assertNull(samples.repRgb)
    }
}
