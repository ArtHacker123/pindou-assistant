package com.pindou.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class PixelImagePadTest {

    private fun filled(w: Int, h: Int, argb: Int): PixelImage =
        PixelImage(w, h, IntArray(w * h) { argb })

    @Test
    fun `square image is returned unchanged`() {
        val img = filled(4, 4, 0xFF112233.toInt())
        val padded = img.paddedToSquare()
        assertSame(img, padded)
        assertEquals(4, padded.width)
        assertEquals(4, padded.height)
        assertEquals(img.pixels.toList(), padded.pixels.toList())
    }

    @Test
    fun `wider than tall pads height and centers vertically`() {
        // 4 wide x 2 tall -> 4 x 4, content placed in the middle two rows.
        val img = PixelImage(4, 2, IntArray(8) { i -> 0xFFAA0000.toInt() })
        val padded = img.paddedToSquare()
        assertEquals(4, padded.width)
        assertEquals(4, padded.height)
        assertEquals(0x00000000, padded.argb(0, 0))
        assertEquals(0xFFAA0000.toInt(), padded.argb(0, 1))
        assertEquals(0xFFAA0000.toInt(), padded.argb(3, 2))
        assertEquals(0x00000000, padded.argb(0, 3))
    }

    @Test
    fun `taller than wide pads width and centers horizontally`() {
        // 2 wide x 4 tall -> 4 x 4, content placed in the middle two columns.
        val img = PixelImage(2, 4, IntArray(8) { i -> 0xFF0022FF.toInt() })
        val padded = img.paddedToSquare()
        assertEquals(4, padded.width)
        assertEquals(4, padded.height)
        assertEquals(0x00000000, padded.argb(0, 0))
        assertEquals(0xFF0022FF.toInt(), padded.argb(1, 0))
        assertEquals(0xFF0022FF.toInt(), padded.argb(2, 3))
        assertEquals(0x00000000, padded.argb(3, 0))
    }
}