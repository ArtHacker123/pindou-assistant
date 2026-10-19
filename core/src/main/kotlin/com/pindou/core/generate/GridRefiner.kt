package com.pindou.core.generate

import com.pindou.core.color.ColorSpace
import com.pindou.core.color.OklabColorSpace
import com.pindou.core.model.Rgb
import com.pindou.core.util.NaturalOrder
import java.util.TreeSet

/**
 * Removes speckle from a snapped bead grid.
 *
 * A photo snapped cell-by-cell leaves many isolated cells and tiny blobs of
 * distinct codes. This refiner merges them, but only when the colours involved
 * are perceptually close — so gradient/sensor noise collapses while small
 * high-contrast features (eyes, thin outlines) survive. Colours are compared in
 * [space] (Oklab by default).
 *
 * A single gated pass: every 8-connected region of one code whose area is at most
 * [DenoiseStrength.maxArea] is merged into the perceptually nearest non-hole
 * colour among its neighbouring cells, but only when that distance is at most
 * [DenoiseStrength.maxDelta]. Regions are processed **smallest first** so a small
 * blob is absorbed into the colour around it instead of two adjacent blobs
 * swapping colours, and each region is a fixed set of cells taken from a
 * snapshot, so the result does not depend on visit order. Because a lone cell is
 * just the smallest region, this also removes isolated single cells (the classic
 * salt-and-pepper case) with no separate pass.
 *
 * Empty holes (`null`) are inert: they are never candidates and never targets, so
 * subject boundaries are preserved. Because a merge target is always a colour
 * already present in the neighbourhood, the refiner never introduces a new colour
 * and can only reduce the distinct-code set. Ties break by natural code order.
 */
class GridRefiner(
    private val space: ColorSpace = OklabColorSpace,
) {

    /**
     * Returns a polished copy of [cells] (row-major, [width] x [height]). Returns
     * [cells] unchanged when [strength] is [DenoiseStrength.OFF] or the dimensions
     * do not match.
     */
    fun refine(
        cells: List<String?>,
        width: Int,
        height: Int,
        paletteByCode: Map<String, Rgb>,
        strength: DenoiseStrength,
    ): List<String?> {
        val n = width * height
        if (strength == DenoiseStrength.OFF || strength.maxArea <= 0) return cells
        if (n == 0 || cells.size != n) return cells

        val out = ArrayList(cells)
        val neighbours = neighbourTable(width, height)
        val labCache = HashMap<String, DoubleArray>()
        mergeSmallRegions(out, neighbours, labCache, paletteByCode, strength.maxArea, strength.maxDeltaSq)
        return out
    }

    private class Region(val cells: IntArray, val area: Int)

    private fun mergeSmallRegions(
        out: MutableList<String?>,
        neighbours: Array<IntArray>,
        labCache: MutableMap<String, DoubleArray>,
        paletteByCode: Map<String, Rgb>,
        maxArea: Int,
        maxDeltaSq: Double,
    ) {
        val snapshot = ArrayList(out)
        val n = snapshot.size
        val visited = BooleanArray(n)
        val stack = ArrayList<Int>()
        val regions = ArrayList<Region>()

        for (start in 0 until n) {
            val code = snapshot[start] ?: continue
            if (visited[start]) continue
            visited[start] = true
            stack.clear()
            stack.add(start)
            val members = ArrayList<Int>()
            while (stack.isNotEmpty()) {
                val cur = stack.removeAt(stack.size - 1)
                members.add(cur)
                for (nb in neighbours[cur]) {
                    if (!visited[nb] && snapshot[nb] == code) {
                        visited[nb] = true
                        stack.add(nb)
                    }
                }
            }
            regions.add(Region(members.toIntArray(), members.size))
        }

        // Smallest first; the outer scan discovers each region at its lowest
        // index, so the tie-break is deterministic too.
        regions.sortWith(compareBy({ it.area }, { it.cells[0] }))

        for (region in regions) {
            if (region.area > maxArea) continue
            val code = out[region.cells[0]] ?: continue
            val codeLab = lab(code, labCache, paletteByCode) ?: continue

            val neighbourCodes = TreeSet<String>(NaturalOrder)
            for (idx in region.cells) {
                for (nb in neighbours[idx]) {
                    val c = out[nb]
                    if (c != null && c != code) neighbourCodes.add(c)
                }
            }
            if (neighbourCodes.isEmpty()) continue

            var best: String? = null
            var bestDistance = Double.MAX_VALUE
            for (candidate in neighbourCodes) {
                val candidateLab = lab(candidate, labCache, paletteByCode) ?: continue
                val d = space.squaredDistance(codeLab, candidateLab)
                if (d < bestDistance) {
                    bestDistance = d
                    best = candidate
                }
            }
            if (best != null && bestDistance <= maxDeltaSq) {
                for (idx in region.cells) out[idx] = best
            }
        }
    }

    /** Encodes [code] once and caches it; null when the code is not a known colour. */
    private fun lab(
        code: String,
        cache: MutableMap<String, DoubleArray>,
        paletteByCode: Map<String, Rgb>,
    ): DoubleArray? {
        cache[code]?.let { return it }
        val rgb = paletteByCode[code] ?: return null
        val encoded = space.encode(rgb)
        cache[code] = encoded
        return encoded
    }

    /** In-bounds 8-neighbour indices for every grid position. */
    private fun neighbourTable(width: Int, height: Int): Array<IntArray> {
        val table = Array(width * height) { IntArray(0) }
        for (y in 0 until height) {
            for (x in 0 until width) {
                var count = 0
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        if (dx == 0 && dy == 0) continue
                        if (x + dx in 0 until width && y + dy in 0 until height) count++
                    }
                }
                val list = IntArray(count)
                var k = 0
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        if (dx == 0 && dy == 0) continue
                        val nx = x + dx
                        val ny = y + dy
                        if (nx in 0 until width && ny in 0 until height) list[k++] = ny * width + nx
                    }
                }
                table[y * width + x] = list
            }
        }
        return table
    }
}
