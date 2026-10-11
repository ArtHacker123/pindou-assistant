package com.pindou.core.generate

import com.pindou.core.color.Srgb
import com.pindou.core.model.PixelImage
import com.pindou.core.model.Rgb

/** Computes the average color of the source region backing a grid cell. */
object AreaColor {

    private data class Region(val left: Int, val top: Int, val maxX: Int, val maxY: Int)

    /** Coverage plus the linear-light average of a single cell's source region. */
    data class CellSample(val coverage: Float, val rgb: Rgb)

    /**
     * Coverage and average of the source rectangle mapped from grid cell
     * ([row], [col]) in a **single** walk of the region. Equivalent to calling
     * [coverage] and [average] together, but cheaper, and the natural place to
     * build the grid-resolution reference image.
     */
    fun cell(image: PixelImage, gridW: Int, gridH: Int, row: Int, col: Int): CellSample {
        val r = region(image, gridW, gridH, row, col)
        var rs = 0.0
        var gs = 0.0
        var bs = 0.0
        var valid = 0
        var total = 0
        for (y in r.top until r.maxY) {
            var index = y * image.width + r.left
            for (x in r.left until r.maxX) {
                total++
                if (image.valid(x, y)) {
                    val p = image.pixels[index]
                    rs += Srgb.toLinear((p shr 16) and 0xFF)
                    gs += Srgb.toLinear((p shr 8) and 0xFF)
                    bs += Srgb.toLinear(p and 0xFF)
                    valid++
                }
                index++
            }
        }
        if (total == 0 || valid == 0) {
            return CellSample(if (total == 0) 0f else valid.toFloat() / total, Rgb(255, 255, 255))
        }
        return CellSample(
            coverage = valid.toFloat() / total,
            rgb = Rgb(
                Srgb.fromLinear(rs / valid),
                Srgb.fromLinear(gs / valid),
                Srgb.fromLinear(bs / valid),
            ),
        )
    }

    /**
     * Fraction of valid pixels within the source rectangle mapped from grid cell
     * ([row], [col]). A cell is an empty hole if this drops below the generation
     * threshold (e.g. `PatternGenerator.MIN_CELL_VALID`).
     */
    fun coverage(image: PixelImage, gridW: Int, gridH: Int, row: Int, col: Int): Float {
        val r = region(image, gridW, gridH, row, col)
        var valid = 0
        var total = 0
        for (y in r.top until r.maxY) {
            var index = y * image.width + r.left
            for (x in r.left until r.maxX) {
                total++
                if (image.valid(x, y)) valid++
                index++
            }
        }
        if (total == 0) return 0f
        return valid.toFloat() / total
    }

    /**
     * Average RGB over the source rectangle mapped from grid cell ([row], [col]).
     * Invalid pixels (see [PixelImage.valid]) are ignored. Averaging is done in
     * linear light so mixing light and dark pixels yields the physically correct
     * midtone instead of a too-dark sRGB mean. Falls back to white if the region
     * is entirely invalid.
     */
    fun average(image: PixelImage, gridW: Int, gridH: Int, row: Int, col: Int): Rgb {
        val r = region(image, gridW, gridH, row, col)
        var rs = 0.0
        var gs = 0.0
        var bs = 0.0
        var n = 0L
        for (y in r.top until r.maxY) {
            var index = y * image.width + r.left
            for (x in r.left until r.maxX) {
                if (image.valid(x, y)) {
                    val p = image.pixels[index]
                    rs += Srgb.toLinear((p shr 16) and 0xFF)
                    gs += Srgb.toLinear((p shr 8) and 0xFF)
                    bs += Srgb.toLinear(p and 0xFF)
                    n++
                }
                index++
            }
        }
        return if (n == 0L) Rgb(255, 255, 255)
        else Rgb(
            Srgb.fromLinear(rs / n),
            Srgb.fromLinear(gs / n),
            Srgb.fromLinear(bs / n),
        )
    }

    private fun region(image: PixelImage, gridW: Int, gridH: Int, row: Int, col: Int): Region {
        val left = (col.toLong() * image.width / gridW).toInt()
        val right = (((col + 1).toLong() * image.width / gridW).toInt()).coerceAtLeast(left + 1)
        val top = (row.toLong() * image.height / gridH).toInt()
        val bottom = (((row + 1).toLong() * image.height / gridH).toInt()).coerceAtLeast(top + 1)
        return Region(
            left = left,
            top = top,
            maxX = right.coerceAtMost(image.width),
            maxY = bottom.coerceAtMost(image.height),
        )
    }
}