package com.pindou.core.imaging

import com.pindou.core.model.PixelImage
import kotlin.math.roundToInt

/**
 * Pure pixel operations used by the image-type presets. Kept free of Android so
 * they can be unit tested on the JVM.
 */
object ImageOps {

    fun adjustContrast(image: PixelImage, factor: Double): PixelImage {
        if (factor == 1.0) return image
        val out = IntArray(image.pixels.size)
        val table = IntArray(256) { v ->
            (((v - 128) * factor) + 128).roundToInt().coerceIn(0, 255)
        }
        for (i in image.pixels.indices) out[i] = mapRgb(image.pixels[i], table)
        return PixelImage(image.width, image.height, out, image.mask)
    }

    fun adjustSaturation(image: PixelImage, factor: Double): PixelImage {
        if (factor == 1.0) return image
        val out = IntArray(image.pixels.size)
        for (i in image.pixels.indices) {
            val p = image.pixels[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            val gray = 0.299 * r + 0.587 * g + 0.114 * b
            val nr = (gray + (r - gray) * factor).roundToInt().coerceIn(0, 255)
            val ng = (gray + (g - gray) * factor).roundToInt().coerceIn(0, 255)
            val nb = (gray + (b - gray) * factor).roundToInt().coerceIn(0, 255)
            out[i] = (p and 0xFF000000.toInt()) or (nr shl 16) or (ng shl 8) or nb
        }
        return PixelImage(image.width, image.height, out, image.mask)
    }

    /** Box blur used for light denoising of photos. [radius] >= 1. */
    fun boxBlur(image: PixelImage, radius: Int): PixelImage {
        require(radius >= 1) { "radius must be >= 1" }
        val w = image.width
        val h = image.height
        val out = IntArray(image.pixels.size)
        for (y in 0 until h) {
            for (x in 0 until w) {
                var rs = 0L
                var gs = 0L
                var bs = 0L
                var n = 0
                for (dy in -radius..radius) {
                    val yy = (y + dy).coerceIn(0, h - 1)
                    for (dx in -radius..radius) {
                        val xx = (x + dx).coerceIn(0, w - 1)
                        if (!image.valid(xx, yy)) continue
                        val p = image.pixels[yy * w + xx]
                        rs += (p shr 16) and 0xFF
                        gs += (p shr 8) and 0xFF
                        bs += p and 0xFF
                        n++
                    }
                }
                val a = image.pixels[y * w + x] and 0xFF000000.toInt()
                out[y * w + x] = if (n == 0) a else a or
                    (((rs / n).toInt()) shl 16) or
                    (((gs / n).toInt()) shl 8) or
                    ((bs / n).toInt())
            }
        }
        return PixelImage(w, h, out, image.mask)
    }

    /** Unsharp mask: pushes pixels away from a blurred copy to crisp edges. */
    fun sharpen(image: PixelImage, amount: Double): PixelImage {
        if (amount <= 0.0) return image
        val blurred = boxBlur(image, 1)
        val out = IntArray(image.pixels.size)
        for (i in image.pixels.indices) {
            val p = image.pixels[i]
            val b = blurred.pixels[i]
            val pr = (p shr 16) and 0xFF
            val pg = (p shr 8) and 0xFF
            val pb = p and 0xFF
            val br = (b shr 16) and 0xFF
            val bg = (b shr 8) and 0xFF
            val bb = b and 0xFF
            val nr = (pr + amount * (pr - br)).roundToInt().coerceIn(0, 255)
            val ng = (pg + amount * (pg - bg)).roundToInt().coerceIn(0, 255)
            val nb = (pb + amount * (pb - bb)).roundToInt().coerceIn(0, 255)
            out[i] = (p and 0xFF000000.toInt()) or (nr shl 16) or (ng shl 8) or nb
        }
        return PixelImage(image.width, image.height, out, image.mask)
    }

    private fun mapRgb(argb: Int, table: IntArray): Int {
        val a = argb and 0xFF000000.toInt()
        val r = table[(argb shr 16) and 0xFF]
        val g = table[(argb shr 8) and 0xFF]
        val b = table[argb and 0xFF]
        return a or (r shl 16) or (g shl 8) or b
    }
}
