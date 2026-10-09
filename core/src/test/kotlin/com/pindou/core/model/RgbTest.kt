package com.pindou.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RgbTest {

    @Test
    fun `fromArgb splits channels`() {
        val rgb = Rgb.fromArgb(0xFF3366CC.toInt())
        assertEquals(Rgb(0x33, 0x66, 0xCC), rgb)
    }

    @Test
    fun `toArgb rebuilds opaque color`() {
        assertEquals(0xFF3366CC.toInt(), Rgb(0x33, 0x66, 0xCC).toArgb())
    }

    @Test
    fun `whites have max luminance and blacks zero`() {
        assertEquals(255.0, Rgb(255, 255, 255).luminance(), 0.001)
        assertEquals(0.0, Rgb(0, 0, 0).luminance(), 0.001)
    }
}
