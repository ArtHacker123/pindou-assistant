package com.pindou.core.editor

import com.pindou.core.color.ColorSpace
import com.pindou.core.color.OklabColorSpace
import com.pindou.core.model.Grid
import com.pindou.core.model.Palette
import com.pindou.core.model.PixelImage
import com.pindou.core.model.Rgb

/**
 * Pure, Android-independent grid editing operations. Every operation returns a
 * new immutable [Grid] (or the same instance when nothing changes), so the UI
 * can treat edits as ordinary state updates and the core stays unit-testable.
 *
 * Dependencies are limited to `model` + `color` (see `PackageBoundaryTest`).
 */
object GridEditor {

    /** Whether ([row], [col]) addresses a cell of [grid]. */
    fun inBounds(grid: Grid, row: Int, col: Int): Boolean =
        row in 0 until grid.height && col in 0 until grid.width

    /** Sets the bead at ([row], [col]) to [code] (`null` clears it). Out-of-range is a no-op. */
    fun paint(grid: Grid, row: Int, col: Int, code: String?): Grid {
        if (!inBounds(grid, row, col)) return grid
        val i = row * grid.width + col
        if (grid.cells[i] == code) return grid
        val cells = grid.cells.toMutableList()
        cells[i] = code
        return Grid(grid.width, grid.height, cells)
    }

    /** Clears the cell at ([row], [col]) to an empty hole. */
    fun erase(grid: Grid, row: Int, col: Int): Grid = paint(grid, row, col, null)

    /**
     * Flood-fills the 4-connected region of cells sharing the target cell's
     * color code (including `null` holes) with [code], bounded by the grid.
     */
    fun floodFill(grid: Grid, row: Int, col: Int, code: String?): Grid {
        if (!inBounds(grid, row, col)) return grid
        val target = grid[row, col]
        if (target == code) return grid
        val w = grid.width
        val h = grid.height
        val cells = grid.cells.toMutableList()
        val seen = BooleanArray(w * h)
        val stack = ArrayDeque<Int>()
        stack.addLast(row * w + col)
        seen[row * w + col] = true
        while (stack.isNotEmpty()) {
            val i = stack.removeLast()
            if (cells[i] != target) continue
            cells[i] = code
            val r = i / w
            val c = i % w
            if (c > 0) push(seen, stack, i - 1)
            if (c < w - 1) push(seen, stack, i + 1)
            if (r > 0) push(seen, stack, i - w)
            if (r < h - 1) push(seen, stack, i + w)
        }
        return Grid(w, h, cells)
    }

    private fun push(seen: BooleanArray, stack: ArrayDeque<Int>, i: Int) {
        if (!seen[i]) {
            seen[i] = true
            stack.addLast(i)
        }
    }

    /**
     * Clears the 4-connected region of cells sharing the target cell's color
     * code (the connected-region erase, symmetric to [floodFill]). Cells of
     * other codes and holes remain untouched.
     */
    fun regionErase(grid: Grid, row: Int, col: Int): Grid = floodFill(grid, row, col, null)

    /**
     * Re-maps every non-null cell to the perceptually closest color of [target].
     * Each existing code is resolved through [source] (the palette the grid was
     * authored with) — or through [target] itself as a fallback. Holes stay holes.
     */
    fun remap(
        grid: Grid,
        source: Palette,
        target: Palette,
        space: ColorSpace = OklabColorSpace,
    ): Grid {
        if (target.items.isEmpty()) return grid
        val candidates = target.items.map { it.rgb }
        val cache = HashMap<String, String>()
        var changed = false
        val cells = grid.cells.map { code ->
            if (code == null) return@map null
            val mapped = cache.getOrPut(code) {
                val rgb = source.item(code)?.rgb ?: target.item(code)?.rgb
                if (rgb == null) {
                    code
                } else {
                    val idx = space.nearestIndex(rgb, candidates)
                    if (idx >= 0) target.items[idx].code else code
                }
            }
            if (mapped != code) changed = true
            mapped
        }
        return if (changed) Grid(grid.width, grid.height, cells) else grid
    }

    /**
     * Resolves what the eyedropper selects at ([row], [col]). By default a bead
     * cell yields its own code, and an empty hole with a valid [reference] pixel
     * yields the nearest [target] palette color to that reference color. With
     * [preferReference] (used while the reference is shown on its own, e.g. the
     * press-and-hold peek) the reference takes precedence even over a bead, so
     * the pick matches what the user sees; without a reference it falls back to
     * the bead code.
     */
    fun pick(
        grid: Grid,
        reference: PixelImage?,
        target: Palette,
        space: ColorSpace = OklabColorSpace,
        preferReference: Boolean = false,
        row: Int,
        col: Int,
    ): String? {
        if (!inBounds(grid, row, col)) return null
        val code = grid[row, col]
        if (!preferReference && code != null) return code
        val ref = reference ?: return code
        if (col !in 0 until ref.width || row !in 0 until ref.height) return code
        val argb = ref.argb(col, row)
        if (((argb ushr 24) and 0xFF) < 8) return code
        val idx = space.nearestIndex(Rgb.fromArgb(argb), target.items.map { it.rgb })
        return if (idx >= 0) target.items[idx].code else code
    }
}
