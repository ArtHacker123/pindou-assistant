package com.pindou.core.imaging

import com.pindou.core.model.PixelImage
import org.junit.Assert.assertArrayEquals
import org.junit.Test

/**
 * Pins the built-in presets to the exact operation sequences the former
 * monolithic `CartoonProcessor`/`PhotoProcessor` used, so the pipeline
 * refactor stays byte-for-byte behavior preserving.
 */
class ProcessorPipelineTest {

    private fun sample(): PixelImage {
        val w = 6
        val h = 4
        val pixels = IntArray(w * h) { i ->
            val x = i % w
            val y = i / w
            (0xFF shl 24) or ((x * 40 + 20) shl 16) or ((y * 60 + 10) shl 8) or ((x * 10 + y * 20) and 0xFF)
        }
        return PixelImage(w, h, pixels)
    }

    @Test
    fun `cartoon pipeline matches the former processor`() {
        val image = sample()
        val expected = ImageOps.adjustSaturation(
            ImageOps.sharpen(ImageOps.adjustContrast(image, 1.12), amount = 0.6),
            factor = 1.08,
        )
        val actual = ImageProcessorRegistry.default().get(ImageType.CARTOON.id).process(image)
        assertArrayEquals(expected.pixels, actual.pixels)
    }

    @Test
    fun `photo pipeline matches the former processor`() {
        val image = sample()
        val expected = ImageOps.adjustSaturation(
            ImageOps.adjustContrast(ImageOps.boxBlur(image, radius = 1), factor = 1.06),
            factor = 1.05,
        )
        val actual = ImageProcessorRegistry.default().get(ImageType.PHOTO.id).process(image)
        assertArrayEquals(expected.pixels, actual.pixels)
    }
}
