package com.pindou.core.match

import com.pindou.core.color.ColorSpace
import com.pindou.core.color.OklabColorSpace
import com.pindou.core.model.Palette
import com.pindou.core.model.PaletteItem
import com.pindou.core.model.Rgb

/**
 * Maps representative image colors onto real bead colors. This is the second
 * stage of the pipeline and is fully decoupled from how the representative
 * colors were produced. Comparison runs in a [ColorSpace] (Oklab by default) so
 * the bead that is picked is the perceptually closest one.
 */
class PaletteMapper(
    private val space: ColorSpace = OklabColorSpace,
) {
    fun nearestItem(target: Rgb, items: List<PaletteItem>): PaletteItem {
        require(items.isNotEmpty()) { "Palette is empty" }
        val idx = space.nearestIndex(target, items.map { it.rgb })
        return items[idx]
    }

    /** One bead color per representative color, preserving order. */
    fun mapCenters(centers: List<Rgb>, palette: Palette): List<PaletteItem> =
        centers.map { nearestItem(it, palette.items) }
}
