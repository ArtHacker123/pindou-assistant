package com.pindou.core.imaging

import com.pindou.core.color.Srgb
import com.pindou.core.model.Palette
import com.pindou.core.model.PixelImage
import com.pindou.core.model.Rgb
import com.pindou.core.util.NaturalOrder

/** Computes the average (and optionally the representative) color backing a grid cell. */
object AreaColor {

    private data class Region(val left: Int, val top: Int, val maxX: Int, val maxY: Int)

    /**
     * Coverage, the linear-light average, and — when a [PaletteLut] + [Palette]
     * are supplied — the **mode** representative (the most frequent nearest
     * palette color) of a single cell's source region. `repRgb` is always a real
     * palette color rather than a synthesized average.
     */
    data class CellSample(val coverage: Float, val rgb: Rgb, val repRgb: Rgb? = null)

    /**
     * Coverage and average of the source rectangle mapped from grid cell
     * ([row], [col]) in a **single** walk of the region. Invalid pixels
     * (see [PixelImage.valid]) do not contribute to the average, which is done
     * in linear light so mixing light and dark pixels yields the physically
     * correct midtone. Falls back to white, with coverage 0, when the region is
     * empty or entirely invalid.
     *
     * Pass [lut] and [palette] to also histogram each valid pixel's nearest
     * palette entry and return its mode as `repRgb` (ties broken by natural
     * color-code order). The histogram reuses [lut] across every cell, so this
     * costs one extra table lookup per pixel, not a per-cell search.
     */
    fun cell(
        image: PixelImage,
        gridW: Int,
        gridH: Int,
        row: Int,
        col: Int,
        lut: PaletteLut? = null,
        palette: Palette? = null,
    ): CellSample {
        val r = region(image, gridW, gridH, row, col)
        val hist = if (lut != null && palette != null) IntArray(palette.size) else null
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
                    val pr = (p shr 16) and 0xFF
                    val pg = (p shr 8) and 0xFF
                    val pb = p and 0xFF
                    rs += Srgb.toLinear(pr)
                    gs += Srgb.toLinear(pg)
                    bs += Srgb.toLinear(pb)
                    if (hist != null) hist[lut!!.indexOf(pr, pg, pb)]++
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
            repRgb = hist?.let { mode(it, palette!!) },
        )
    }

    /** Most frequent palette index in [hist]; ties go to the smallest natural code. */
    private fun mode(hist: IntArray, palette: Palette): Rgb? {
        var bestIdx = -1
        var bestCount = 0
        for (i in hist.indices) {
            val c = hist[i]
            if (c == 0) continue
            if (c > bestCount ||
                (c == bestCount && NaturalOrder.compare(palette.items[i].code, palette.items[bestIdx].code) < 0)
            ) {
                bestCount = c
                bestIdx = i
            }
        }
        return if (bestIdx >= 0) palette.items[bestIdx].rgb else null
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
