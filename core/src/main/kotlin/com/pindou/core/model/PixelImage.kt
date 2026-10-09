package com.pindou.core.model

/**
 * A simple, Android-independent RGBA image buffer.
 *
 * [pixels] stores packed ARGB ints row-major, so the pixel at (x, y) lives at
 * index `y * width + x`. Keeping this independent of `android.graphics.Bitmap`
 * lets the entire processing pipeline be unit tested on the JVM.
 */
class PixelImage(
    val width: Int,
    val height: Int,
    val pixels: IntArray,
) {
    init {
        require(width > 0 && height > 0) { "Image dimensions must be positive" }
        require(pixels.size >= width * height) { "Pixel buffer is smaller than width*height" }
    }

    val size: Int get() = width * height

    fun argb(x: Int, y: Int): Int = pixels[y * width + x]

    fun rgb(x: Int, y: Int): Rgb = Rgb.fromArgb(argb(x, y))

    fun alpha(x: Int, y: Int): Int = (argb(x, y) ushr 24) and 0xFF

    companion object {
        /** Fills a new image with a single color, convenient for tests. */
        fun filled(width: Int, height: Int, argb: Int): PixelImage =
            PixelImage(width, height, IntArray(width * height) { argb })
    }
}
