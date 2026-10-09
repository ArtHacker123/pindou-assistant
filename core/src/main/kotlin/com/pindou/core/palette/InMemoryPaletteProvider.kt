package com.pindou.core.palette

import com.pindou.core.model.Palette
import com.pindou.core.model.PaletteItem

/** In-memory palette provider, handy for tests and previews. */
class InMemoryPaletteProvider(
    private val palettes: Map<String, Palette>,
) : PaletteProvider {

    override fun available(): List<PaletteMeta> =
        palettes.values.map {
            PaletteMeta(it.name, it.displayName, it.size, it.version)
        }

    override fun load(name: String): Palette =
        palettes[name] ?: error("Unknown palette: $name")

    companion object {
        fun single(name: String, items: List<PaletteItem>, version: String = "1.0.0"): InMemoryPaletteProvider =
            InMemoryPaletteProvider(mapOf(name to Palette(name, items, version)))
    }
}
