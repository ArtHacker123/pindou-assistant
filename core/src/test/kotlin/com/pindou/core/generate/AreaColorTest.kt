package com.pindou.core.generate

import com.pindou.core.model.PixelImage
import com.pindou.core.model.Rgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AreaColorTest {

    @Test
    fun `coverage counts valid pixels by cell region`() {
        // 2x2: only the bottom-right pixel is opaque.
        val img = PixelImage(
            width = 2,
            height = 2,
            pixels = intArrayOf(
                0x00FFFFFF.toInt(), 0x00FFFFFF.toInt(),
                0x00FFFFFF.toInt(), 0xFF123456.toInt(),
            ),
        )
        assertEquals(1f, AreaColor.coverage(img, 2, 2, 1, 1))
        assertEquals(0f, AreaColor.coverage(img, 2, 2, 0, 0))
        assertEquals(0.25f, AreaColor.coverage(img, 1, 1, 0, 0))
    }

    @Test
    fun `coverage uses explicit mask when present`() {
        // Opaque everywhere, but the mask marks the right half valid.
        val img = PixelImage(
            width = 4,
            height = 1,
            pixels = intArrayOf(0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt()),
            mask = booleanArrayOf(false, false, true, true),
        )
        assertEquals(0.5f, AreaColor.coverage(img, 1, 1, 0, 0))
    }

    @Test
    fun `average ignores invalid pixels and falls back to white when empty`() {
        val halfTransparent = PixelImage(4, 1, intArrayOf(0x00000000, 0x00000000, 0xFFFF0000.toInt(), 0xFFFF0000.toInt()))
        assertEquals(Rgb(255, 0, 0), AreaColor.average(halfTransparent, 1, 1, 0, 0))

        val allTransparent = PixelImage(2, 1, intArrayOf(0x00FFFFFF.toInt(), 0x00FFFFFF.toInt()))
        assertEquals(Rgb(255, 255, 255), AreaColor.average(allTransparent, 1, 1, 0, 0))
    }

    @Test
    fun `average mixes in linear light so black plus white stays bright`() {
        // One black + one white pixel: an sRGB mean would give 128, but the
        // physically correct midtone is ~188.
        val img = PixelImage(2, 1, intArrayOf(0xFF000000.toInt(), 0xFFFFFFFF.toInt()))
        val avg = AreaColor.average(img, 1, 1, 0, 0)
        assertTrue("expected a light midtone, was $avg", avg.r in 186..190 && avg.g in 186..190 && avg.b in 186..190)
    }
}