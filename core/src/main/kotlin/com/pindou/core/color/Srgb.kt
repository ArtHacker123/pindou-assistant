package com.pindou.core.color

/**
 * sRGB transfer function (IEC 61966-2-1).
 *
 * Averaging must happen in linear light because sRGB encodes brightness
 * non-linearly: the naive mean of the black and white bytes is 128, whereas the
 * physically (and visually) correct midtone is about 188.
 */
object Srgb {

    /** Linearises an 8-bit sRGB channel into 0..1. */
    fun toLinear(channel: Int): Double = toLinearUnit(channel / 255.0)

    /** Linearises a 0..1 sRGB value into 0..1 linear light. */
    fun toLinearUnit(value: Double): Double {
        val v = value.coerceIn(0.0, 1.0)
        return if (v <= 0.04045) v / 12.92 else Math.pow((v + 0.055) / 1.055, 2.4)
    }

    /** Encodes a 0..1 linear value back into an 8-bit sRGB channel. */
    fun fromLinear(linear: Double): Int {
        val v = linear.coerceIn(0.0, 1.0)
        val encoded = if (v <= 0.0031308) v * 12.92 else 1.055 * Math.pow(v, 1.0 / 2.4) - 0.055
        return Math.round(encoded * 255.0).toInt().coerceIn(0, 255)
    }
}
