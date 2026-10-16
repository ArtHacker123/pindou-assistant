package com.pindou.core.model

/**
 * A simple, Android-independent RGBA image buffer.
 *
 * [pixels] stores packed ARGB ints row-major, so the pixel at (x, y) lives at
 * index `y * width + x`. Keeping this independent of `android.graphics.Bitmap`
 * lets the entire processing pipeline be unit tested on the JVM.
 *
 * [mask] optionally marks the valid/foreground region (true = valid). When
 * `null`, validity falls back to per-pixel transparency (alpha >= 8), so today's
 * transparent padding is invalid by default. A future foreground/background
 * segmentation can supply an explicit mask without touching the pipeline.
 */
class PixelImage(
    val width: Int,
    val height: Int,
    val pixels: IntArray,
    val mask: BooleanArray? = null,
) {
    init {
        require(width > 0 && height > 0) { "Image dimensions must be positive" }
        require(pixels.size >= width * height) { "Pixel buffer is smaller than width*height" }
        require(mask == null || mask.size >= width * height) { "Mask buffer is smaller than width*height" }
    }

    val size: Int get() = width * height

    fun argb(x: Int, y: Int): Int = pixels[y * width + x]

    fun rgb(x: Int, y: Int): Rgb = Rgb.fromArgb(argb(x, y))

    fun alpha(x: Int, y: Int): Int = (argb(x, y) ushr 24) and 0xFF

    /**
     * Returns a copy of this image carrying [mask] as its explicit validity mask
     * (same pixels), used to attach a subject-cutout mask before the pipeline runs.
     */
    fun withMask(mask: BooleanArray?): PixelImage = PixelImage(width, height, pixels, mask)

    /** Whether the pixel at (x, y) belongs to the valid/foreground region. */
    fun valid(x: Int, y: Int): Boolean {
        val m = mask
        if (m != null) return m[y * width + x]
        return alpha(x, y) >= 8
    }

    /**
     * Returns a square image holding this image centered (or top-left aligned)
     * over a [fillArgb] background. No-op when already square, so cells outside
     * the original content are exactly [fillArgb]; the pipeline ignores it via
     * transparency-aware sampling/averaging when [fillArgb] is transparent.
     */
    fun paddedToSquare(center: Boolean = true, fillArgb: Int = 0x00000000): PixelImage {
        val size = maxOf(width, height)
        if (width == size && height == size) return this
        val out = IntArray(size * size) { fillArgb }
        val outMask = mask?.let { BooleanArray(size * size) }
        val dx = if (center) (size - width) / 2 else 0
        val dy = if (center) (size - height) / 2 else 0
        for (y in 0 until height) {
            val dst = (y + dy) * size + dx
            System.arraycopy(pixels, y * width, out, dst, width)
            if (outMask != null) {
                System.arraycopy(mask, y * width, outMask, dst, width)
            }
        }
        return PixelImage(size, size, out, outMask)
    }

    companion object {
        /** Fills a new image with a single color, convenient for tests. */
        fun filled(width: Int, height: Int, argb: Int): PixelImage =
            PixelImage(width, height, IntArray(width * height) { argb })
    }
}
