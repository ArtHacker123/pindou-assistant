package com.pindou.core.editor

import com.pindou.core.model.Grid
import com.pindou.core.model.Palette
import com.pindou.core.model.PaletteItem
import com.pindou.core.model.PixelImage
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class GridEditorTest {

    private fun item(code: String, r: Int, g: Int, b: Int) = PaletteItem(code = code, r = r, g = g, b = b)

    private fun palette(vararg items: PaletteItem) = Palette(name = "test", items = items.toList())

    private fun grid(width: Int, height: Int, vararg cells: String?): Grid =
        Grid(width, height, cells.toList())

    // ---- paint / erase ----------------------------------------------------

    @Test
    fun `paint sets a cell`() {
        val g = grid(2, 1, null, "A")
        val out = GridEditor.paint(g, 0, 0, "B")
        assertEquals("B", out[0, 0])
        assertEquals("A", out[0, 1])
    }

    @Test
    fun `paint out of range is a no-op`() {
        val g = grid(2, 2, null, null, null, null)
        assertSame(g, GridEditor.paint(g, -1, 0, "A"))
        assertSame(g, GridEditor.paint(g, 0, 5, "A"))
        assertSame(g, GridEditor.paint(g, 2, 0, "A"))
    }

    @Test
    fun `paint same code returns the same instance`() {
        val g = grid(1, 1, "A")
        assertSame(g, GridEditor.paint(g, 0, 0, "A"))
    }

    @Test
    fun `erase clears a cell`() {
        val g = grid(1, 1, "A")
        assertNull(GridEditor.erase(g, 0, 0)[0, 0])
    }

    // ---- flood fill -------------------------------------------------------

    @Test
    fun `flood fill replaces a connected region only`() {
        val g = grid(
            3, 3,
            "A", "A", "B",
            "A", "B", "B",
            "C", "C", "B",
        )
        val out = GridEditor.floodFill(g, 0, 0, "X")
        assertEquals("X", out[0, 0])
        assertEquals("X", out[1, 0])
        assertEquals("X", out[0, 1])
        assertEquals("B", out[0, 2])
        assertEquals("B", out[1, 1])
        assertEquals("C", out[2, 0])
    }

    @Test
    fun `flood fill treats holes as a region`() {
        val g = grid(
            2, 2,
            null, "A",
            null, "A",
        )
        val out = GridEditor.floodFill(g, 0, 0, "X")
        assertEquals("X", out[0, 0])
        assertEquals("X", out[1, 0])
        assertEquals("A", out[0, 1])
    }

    @Test
    fun `flood fill with the same target is a no-op`() {
        val g = grid(1, 1, "A")
        assertSame(g, GridEditor.floodFill(g, 0, 0, "A"))
    }

    @Test
    fun `region erase clears a connected region only`() {
        val g = grid(
            3, 3,
            "A", "A", "B",
            "A", "B", "B",
            "C", "C", "B",
        )
        val out = GridEditor.regionErase(g, 0, 0)
        assertNull(out[0, 0])
        assertNull(out[1, 0])
        assertNull(out[0, 1])
        assertEquals("B", out[0, 2])
        assertEquals("B", out[1, 1])
        assertEquals("C", out[2, 0])
    }

    @Test
    fun `region erase on a hole is a no-op`() {
        val g = grid(2, 1, null, "A")
        assertSame(g, GridEditor.regionErase(g, 0, 0))
    }

    // ---- replace all ------------------------------------------------------

    @Test
    fun `replaceAll changes every matching cell everywhere`() {
        val g = grid(
            3, 3,
            "A", "B", "A",
            "B", "B", "A",
            "C", "A", "B",
        )
        val out = GridEditor.replaceAll(g, "A", "X")
        assertEquals("X", out[0, 0])
        assertEquals("X", out[0, 2])
        assertEquals("X", out[1, 2])
        assertEquals("X", out[2, 1])
        assertEquals("B", out[0, 1])
        assertEquals("C", out[2, 0])
    }

    @Test
    fun `replaceAll replaces disconnected cells of the same code`() {
        val g = grid(3, 1, "A", "B", "A")
        val out = GridEditor.replaceAll(g, "A", "X")
        assertEquals("X", out[0, 0])
        assertEquals("B", out[0, 1])
        assertEquals("X", out[0, 2])
    }

    @Test
    fun `replaceAll keeps holes and other codes`() {
        val g = grid(3, 1, "A", null, "B")
        val out = GridEditor.replaceAll(g, "A", "X")
        assertEquals("X", out[0, 0])
        assertNull(out[0, 1])
        assertEquals("B", out[0, 2])
    }

    @Test
    fun `replaceAll with the same code is a no-op`() {
        val g = grid(2, 1, "A", "B")
        assertSame(g, GridEditor.replaceAll(g, "A", "A"))
    }

    @Test
    fun `replaceAll with no match returns the same instance`() {
        val g = grid(2, 1, "A", "B")
        assertSame(g, GridEditor.replaceAll(g, "Z", "X"))
    }

    // ---- remap ------------------------------------------------------------

    @Test
    fun `remap maps each code to the nearest target color and keeps holes`() {
        val source = palette(item("A", 0, 0, 0), item("B", 255, 255, 255))
        val target = palette(item("X", 10, 10, 10), item("Y", 245, 245, 245))
        val g = grid(3, 1, "A", "B", null)
        val out = GridEditor.remap(g, source, target)
        assertEquals("X", out[0, 0])
        assertEquals("Y", out[0, 1])
        assertNull(out[0, 2])
    }

    @Test
    fun `remap falls back to the target palette when source lacks the code`() {
        val source = palette()
        val target = palette(item("X", 0, 0, 0), item("Y", 255, 255, 255))
        val g = grid(2, 1, "X", "Z")
        val out = GridEditor.remap(g, source, target)
        assertEquals("X", out[0, 0])
        // "Z" is unknown everywhere, so it is left untouched.
        assertEquals("Z", out[0, 1])
    }

    // ---- eyedropper -------------------------------------------------------

    @Test
    fun `eyedropper returns an existing bead code`() {
        val target = palette(item("C5", 200, 0, 0))
        val g = grid(1, 1, "C5")
        assertEquals("C5", GridEditor.pick(g, null, target, row = 0, col = 0))
    }

    @Test
    fun `eyedropper snaps a reference hole to the nearest palette color`() {
        val target = palette(item("RED", 255, 0, 0), item("BLU", 0, 0, 255))
        val g = grid(1, 1, null)
        val reference = PixelImage(1, 1, intArrayOf(0xFFCC1818.toInt())) // red-leaning
        assertEquals("RED", GridEditor.pick(g, reference, target, row = 0, col = 0))
    }

    @Test
    fun `eyedropper on a hole without a reference is a no-op`() {
        val target = palette(item("RED", 255, 0, 0))
        val g = grid(1, 1, null)
        assertNull(GridEditor.pick(g, null, target, row = 0, col = 0))
    }

    @Test
    fun `eyedropper ignores transparent reference holes`() {
        val target = palette(item("RED", 255, 0, 0))
        val g = grid(1, 1, null)
        val reference = PixelImage(1, 1, intArrayOf(0x00000000))
        assertNull(GridEditor.pick(g, reference, target, row = 0, col = 0))
    }

    @Test
    fun `eyedropper with prefer-reference takes the reference color over a bead`() {
        val target = palette(item("RED", 255, 0, 0), item("BLU", 0, 0, 255))
        val g = grid(1, 1, "BLU")
        val reference = PixelImage(1, 1, intArrayOf(0xFFCC1818.toInt())) // red-leaning
        assertEquals("RED", GridEditor.pick(g, reference, target, preferReference = true, row = 0, col = 0))
    }

    @Test
    fun `eyedropper with prefer-reference falls back to the bead without a reference`() {
        val target = palette(item("RED", 255, 0, 0), item("BLU", 0, 0, 255))
        val g = grid(1, 1, "BLU")
        assertEquals("BLU", GridEditor.pick(g, null, target, preferReference = true, row = 0, col = 0))
    }
}
