package com.pindou.core.match

import com.pindou.core.TestPalettes
import com.pindou.core.imaging.GridSamples
import com.pindou.core.model.Palette
import com.pindou.core.model.PaletteItem
import com.pindou.core.model.PixelImage
import com.pindou.core.model.Rgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReferenceDirectStrategyTest {

    private val image = PixelImage.filled(1, 1, 0xFF000000.toInt())

    /** black C1, white C2, red C3, plus a near-black dark gray C6. */
    private val palette: Palette = Palette(
        name = "t",
        items = listOf(
            TestPalettes.black,
            TestPalettes.white,
            TestPalettes.red,
            PaletteItem(code = "C6", r = 15, g = 15, b = 15),
        ),
    )

    /** Builds 1-row samples from (color, valid) pairs. */
    private fun samples(cells: List<Pair<Rgb, Boolean>>): GridSamples {
        val n = cells.size
        return GridSamples(
            gridWidth = n,
            gridHeight = 1,
            coverage = FloatArray(n) { if (cells[it].second) 1f else 0f },
            avgRgb = Array(n) { cells[it].first },
            valid = BooleanArray(n) { cells[it].second },
        )
    }

    private fun repeated(color: Rgb, times: Int): List<Pair<Rgb, Boolean>> =
        List(times) { color to true }

    private val black = TestPalettes.black.rgb
    private val white = TestPalettes.white.rgb
    private val red = TestPalettes.red.rgb
    private val darkGray = PaletteItem(code = "C6", r = 15, g = 15, b = 15).rgb

    @Test
    fun `automatic keeps every bead that appears`() {
        val result = ReferenceDirectStrategy().select(
            image = image,
            samples = samples(listOf(black to true, white to true, red to true)),
            palette = palette,
            config = ColorSelectConfig(colorCountTarget = 16, auto = true),
        )
        assertEquals(setOf("C1", "C2", "C3"), result.map { it.code }.toSet())
    }

    @Test
    fun `capped returns the requested number of colors`() {
        val result = ReferenceDirectStrategy().select(
            image = image,
            samples = samples(listOf(black to true, white to true, red to true)),
            palette = palette,
            config = ColorSelectConfig(colorCountTarget = 2, auto = false),
        )
        assertEquals(2, result.size)
    }

    @Test
    fun `a rare distinct color is kept over a near-duplicate frequent color`() {
        // 30 black + 30 near-black gray + 1 red, capped to 2. Frequency ranking
        // would keep black+gray and drop red; max-coverage keeps the distinct red.
        val cells = repeated(black, 30) + repeated(darkGray, 30) + listOf(red to true)
        val result = ReferenceDirectStrategy().select(
            image = image,
            samples = samples(cells),
            palette = palette,
            config = ColorSelectConfig(colorCountTarget = 2, auto = false),
        )
        assertEquals(2, result.size)
        assertTrue("rare distinct red must be kept", result.any { it.code == "C3" })
    }

    @Test
    fun `invalid cells are ignored`() {
        val result = ReferenceDirectStrategy().select(
            image = image,
            samples = samples(listOf(black to true, white to false, red to true)),
            palette = palette,
            config = ColorSelectConfig(colorCountTarget = 16, auto = true),
        )
        assertEquals(setOf("C1", "C3"), result.map { it.code }.toSet())
    }

    @Test
    fun `is deterministic without a seed`() {
        val config = ColorSelectConfig(colorCountTarget = 2, auto = false)
        val cells = samples(listOf(black to true, black to true, black to true, white to true, red to true))
        val a = ReferenceDirectStrategy().select(image, cells, palette, config)
        val b = ReferenceDirectStrategy().select(image, cells, palette, config)
        assertEquals(a.map { it.code }, b.map { it.code })
        assertTrue(a.isNotEmpty())
    }
}
