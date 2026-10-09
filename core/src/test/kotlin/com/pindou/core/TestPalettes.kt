package com.pindou.core

import com.pindou.core.model.Palette
import com.pindou.core.model.PaletteItem

/** Shared fixtures for core unit tests. */
object TestPalettes {

    val black = PaletteItem(code = "C1", r = 0, g = 0, b = 0, name = "Black")
    val white = PaletteItem(code = "C2", r = 255, g = 255, b = 255, name = "White")
    val red = PaletteItem(code = "C3", r = 255, g = 0, b = 0, name = "Red")
    val green = PaletteItem(code = "C4", r = 0, g = 255, b = 0, name = "Green")
    val blue = PaletteItem(code = "C5", r = 0, g = 0, b = 255, name = "Blue")

    fun basic(): Palette = Palette(
        name = "test",
        items = listOf(black, white, red, green, blue),
    )

    /** A palette with C10 after C2 to exercise natural ordering. */
    fun withMany(): Palette = Palette(
        name = "many",
        items = listOf(
            black,
            white,
            red,
            green,
            blue,
            PaletteItem(code = "C10", r = 128, g = 128, b = 128),
        ),
    )
}
