package com.pindou.core.generate

import com.pindou.core.model.PixelImage
import com.pindou.core.model.Rgb

/** Computes the average color of the source region backing a grid cell. */
object AreaColor {
    /**
     * Average RGB over the source rectangle mapped from grid cell ([row], [col]).
     * Fully transparent pixels are ignored. Falls back to white if the region is
     * entirely transparent.
     */
    fun average(image: PixelImage, gridW: Int, gridH: Int, row: Int, col: Int): Rgb {
        val left = (col.toLong() * image.width / gridW).toInt()
        val right = (((col + 1).toLong() * image.width / gridW).toInt()).coerceAtLeast(left + 1)
        val top = (row.toLong() * image.height / gridH).toInt()
        val bottom = (((row + 1).toLong() * image.height / gridH).toInt()).coerceAtLeast(top + 1)

        var rs = 0L
        var gs = 0L
        var bs = 0L
        var n = 0L
        val maxX = right.coerceAtMost(image.width)
        val maxY = bottom.coerceAtMost(image.height)
        for (y in top until maxY) {
            var index = y * image.width + left
            for (x in left until maxX) {
                val p = image.pixels[index]
                if (((p ushr 24) and 0xFF) >= 8) {
                    rs += (p shr 16) and 0xFF
                    gs += (p shr 8) and 0xFF
                    bs += p and 0xFF
                    n++
                }
                index++
            }
        }
        return if (n == 0L) Rgb(255, 255, 255)
        else Rgb((rs / n).toInt(), (gs / n).toInt(), (bs / n).toInt())
    }
}
