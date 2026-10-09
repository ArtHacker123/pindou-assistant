package com.pindou.core.generate

import com.pindou.core.model.PixelImage
import com.pindou.core.model.Rgb
import org.junit.Assert.assertEquals
import org.junit.Test

class AreaColorTest {

    @Test
    fun `averages a two by two split image`() {
        // Left half black, right half white (4x1 image, 2 columns).
        val pixels = intArrayOf(
            0xFF000000.toInt(), 0xFF000000.toInt(),
            0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt(),
        )
        val image = PixelImage(4, 1, pixels)
        assertEquals(Rgb(0, 0, 0), AreaColor.average(image, gridW = 2, gridH = 1, row = 0, col = 0))
        assertEquals(Rgb(255, 255, 255), AreaColor.average(image, gridW = 2, gridH = 1, row = 0, col = 1))
    }

    @Test
    fun `covers the final cell even when dimensions are not divisible`() {
        val image = PixelImage.filled(3, 3, 0xFF404040.toInt())
        // 2x2 grid over 3x3 pixels: last row/col must still sample real pixels.
        assertEquals(Rgb(64, 64, 64), AreaColor.average(image, gridW = 2, gridH = 2, row = 1, col = 1))
    }

    @Test
    fun `transparent region falls back to white`() {
        val image = PixelImage(2, 2, IntArray(4) { 0x00FFFFFF })
        assertEquals(Rgb(255, 255, 255), AreaColor.average(image, gridW = 1, gridH = 1, row = 0, col = 0))
    }
}
