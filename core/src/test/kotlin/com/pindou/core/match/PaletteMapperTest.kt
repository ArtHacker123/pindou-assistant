package com.pindou.core.match

import com.pindou.core.TestPalettes
import com.pindou.core.model.Rgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PaletteMapperTest {

    private val mapper = PaletteMapper()

    @Test
    fun `maps to nearest bead`() {
        val items = TestPalettes.basic().items
        assertEquals("C1", mapper.nearestItem(Rgb(5, 5, 5), items).code)
        assertEquals("C2", mapper.nearestItem(Rgb(250, 250, 250), items).code)
        assertEquals("C3", mapper.nearestItem(Rgb(240, 20, 20), items).code)
    }

    @Test
    fun `mapCenters preserves order`() {
        val palette = TestPalettes.basic()
        val mapped = mapper.mapCenters(listOf(Rgb(0, 0, 0), Rgb(255, 255, 255)), palette)
        assertEquals(listOf("C1", "C2"), mapped.map { it.code })
    }

    @Test
    fun `empty palette is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            mapper.nearestItem(Rgb(1, 1, 1), emptyList())
        }
    }
}
