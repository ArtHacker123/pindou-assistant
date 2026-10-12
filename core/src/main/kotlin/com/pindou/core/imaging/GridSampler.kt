package com.pindou.core.imaging

import com.pindou.core.model.PixelImage
import com.pindou.core.model.Rgb

/**
 * The per-cell samples backing both pattern generation and the grid-resolution
 * reference image: the linear-light average color of each cell's source region,
 * how much of that region was valid, and whether the cell survives the validity
 * threshold as a real cell (as opposed to an empty hole).
 *
 * Kept as parallel arrays so a whole grid can be sampled once and consumed by
 * several stages (snapping, reference emission) without re-walking the image.
 */
class GridSamples(
    val gridWidth: Int,
    val gridHeight: Int,
    val coverage: FloatArray,
    val avgRgb: Array<Rgb>,
    val valid: BooleanArray,
) {
    val size: Int get() = gridWidth * gridHeight
}

/**
 * Maps an image onto a grid in a single pass, producing the [GridSamples] used
 * by both cell snapping and the reference image. This is the single source of
 * truth for the per-cell walk [AreaColor.cell] used to duplicate.
 */
object GridSampler {

    /** A cell is an empty hole when its source region is less than this fraction valid. */
    const val MIN_CELL_VALID = 0.5f

    /** Samples every cell of [image] on a [gridWidth] x [gridHeight] grid. */
    fun sample(image: PixelImage, gridWidth: Int, gridHeight: Int): GridSamples {
        val n = gridWidth * gridHeight
        val coverage = FloatArray(n)
        val avg = Array(n) { Rgb(255, 255, 255) }
        val valid = BooleanArray(n)
        for (row in 0 until gridHeight) {
            for (col in 0 until gridWidth) {
                val i = row * gridWidth + col
                val sample = AreaColor.cell(image, gridWidth, gridHeight, row, col)
                coverage[i] = sample.coverage
                avg[i] = sample.rgb
                valid[i] = sample.coverage >= MIN_CELL_VALID
            }
        }
        return GridSamples(gridWidth, gridHeight, coverage, avg, valid)
    }

    /**
     * Recovers [GridSamples] from a previously emitted grid-resolution reference
     * (one pixel per cell; alpha 0 marks a hole). Reusing the reference lets a
     * regeneration skip the per-cell averaging pass while snapping to exactly the
     * same average colors.
     */
    fun fromReference(reference: PixelImage): GridSamples {
        val n = reference.size
        val coverage = FloatArray(n)
        val avg = Array(n) { Rgb(255, 255, 255) }
        val valid = BooleanArray(n)
        for (i in 0 until n) {
            val argb = reference.pixels[i]
            val opaque = ((argb ushr 24) and 0xFF) >= 8
            valid[i] = opaque
            coverage[i] = if (opaque) 1f else 0f
            if (opaque) avg[i] = Rgb.fromArgb(argb)
        }
        return GridSamples(reference.width, reference.height, coverage, avg, valid)
    }
}
