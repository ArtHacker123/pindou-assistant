package com.pindou.core.model

/**
 * An immutable RGB color triple with components in 0..255.
 */
data class Rgb(val r: Int, val g: Int, val b: Int) {

    /** Packs this color into an opaque ARGB int. */
    fun toArgb(): Int = (0xFF shl 24) or ((r and 0xFF) shl 16) or ((g and 0xFF) shl 8) or (b and 0xFF)

    /** Perceptual luminance in 0..255, used to choose readable label ink. */
    fun luminance(): Double = 0.299 * r + 0.587 * g + 0.114 * b

    companion object {
        fun fromArgb(argb: Int): Rgb =
            Rgb((argb shr 16) and 0xFF, (argb shr 8) and 0xFF, argb and 0xFF)
    }
}
