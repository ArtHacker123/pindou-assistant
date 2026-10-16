package com.pindou.core.imaging

/**
 * A soft foreground mask: one value in `0..1` per pixel, held at the
 * working-image resolution.
 *
 * This is the currency of the [MaskSource] seam. Class-agnostic saliency returns
 * a soft probability map, and a later semantic/instance source can compose into
 * it with [union] / [intersect] / [difference]. Attach it to a
 * [com.pindou.core.model.PixelImage] via [toBooleanMask] when the pipeline needs
 * a hard validity mask.
 */
class FloatMask(
    val width: Int,
    val height: Int,
    val values: FloatArray,
) {
    init {
        require(width > 0 && height > 0) { "Mask dimensions must be positive" }
        require(values.size >= width * height) { "Mask buffer is smaller than width*height" }
    }

    val size: Int get() = width * height

    operator fun get(x: Int, y: Int): Float = values[y * width + x]

    /** Hard validity mask: a pixel is valid when its value is at least [threshold]. */
    fun toBooleanMask(threshold: Float = 0.5f): BooleanArray =
        BooleanArray(size) { values[it] >= threshold }

    fun union(other: FloatMask): FloatMask = combine(other) { a, b -> maxOf(a, b) }

    fun intersect(other: FloatMask): FloatMask = combine(other) { a, b -> minOf(a, b) }

    fun difference(other: FloatMask): FloatMask = combine(other) { a, b -> (a - b).coerceAtLeast(0f) }

    private inline fun combine(other: FloatMask, op: (Float, Float) -> Float): FloatMask {
        require(width == other.width && height == other.height) { "Mask dimensions must match" }
        return FloatMask(width, height, FloatArray(size) { op(values[it], other.values[it]) })
    }

    companion object {
        fun filled(width: Int, height: Int, value: Float): FloatMask =
            FloatMask(width, height, FloatArray(width * height) { value })
    }
}
