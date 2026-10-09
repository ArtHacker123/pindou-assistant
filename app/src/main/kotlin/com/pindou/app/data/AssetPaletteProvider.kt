package com.pindou.app.data

import android.content.Context
import com.pindou.core.model.Palette
import com.pindou.core.palette.PaletteMeta
import com.pindou.core.palette.PaletteProvider
import kotlinx.serialization.json.Json

/** Loads bundled bead palettes from the `assets/palettes` directory. */
class AssetPaletteProvider(private val context: Context) : PaletteProvider {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override fun available(): List<PaletteMeta> =
        assetNames().map { name ->
            val palette = load(name)
            PaletteMeta(palette.name, palette.displayName, palette.size, palette.version)
        }

    override fun load(name: String): Palette {
        val text = context.assets.open("palettes/$name.json").bufferedReader().use { it.readText() }
        return json.decodeFromString(Palette.serializer(), text)
    }

    private fun assetNames(): List<String> =
        context.assets.list("palettes")
            ?.filter { it.endsWith(".json") }
            ?.map { it.removeSuffix(".json") }
            ?.sorted()
            ?: emptyList()
}
