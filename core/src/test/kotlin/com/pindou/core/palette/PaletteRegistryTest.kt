package com.pindou.core.palette

import com.pindou.core.TestPalettes
import org.junit.Assert.assertEquals
import org.junit.Test

class PaletteRegistryTest {

    @Test
    fun `lists and loads palettes`() {
        val provider = InMemoryPaletteProvider.single("test", TestPalettes.basic().items)
        val registry = PaletteRegistry(provider)

        val metas = registry.available()
        assertEquals(1, metas.size)
        assertEquals("test", metas.first().name)
        assertEquals(5, metas.first().colorCount)
        assertEquals(5, registry.load("test").size)
    }

    @Test
    fun `caches loaded palettes`() {
        val provider = InMemoryPaletteProvider.single("test", TestPalettes.basic().items)
        val registry = PaletteRegistry(provider)
        val first = registry.load("test")
        val second = registry.load("test")
        assertEquals(first, second)
        assert(first === second)
    }
}
