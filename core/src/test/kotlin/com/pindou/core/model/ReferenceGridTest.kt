package com.pindou.core.model

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ReferenceGridTest {

    @Test
    fun `round trips pixels including transparent holes`() {
        val pixels = intArrayOf(
            0xFFFF0000.toInt(), 0x00000000, // opaque red, transparent hole
            0x80123456.toInt(), 0xFFFFFFFF.toInt(), // semi-transparent, white
        )
        val image = PixelImage(2, 2, pixels)

        val reference = image.toReferenceGrid()

        assertEquals(2, reference.width)
        assertEquals(2, reference.height)
        assertEquals(2 * 2 * 8, reference.argbHex.length)
        assertArrayEquals(pixels, reference.toImage().pixels)
    }

    @Test
    fun `pads short hex to eight digits`() {
        val reference = PixelImage(1, 1, intArrayOf(0x00000001)).toReferenceGrid()
        assertEquals("00000001", reference.argbHex)
    }

    @Test
    fun `rejects mismatched hex length`() {
        assertThrows(IllegalArgumentException::class.java) {
            ReferenceGrid(width = 2, height = 2, argbHex = "00000000")
        }
    }
}
