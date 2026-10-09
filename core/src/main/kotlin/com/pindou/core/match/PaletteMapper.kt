package com.pindou.core.match

import com.pindou.core.color.ColorDistance
import com.pindou.core.color.RgbEuclideanDistance
import com.pindou.core.model.Palette
import com.pindou.core.model.PaletteItem
import com.pindou.core.model.Rgb

/**
 * Maps representative image colors onto real bead colors. This is the second
 * stage of the pipeline and is fully decoupled from how the representative
 * colors were produced.
 */
class PaletteMapper(
    private val distance: ColorDistance = RgbEuclideanDistance,
) {
    fun nearestItem(target: Rgb, items: List<PaletteItem>): PaletteItem {
        require(items.isNotEmpty()) { "Palette is empty" }
        var best = items.first()
        var bestDistance = Long.MAX_VALUE
        for (item in items) {
            val d = distance.squaredDistance(target, item.rgb)
            if (d < bestDistance) {
                bestDistance = d
                best = item
            }
        }
        return best
    }

    /** One bead color per representative color, preserving order. */
    fun mapCenters(centers: List<Rgb>, palette: Palette): List<PaletteItem> =
        centers.map { nearestItem(it, palette.items) }
}
