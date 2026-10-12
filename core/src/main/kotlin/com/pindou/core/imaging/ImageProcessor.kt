package com.pindou.core.imaging

import com.pindou.core.model.PixelImage

/** Adjusts the working image to suit a particular kind of source image. */
interface ImageProcessor {
    val id: String
    val displayName: String
    fun process(image: PixelImage): PixelImage
}

/**
 * An [ImageProcessor] defined as an ordered list of [ImageOp]s. This is the
 * composition seam: a preset is a sequence of operations rather than a
 * monolithic class, so new stages (segmentation, background removal, ...) are
 * added by appending an op.
 */
class ProcessorPipeline(
    override val id: String,
    override val displayName: String,
    private val ops: List<ImageOp>,
) : ImageProcessor {
    override fun process(image: PixelImage): PixelImage =
        ops.fold(image) { acc, op -> op.apply(acc) }
}
