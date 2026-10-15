package com.pindou.core.palette

import com.pindou.core.model.Palette
import com.pindou.core.model.PaletteItem

/**
 * Shared defaults for the bundled bead palettes.
 *
 * The default palette is pinned by [name] rather than asset ordering, so adding
 * or removing bundled palette files never silently changes the startup or
 * template-mapping palette.
 */
object PaletteDefaults {

    /** Name of the bundled palette used at startup and for template mapping. */
    const val DEFAULT_NAME: String = "mard_221"

    /** Resolves the default palette meta, falling back to the first available. */
    fun defaultMeta(metas: List<PaletteMeta>): PaletteMeta? =
        metas.firstOrNull { it.name == DEFAULT_NAME } ?: metas.firstOrNull()

    /**
     * The bead used as the default brush: the palette's bead named `Black` when
     * one exists, otherwise the color nearest to pure black. Palettes without
     * names (e.g. MARD) therefore fall back to their nearest-black bead.
     */
    fun defaultBrushItem(palette: Palette): PaletteItem? {
        val items = palette.items
        items.firstOrNull { it.name?.trim().equals("Black", ignoreCase = true) }?.let { return it }
        return items.minByOrNull { it.r * it.r + it.g * it.g + it.b * it.b }
    }
}
