package com.pindou.core.processor

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
        fun default(): ImageProcessorRegistry =
            ImageProcessorRegistry(listOf(CartoonProcessor(), PhotoProcessor()))
    }
}
