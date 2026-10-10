package com.pindou.core.palette

import com.pindou.core.model.Palette
import com.pindou.core.util.NaturalOrder
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class Mard221PaletteFileTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun loadAsset(): Palette {
        val file = File("../app/src/main/assets/palettes/mard_221.json")
        assertTrue("bundled mard_221.json not found at ${file.absolutePath}", file.exists())
        return json.decodeFromString<Palette>(file.readText())
    }

    @Test
    fun `bundled mard 221 palette has exactly 221 unique colors`() {
        val palette = loadAsset()
        assertEquals("mard_221", palette.name)
        assertEquals(221, palette.items.size)
        assertEquals(221, palette.items.map { it.code }.toSet().size)
        assertEquals((1..221).toList(), palette.items.map { it.id })
    }

    @Test
    fun `bundled mard 221 rgb values are within range`() {
        val palette = loadAsset()
        assertTrue(palette.items.all { it.r in 0..255 && it.g in 0..255 && it.b in 0..255 })
    }

    @Test
    fun `bundled mard 221 codes are natural-order sorted`() {
        val palette = loadAsset()
        val codes = palette.items.map { it.code }
        assertEquals(codes.sortedWith(NaturalOrder), codes)
    }
}