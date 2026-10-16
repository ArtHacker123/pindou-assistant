package com.pindou.core.imaging

import com.pindou.core.model.PixelImage
import kotlin.math.floor

/**
 * Pure preprocessing for the `u2netp` saliency model, kept in `core` (no Android)
 * so it is unit-testable on the JVM.
 *
 * Input: resize the working image to a fixed square (aspect not preserved), scale
 * to `0..1`, divide by the image's own maximum, then ImageNet-normalize — the
 * contract published for this model. Output: min-max normalize the saliency map
 * and bilinearly resample it back to the working resolution.
 */
object SaliencyPreprocess {

    const val INPUT_SIZE = 320
    val MEAN = floatArrayOf(0.485f, 0.456f, 0.406f)
    val STD = floatArrayOf(0.229f, 0.224f, 0.225f)

    private const val EPSILON = 1e-6f

    /**
     * Builds the `[1, 3, size, size]` NCHW RGB input for the model from [image],
     * as a flat array in channel-major order (`c * size * size + y * size + x`).
     */
    fun toModelInput(image: PixelImage, size: Int = INPUT_SIZE): FloatArray {
        require(size > 0) { "Model input size must be positive" }
        val n = size * size
        val rgb = FloatArray(3 * n)
        val w = image.width
        val h = image.height
        for (y in 0 until size) {
            val sy = sampleCoordinate(y, size, h)
            for (x in 0 until size) {
                val sx = sampleCoordinate(x, size, w)
                val idx = y * size + x
                sampleRgb(image, sx, sy, rgb, idx, n)
            }
        }
        var max = 0f
        for (v in rgb) if (v > max) max = v
        val denom = if (max > EPSILON) max else 1f
        for (c in 0 until 3) {
            val mean = MEAN[c]
            val std = STD[c]
            val base = c * n
            for (i in 0 until n) {
                rgb[base + i] = (rgb[base + i] / denom - mean) / std
            }
        }
        return rgb
    }

    /**
     * Turns the model's `side × side` saliency map into a working-resolution
     * [FloatMask]: contrast-stretched to `0..1` and bilinearly upsampled.
     */
    fun toWorkingMask(saliency: FloatArray, side: Int, width: Int, height: Int): FloatMask {
        require(side > 0) { "Saliency side must be positive" }
        require(saliency.size >= side * side) { "Saliency buffer is smaller than side*side" }
        val m = side * side
        val min = (0 until m).minOf { saliency[it] }
        val max = (0 until m).maxOf { saliency[it] }
        val range = max - min
        val normalized = FloatArray(m) { i ->
            if (range > EPSILON) (saliency[i] - min) / range else saliency[i]
        }
        val out = FloatArray(width * height)
        for (y in 0 until height) {
            val sy = sampleCoordinate(y, height, side)
            for (x in 0 until width) {
                val sx = sampleCoordinate(x, width, side)
                out[y * width + x] = sampleField(normalized, side, side, sx, sy)
            }
        }
        return FloatMask(width, height, out)
    }

    /** Maps a destination index to source coordinates (pixel-center sampling). */
    private fun sampleCoordinate(dst: Int, dstSize: Int, srcSize: Int): Float =
        ((dst + 0.5f) * srcSize / dstSize - 0.5f).coerceIn(0f, (srcSize - 1).toFloat())

    private fun sampleRgb(image: PixelImage, sx: Float, sy: Float, out: FloatArray, outIndex: Int, stride: Int) {
        val x0 = floor(sx).toInt()
        val y0 = floor(sy).toInt()
        val x1 = (x0 + 1).coerceAtMost(image.width - 1)
        val y1 = (y0 + 1).coerceAtMost(image.height - 1)
        val dx = sx - x0
        val dy = sy - y0
        val w00 = (1 - dx) * (1 - dy)
        val w10 = dx * (1 - dy)
        val w01 = (1 - dx) * dy
        val w11 = dx * dy
        val p00 = image.argb(x0, y0)
        val p10 = image.argb(x1, y0)
        val p01 = image.argb(x0, y1)
        val p11 = image.argb(x1, y1)
        out[outIndex] = blend(p00, p10, p01, p11, w00, w10, w01, w11, 16) / 255f
        out[outIndex + stride] = blend(p00, p10, p01, p11, w00, w10, w01, w11, 8) / 255f
        out[outIndex + 2 * stride] = blend(p00, p10, p01, p11, w00, w10, w01, w11, 0) / 255f
    }

    private fun blend(
        p00: Int,
        p10: Int,
        p01: Int,
        p11: Int,
        w00: Float,
        w10: Float,
        w01: Float,
        w11: Float,
        shift: Int,
    ): Float {
        val c00 = (p00 ushr shift) and 0xFF
        val c10 = (p10 ushr shift) and 0xFF
        val c01 = (p01 ushr shift) and 0xFF
        val c11 = (p11 ushr shift) and 0xFF
        return c00 * w00 + c10 * w10 + c01 * w01 + c11 * w11
    }

    private fun sampleField(field: FloatArray, width: Int, height: Int, sx: Float, sy: Float): Float {
        val x0 = floor(sx).toInt()
        val y0 = floor(sy).toInt()
        val x1 = (x0 + 1).coerceAtMost(width - 1)
        val y1 = (y0 + 1).coerceAtMost(height - 1)
        val dx = sx - x0
        val dy = sy - y0
        val v00 = field[y0 * width + x0]
        val v10 = field[y0 * width + x1]
        val v01 = field[y1 * width + x0]
        val v11 = field[y1 * width + x1]
        val top = v00 + (v10 - v00) * dx
        val bottom = v01 + (v11 - v01) * dx
        return top + (bottom - top) * dy
    }
}
