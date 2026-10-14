package com.pindou.core.match

import com.pindou.core.color.ColorSpace
import com.pindou.core.color.LabColorSpace
import com.pindou.core.imaging.GridSamples
import com.pindou.core.model.Palette
import com.pindou.core.model.PaletteItem
import com.pindou.core.model.PixelImage
import com.pindou.core.util.NaturalOrder
import kotlin.math.pow

/**
 * Selects the used beads directly from the reference image: each valid cell's
 * average color is matched to the perceptually nearest bead in the full palette.
 *
 * Automatic color count keeps every bead that appears (no cap). When a manual
 * count N is set, the N beads are chosen by **greedy maximum coverage** rather
 * than by raw frequency: each pick is the bead that reduces the total perceptual
 * error of snapping every cell to the chosen set the most. This preserves rare
 * but visually distinct colors (eyes, outlines, highlights) that a pure
 * frequency ranking would drop, because discarding them would cost a large error.
 *
 * To keep the greedy tractable, cells are grouped by their nearest bead and each
 * group is represented by its weighted Lab centroid. Cell weights use
 * `count^GAMMA` (< count) so small areas carry relatively more influence, biasing
 * toward rare key colors while still respecting area.
 *
 * Deterministic: the greedy is a pure function of the cells, and ties are broken
 * by natural color-code order, so no seed is needed.
 */
class ReferenceDirectStrategy(
    private val space: ColorSpace = LabColorSpace,
) : ColorStrategy {

    override val id = ID
    override val displayName = "参考图直取"

    override fun select(
        image: PixelImage,
        samples: GridSamples,
        palette: Palette,
        config: ColorSelectConfig,
    ): List<PaletteItem> {
        val items = palette.items
        if (items.isEmpty()) return emptyList()

        val paletteLab: List<DoubleArray> = items.map { space.encode(it.rgb) }

        // Group valid cells by their nearest palette bead: weight + Lab centroid.
        // The palette is encoded once (above) and each cell once here; nearest
        // lookups reuse the cached encodings instead of re-encoding per candidate.
        val counts = IntArray(items.size)
        val sums = Array(items.size) { DoubleArray(3) }
        for (i in 0 until samples.size) {
            if (!samples.valid[i]) continue
            val lab = space.encode(samples.avgRgb[i])
            val idx = space.nearestIndex(lab, paletteLab)
            if (idx < 0) continue
            counts[idx]++
            val sum = sums[idx]
            sum[0] += lab[0]
            sum[1] += lab[1]
            sum[2] += lab[2]
        }

        val candidates = items.indices
            .filter { counts[it] > 0 }
            .sortedWith(compareBy(NaturalOrder) { items[it].code })
        if (candidates.isEmpty()) return emptyList()

        // Automatic (uncapped), or a target that already covers every color: keep all.
        val target = config.colorCountTarget.coerceAtLeast(1)
        if (config.auto || target >= candidates.size) {
            return candidates.map { items[it] }
        }

        val weight = DoubleArray(items.size)
        val centroid = Array(items.size) { DoubleArray(3) }
        for (j in candidates) {
            val count = counts[j]
            weight[j] = count.toDouble().pow(GAMMA)
            centroid[j] = doubleArrayOf(
                sums[j][0] / count,
                sums[j][1] / count,
                sums[j][2] / count,
            )
        }

        val bestDist = DoubleArray(items.size) { Double.POSITIVE_INFINITY }
        val selected = ArrayList<Int>(target)

        // First pick: the weighted medoid (minimizes total error on its own).
        var first = candidates[0]
        var firstCost = Double.MAX_VALUE
        for (j in candidates) {
            var cost = 0.0
            for (i in candidates) {
                cost += weight[i] * space.squaredDistance(centroid[i], paletteLab[j])
            }
            if (cost < firstCost) {
                firstCost = cost
                first = j
            }
        }
        selected.add(first)
        for (i in candidates) {
            bestDist[i] = space.squaredDistance(centroid[i], paletteLab[first])
        }

        // Remaining picks: the bead that reduces total error the most.
        while (selected.size < target) {
            var pick = -1
            var pickGain = 0.0
            for (j in candidates) {
                var gain = 0.0
                for (i in candidates) {
                    val d = space.squaredDistance(centroid[i], paletteLab[j])
                    if (d < bestDist[i]) gain += weight[i] * (bestDist[i] - d)
                }
                if (gain > pickGain) {
                    pickGain = gain
                    pick = j
                }
            }
            if (pick < 0) break
            selected.add(pick)
            for (i in candidates) {
                val d = space.squaredDistance(centroid[i], paletteLab[pick])
                if (d < bestDist[i]) bestDist[i] = d
            }
        }

        return selected.map { items[it] }
    }

    companion object {
        const val ID = "reference"

        /** Weight exponent for cell counts: <1 lifts rare distinct colors. */
        private const val GAMMA = 0.5
    }
}
