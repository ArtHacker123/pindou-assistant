package com.pindou.core.imaging

import com.pindou.core.model.PixelImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageOpsTest {

    @Test
    fun `contrast factor one is identity`() {
        val image = PixelImage.filled(2, 2, 0xFF808080.toInt())
        assertEquals(image.argb(0, 0), ImageOps.adjustContrast(image, 1.0).argb(0, 0))
    }

    @Test
    fun `contrast pushes values away from mid gray`() {
        val dark = PixelImage.filled(1, 1, 0xFF404040.toInt())
        val out = ImageOps.adjustContrast(dark, 1.5)
        assertTrue((out.argb(0, 0) and 0xFF) < 0x40)
    }

    @Test
    fun `box blur averages a sharp edge`() {
        val pixels = intArrayOf(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
        val image = PixelImage(2, 1, pixels)
        val blurred = ImageOps.boxBlur(image, radius = 1)
        val left = blurred.argb(0, 0) and 0xFF
        assertTrue("blur should lighten the black pixel, was $left", left > 0)
    }

    @Test
    fun `box blur keeps transparent pixels invalid and ignores them when averaging`() {
        val image = PixelImage(2, 1, intArrayOf(0xFF808080.toInt(), 0x00000000))
        val blurred = ImageOps.boxBlur(image, radius = 1)
        // The opaque pixel is not darkened by its transparent neighbour.
        assertEquals(0xFF808080.toInt(), blurred.argb(0, 0))
        // The transparent pixel stays transparent (and therefore invalid).
        assertEquals(0, (blurred.argb(1, 0) ushr 24) and 0xFF)
        assertTrue(!blurred.valid(1, 0))
    }

    @Test
    fun `preserves alpha channel`() {
        val image = PixelImage.filled(1, 1, 0x80336699.toInt())
        val out = ImageOps.adjustSaturation(image, 1.4)
        assertEquals(0x80, (out.argb(0, 0) ushr 24) and 0xFF)
    }

    @Test
    fun `sharpen is identity for zero amount`() {
        val image = PixelImage.filled(2, 2, 0xFF123456.toInt())
        assertEquals(image.argb(0, 0), ImageOps.sharpen(image, 0.0).argb(0, 0))
    }
}
