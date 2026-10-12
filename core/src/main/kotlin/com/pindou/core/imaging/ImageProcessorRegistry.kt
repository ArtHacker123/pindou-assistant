package com.pindou.core.imaging

/** Built-in image-type presets. New presets only need a new [ImageProcessor]. */
enum class ImageType(val id: String, val displayName: String) {
    CARTOON("cartoon", "卡通"),
    PHOTO("photo", "照片");

    companion object {
        fun fromId(id: String): ImageType = values().firstOrNull { it.id == id } ?: CARTOON
    }
}

/** Resolves image processors by preset id. */
class ImageProcessorRegistry(processors: List<ImageProcessor>) {
    private val byId: Map<String, ImageProcessor> = processors.associateBy { it.id }

    val ids: List<String> get() = byId.keys.toList()

    fun get(id: String): ImageProcessor =
        byId[id] ?: byId.values.firstOrNull()
        ?: error("No image processors registered")

    fun all(): List<ImageProcessor> = byId.values.toList()

    companion object {
        /**
         * The built-in presets, expressed as ordered [ProcessorPipeline]s. The
         * operation order is byte-for-byte the same as the former monolithic
         * `CartoonProcessor`/`PhotoProcessor`.
         */
        fun default(): ImageProcessorRegistry =
            ImageProcessorRegistry(
                listOf(
                    ProcessorPipeline(
                        id = ImageType.CARTOON.id,
                        displayName = ImageType.CARTOON.displayName,
                        ops = listOf(
                            ImageOp { ImageOps.adjustContrast(it, 1.12) },
                            ImageOp { ImageOps.sharpen(it, amount = 0.6) },
                            ImageOp { ImageOps.adjustSaturation(it, factor = 1.08) },
                        ),
                    ),
                    ProcessorPipeline(
                        id = ImageType.PHOTO.id,
                        displayName = ImageType.PHOTO.displayName,
                        ops = listOf(
                            ImageOp { ImageOps.boxBlur(it, radius = 1) },
                            ImageOp { ImageOps.adjustContrast(it, factor = 1.06) },
                            ImageOp { ImageOps.adjustSaturation(it, factor = 1.05) },
                        ),
                    ),
                )
            )
    }
}
