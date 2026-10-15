package com.pindou.core.palette

import com.pindou.core.model.Palette
import com.pindou.core.util.NaturalOrder
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Validates every bundled palette asset so a malformed or mis-named file is
 * caught at build time, not at runtime.
 */
class BundledPaletteFileTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val palettesDir = File("../app/src/main/assets/palettes")

    private fun paletteFiles(): List<File> {
        val files = palettesDir
            .listFiles { f -> f.isFile && f.name.endsWith(".json") }
            ?.sortedBy { it.name }
            .orEmpty()
        assertTrue("no bundled palettes found at ${palettesDir.absolutePath}", files.isNotEmpty())
        return files
    }

    private fun load(file: File): Palette =
        json.decodeFromString(Palette.serializer(), file.readText())

    @Test
    fun `every bundled palette is named after its file`() {
        paletteFiles().forEach { file ->
            assertEquals(
                "palette name must match the file name for ${file.name}",
                file.nameWithoutExtension,
                load(file).name,
            )
        }
    }

    @Test
    fun `every bundled palette has well-formed items`() {
        paletteFiles().forEach { file ->
            val palette = load(file)
            assertTrue("${file.name} must not be empty", palette.items.isNotEmpty())

            val codes = palette.items.map { it.code }
            assertEquals("${file.name} has duplicate color codes", codes.size, codes.toSet().size)
            assertEquals(
                "${file.name} ids must be 1..N",
                (1..palette.items.size).toList(),
                palette.items.map { it.id },
            )
            assertTrue(
                "${file.name} has out-of-range rgb",
                palette.items.all { it.r in 0..255 && it.g in 0..255 && it.b in 0..255 },
            )
            assertEquals(
                "${file.name} codes must be in natural order",
                codes.sortedWith(NaturalOrder),
                codes,
            )
        }
    }

    @Test
    fun `expected bundled palette sizes`() {
        val sizes = paletteFiles().associate { it.nameWithoutExtension to load(it).items.size }
        assertEquals(221, sizes["mard_221"])
        assertEquals(103, sizes["perler"])
        assertEquals(92, sizes["hama_midi"])
        assertEquals(199, sizes["artkal_s"])
    }

    @Test
    fun `bundled palettes resolve the expected default black brush`() {
        val expected = mapOf(
            "mard_221" to "H7",
            "perler" to "80-19018",
            "hama_midi" to "H18",
            "artkal_s" to "S13",
        )
        expected.forEach { (name, code) ->
            val file = File(palettesDir, "$name.json")
            assertEquals("$name default brush", code, PaletteDefaults.defaultBrushItem(load(file))?.code)
        }
    }
}
