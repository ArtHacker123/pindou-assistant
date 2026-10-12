package com.pindou.app.state

import com.pindou.core.imaging.ImageProcessorRegistry
import com.pindou.core.imaging.ImageType
import com.pindou.core.imaging.ReferenceBuilder
import com.pindou.core.model.PixelImage

/**
 * The shared imaging entry point used by **both** generation and
 * creation-from-reference: it preprocesses a working image with the image-type
 * preset and builds the grid-resolution reference image (one pixel per cell).
 *
 * Because it owns preprocessing, the same preprocessed image can feed the
 * reference and the generator without running the preset twice. Creation can
 * stop after [referenceFrom]; generation continues into [com.pindou.core.generate.PatternGenerator]
 * with the same preprocessed image.
 */
class ReferenceService(
    private val processors: ImageProcessorRegistry = ImageProcessorRegistry.default(),
) {
    /** Applies the image-type preset once, producing the working image. */
    fun preprocess(image: PixelImage, imageType: ImageType): PixelImage =
        processors.get(imageType.id).process(image)

    /** Builds the grid-resolution reference from an already-preprocessed image. */
    fun reference(processed: PixelImage, gridWidth: Int, gridHeight: Int): PixelImage =
        ReferenceBuilder.build(processed, gridWidth, gridHeight)

    /** Convenience for creation: preprocess then build the reference. */
    fun referenceFrom(image: PixelImage, imageType: ImageType, gridWidth: Int, gridHeight: Int): PixelImage =
        reference(preprocess(image, imageType), gridWidth, gridHeight)
}
