package com.pindou.core.imaging

import com.pindou.core.model.PixelImage

/**
 * A single, composable image transformation. Preprocessing presets are ordered
 * lists of these, so new stages can be added without touching the pipeline.
 */
fun interface ImageOp {
    fun apply(image: PixelImage): PixelImage
}
