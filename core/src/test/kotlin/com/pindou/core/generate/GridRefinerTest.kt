package com.pindou.core.generate

import com.pindou.core.model.Rgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class GridRefinerTest {

    private val refiner = GridRefiner()

    // Two perceptually close light skin tones (~0.01 Oklab apart) and a dark eye
    // colour far from both (~0.5), matching the real speckle/feature distances.
    private val l1 = "L1" to Rgb(238, 217, 196)
    private val l2 = "L2" to Rgb(236, 214, 192)
    private val dk = "DK" to Rgb(24, 24, 24)
    private val bg = "BG" to Rgb(255, 246, 224)
    private val palette = mapOf(l1, l2, dk, bg)

    private fun row(vararg codes: String?): List<String?> = listOf(*codes)

    private fun refine(
        cells: List<String?>,
        width: Int,
        height: Int,
        strength: DenoiseStrength,
    ): List<String?> = refiner.refine(cells, width, height, palette, strength)

    @Test
    fun `a close one-cell speckle is merged into its surroundings`() {
        // 3x3 light face with a close-toned speckle at the centre (MEDIUM, maxArea 2).
        val cells = row(
            l1.first, l1.first, l1.first,
            l1.first, l2.first, l1.first,
            l1.first, l1.first, l1.first,
        )
        val out = refine(cells, 3, 3, DenoiseStrength.MEDIUM)
        assertEquals(row(l1.first, l1.first, l1.first,
            l1.first, l1.first, l1.first,
            l1.first, l1.first, l1.first), out)
    }

    @Test
    fun `a far-coloured isolated cell (eye) is preserved even at strong`() {
        // 3x3 light face with a dark eye at the centre: STRONG must not eat it.
        val cells = row(
            l1.first, l1.first, l1.first,
            l1.first, dk.first, l1.first,
            l1.first, l1.first, l1.first,
        )
        val out = refine(cells, 3, 3, DenoiseStrength.STRONG)
        assertEquals(row(l1.first, l1.first, l1.first,
            l1.first, dk.first, l1.first,
            l1.first, l1.first, l1.first), out)
    }

    @Test
    fun `a region larger than maxArea is untouched`() {
        // Two close-toned bands of 5 each: both exceed STRONG's maxArea of 4.
        val cells = row(l1.first, l1.first, l1.first, l1.first, l1.first,
                        l2.first, l2.first, l2.first, l2.first, l2.first)
        val out = refine(cells, 10, 1, DenoiseStrength.STRONG)
        assertEquals(cells, out)
    }

    @Test
    fun `a region exactly at maxArea merges`() {
        // Five L1 then one L2: the lone L2 (area 1) merges into L1.
        val cells = row(l1.first, l1.first, l1.first, l1.first, l1.first, l2.first)
        val out = refine(cells, 6, 1, DenoiseStrength.MEDIUM)
        assertEquals(row(l1.first, l1.first, l1.first, l1.first, l1.first, l1.first), out)
    }

    @Test
    fun `a one-cell-wide line is preserved`() {
        // A dark 4-cell line through a light field (STRONG, area 4 = maxArea):
        // its only neighbour colour is far away, so nothing merges.
        val cells = row(
            bg.first, bg.first, bg.first, bg.first, bg.first, bg.first, bg.first,
            bg.first, bg.first, dk.first, dk.first, dk.first, dk.first, bg.first,
            bg.first, bg.first, bg.first, bg.first, bg.first, bg.first, bg.first,
        )
        val out = refine(cells, 7, 3, DenoiseStrength.STRONG)
        assertEquals(cells, out)
    }

    @Test
    fun `empty holes are untouched`() {
        val cells = row(
            null, null, l1.first,
            null, l2.first, l1.first,
            l1.first, l1.first, l1.first,
        )
        val out = refine(cells, 3, 3, DenoiseStrength.MEDIUM)
        assertEquals(null, out[0])
        assertEquals(null, out[1])
        assertEquals(null, out[3])
        assertEquals(l1.first, out[4]) // speckle still merged
    }

    @Test
    fun `no colour absent beforehand appears afterwards`() {
        val cells = row(
            l1.first, l2.first, l1.first,
            l2.first, dk.first, l2.first,
            l1.first, l2.first, l1.first,
        )
        val out = refine(cells, 3, 3, DenoiseStrength.STRONG)
        assertTrue("output codes must be a subset of the input codes",
            out.filterNotNull().all { it in palette.keys })
    }

    @Test
    fun `two runs are identical`() {
        val cells = row(
            l1.first, l2.first, l1.first, l1.first, l2.first, bg.first, dk.first,
            l2.first, l1.first, l2.first, bg.first, l1.first, l2.first, l1.first,
        )
        val a = refine(cells, 7, 2, DenoiseStrength.STRONG)
        val b = refine(cells, 7, 2, DenoiseStrength.STRONG)
        assertEquals(a, b)
    }

    @Test
    fun `off returns the input list untouched`() {
        val cells = row(l1.first, l2.first, null)
        val out = refine(cells, 3, 1, DenoiseStrength.OFF)
        assertSame(cells, out)
    }

    @Test
    fun `strong merges at least as many cells as weak`() {
        // Five L1 then three L2 (STRONG maxArea 4 merges the whole L2 band;
        // WEAK maxArea 1 leaves it alone).
        val cells = row(l1.first, l1.first, l1.first, l1.first, l1.first,
                        l2.first, l2.first, l2.first)
        val weak = refine(cells, 8, 1, DenoiseStrength.WEAK)
        val strong = refine(cells, 8, 1, DenoiseStrength.STRONG)
        fun changed(out: List<String?>) = out.countIndexed { i, c -> c != cells[i] }
        assertTrue("weak should not merge a 3-cell region", changed(weak) == 0)
        assertTrue("strong should merge the whole region", changed(strong) == 3)
    }

    private inline fun List<String?>.countIndexed(predicate: (Int, String?) -> Boolean): Int {
        var n = 0
        for (i in indices) if (predicate(i, this[i])) n++
        return n
    }
}
