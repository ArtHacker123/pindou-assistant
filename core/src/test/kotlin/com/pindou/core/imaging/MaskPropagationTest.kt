package com.pindou.core.imaging

import com.pindou.core.model.PixelImage
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MaskPropagationTest {

    private fun maskedImage(): PixelImage {
        val w = 4
        val h = 2
        val pixels = IntArray(w * h) { 0xFF3C78B4.toInt() }
        val mask = BooleanArray(w * h) { it % 2 == 0 }
        return PixelImage(w, h, pixels, mask)
    }

    @Test
    fun `every preset color operation preserves the validity mask`() {
        val image = maskedImage()
        assertArrayEquals(image.mask, ImageOps.adjustContrast(image, 1.2).mask)
        assertArrayEquals(image.mask, ImageOps.adjustSaturation(image, 1.1).mask)
        assertArrayEquals(image.mask, ImageOps.boxBlur(image, radius = 1).mask)
        assertArrayEquals(image.mask, ImageOps.sharpen(image, amount = 0.5).mask)
    }

    @Test
    fun `square padding carries the mask and marks the padded region invalid`() {
        val image = maskedImage() // 4x2
        val padded = image.paddedToSquare() // 4x4, centered vertically (dy = 1)
        val dy = 1
        for (y in 0 until 2) {
            for (x in 0 until 4) {
                assertEquals(image.mask!![y * 4 + x], padded.mask!![(y + dy) * 4 + x])
            }
        }
        for (x in 0 until 4) {
            assertFalse("top padded row must be invalid", padded.mask!![x])
            assertFalse("bottom padded row must be invalid", padded.mask!![3 * 4 + x])
        }
    }
}
