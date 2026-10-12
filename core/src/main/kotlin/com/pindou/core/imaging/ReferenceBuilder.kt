package com.pindou.core.imaging

import com.pindou.core.model.PixelImage

/**
 * Produces the grid-resolution reference image (one pixel per cell): the
 * linear-light average color of each cell's source region, with alpha 0 for
 * holes. Independent of quantization and snapping, so creation-from-reference
 * can build a reference on its own.
 */
object ReferenceBuilder {

    /** Builds the reference for [image] on a [gridWidth] x [gridHeight] grid. */
    fun build(image: PixelImage, gridWidth: Int, gridHeight: Int): PixelImage =
        build(GridSampler.sample(image, gridWidth, gridHeight))

    /** Emits the reference pixels for already-computed [samples]. */
    fun build(samples: GridSamples): PixelImage {
        val pixels = IntArray(samples.size) { i ->
            if (samples.valid[i]) samples.avgRgb[i].toArgb() else 0
        }
        return PixelImage(samples.gridWidth, samples.gridHeight, pixels)
    }
}
