package com.pindou.core.palette

import com.pindou.core.model.Palette
import com.pindou.core.model.PaletteItem
import org.junit.Assert.assertEquals
import org.junit.Test

class PaletteDefaultsTest {

    private fun palette(vararg items: PaletteItem) = Palette(name = "test", items = items.toList())

    @Test
    fun `prefers the bead named Black over a numerically darker color`() {
        val p = palette(
            PaletteItem(code = "D1", r = 20, g = 49, b = 59, name = "Dark Spruce"),
            PaletteItem(code = "K1", r = 50, g = 50, b = 52, name = "Black"),
        )
        assertEquals("K1", PaletteDefaults.defaultBrushItem(p)?.code)
    }

    @Test
    fun `matches the Black name case-insensitively and trimmed`() {
        val p = palette(
            PaletteItem(code = "R1", r = 0, g = 0, b = 0, name = "Red"),
            PaletteItem(code = "K9", r = 40, g = 40, b = 40, name = " black "),
        )
        assertEquals("K9", PaletteDefaults.defaultBrushItem(p)?.code)
    }

    @Test
    fun `falls back to the color nearest to pure black when no Black is named`() {
        val p = palette(
            PaletteItem(code = "H16", r = 30, g = 20, b = 14),
            PaletteItem(code = "H7", r = 1, g = 1, b = 1),
            PaletteItem(code = "B23", r = 90, g = 20, b = 27),
        )
        assertEquals("H7", PaletteDefaults.defaultBrushItem(p)?.code)
    }

    @Test
    fun `returns null for an empty palette`() {
        assertEquals(null, PaletteDefaults.defaultBrushItem(palette()))
    }
}
