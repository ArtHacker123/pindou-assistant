package com.pindou.core.processor

import com.pindou.core.model.PixelImage

/** Adjusts the working image to suit a particular kind of source image. */
interface ImageProcessor {
    val id: String
    val displayName: String
    fun process(image: PixelImage): PixelImage
}

/** Presets tuned for flat, hard-edged artwork: emphasize blocks and edges. */
class CartoonProcessor : ImageProcessor {
    override val id: String = ImageType.CARTOON.id
    override val displayName: String = ImageType.CARTOON.displayName

    override fun process(image: PixelImage): PixelImage =
        ImageOps.adjustSaturation(
            ImageOps.sharpen(
                ImageOps.adjustContrast(image, 1.12),
                amount = 0.6,
            ),
            factor = 1.08,
        )
}

/** Presets tuned for photos: light denoise, gentle contrast/saturation. */
class PhotoProcessor : ImageProcessor {
    override val id: String = ImageType.PHOTO.id
    override val displayName: String = ImageType.PHOTO.displayName

    override fun process(image: PixelImage): PixelImage =
        ImageOps.adjustSaturation(
            ImageOps.adjustContrast(
                ImageOps.boxBlur(image, radius = 1),
                factor = 1.06,
            ),
            factor = 1.05,
        )
}
