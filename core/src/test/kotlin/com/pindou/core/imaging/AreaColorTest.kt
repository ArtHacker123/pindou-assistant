package com.pindou.core.imaging

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
        assertEquals(1f, AreaColor.cell(img, 2, 2, 1, 1).coverage)
        assertEquals(0f, AreaColor.cell(img, 2, 2, 0, 0).coverage)
        assertEquals(0.25f, AreaColor.cell(img, 1, 1, 0, 0).coverage)
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
        assertEquals(0.5f, AreaColor.cell(img, 1, 1, 0, 0).coverage)
    }

    @Test
    fun `average ignores invalid pixels and falls back to white when empty`() {
        val halfTransparent = PixelImage(4, 1, intArrayOf(0x00000000, 0x00000000, 0xFFFF0000.toInt(), 0xFFFF0000.toInt()))
        assertEquals(Rgb(255, 0, 0), AreaColor.cell(halfTransparent, 1, 1, 0, 0).rgb)

        val allTransparent = PixelImage(2, 1, intArrayOf(0x00FFFFFF.toInt(), 0x00FFFFFF.toInt()))
        assertEquals(Rgb(255, 255, 255), AreaColor.cell(allTransparent, 1, 1, 0, 0).rgb)
    }

    @Test
    fun `average mixes in linear light so black plus white stays bright`() {
        // One black + one white pixel: an sRGB mean would give 128, but the
        // physically correct midtone is ~188.
        val img = PixelImage(2, 1, intArrayOf(0xFF000000.toInt(), 0xFFFFFFFF.toInt()))
        val avg = AreaColor.cell(img, 1, 1, 0, 0).rgb
        assertTrue("expected a light midtone, was $avg", avg.r in 186..190 && avg.g in 186..190 && avg.b in 186..190)
    }

    @Test
    fun `mode representative is a real palette color for a mixed cell`() {
        val palette = com.pindou.core.TestPalettes.basic() // C1 black, C2 white, ...
        val lut = PaletteLut.build(palette)
        val img = PixelImage(2, 1, intArrayOf(0xFF000000.toInt(), 0xFFFFFFFF.toInt()))
        val sample = AreaColor.cell(img, 1, 1, 0, 0, lut, palette)
        // Average is still the linear-light midtone (~188) for the reference image.
        assertTrue("average unchanged, was ${sample.rgb}", sample.rgb.r in 186..190)
        // Mode snaps to a real bead (tie -> smallest natural code, black C1), never gray.
        assertEquals(Rgb(0, 0, 0), sample.repRgb)
    }

    @Test
    fun `mode representative picks the dominant color`() {
        val palette = com.pindou.core.TestPalettes.basic()
        val lut = PaletteLut.build(palette)
        // Three red-ish pixels, one black: red dominates.
        val img = PixelImage(4, 1, intArrayOf(
            0xFFFF0000.toInt(), 0xFF000000.toInt(), 0xFFFF0000.toInt(), 0xFFFF0000.toInt(),
        ))
        val sample = AreaColor.cell(img, 1, 1, 0, 0, lut, palette)
        assertEquals(Rgb(255, 0, 0), sample.repRgb)
    }
}