package com.pindou.core.palette

import com.pindou.core.model.Palette

/** Lightweight descriptor for a selectable palette. */
data class PaletteMeta(
    val name: String,
    val displayName: String,
    val colorCount: Int,
    val version: String = "1.0.0",
)

/** Loads bead palettes from some backing store (assets, files, network...). */
interface PaletteProvider {
    fun available(): List<PaletteMeta>
    fun load(name: String): Palette
}

/** Caches loaded palettes and resolves them by name. */
class PaletteRegistry(private val provider: PaletteProvider) {
    private val cache = HashMap<String, Palette>()
    private var metas: List<PaletteMeta>? = null

    fun available(): List<PaletteMeta> =
        metas ?: provider.available().also { metas = it }

    fun load(name: String): Palette =
        cache.getOrPut(name) { provider.load(name) }

    /** The default palette (pinned by name), used as the default selection. */
    fun default(): Palette {
        val meta = PaletteDefaults.defaultMeta(available())
            ?: error("No palettes available")
        return load(meta.name)
    }
}
