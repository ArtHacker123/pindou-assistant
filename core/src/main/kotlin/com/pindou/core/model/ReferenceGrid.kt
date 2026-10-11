package com.pindou.core.model

import kotlinx.serialization.Serializable

/**
 * A compact, serializable snapshot of the grid-resolution reference image: one
 * ARGB pixel per grid cell, stored row-major as concatenated 8-digit hex
 * (`"00000000"` = fully transparent, i.e. an empty hole).
 *
 * Kept as hex rather than bytes so the core module stays Android-independent
 * (`java.util.Base64` needs API 26 while `minSdk` is 24) and the `.pindou`
 * payload stays human-inspectable, matching how the bead grid is stored.
 */
@Serializable
data class ReferenceGrid(
    val width: Int,
    val height: Int,
    val argbHex: String,
) {
    init {
        require(width > 0 && height > 0) { "Reference dimensions must be positive" }
        require(argbHex.length == width * height * 8) {
            "argbHex length must equal width*height*8"
        }
    }

    /** Decodes back into an in-memory pixel buffer for rendering. */
    fun toImage(): PixelImage {
        val pixels = IntArray(width * height) { i ->
            argbHex.substring(i * 8, i * 8 + 8).toLong(16).toInt()
        }
        return PixelImage(width, height, pixels)
    }

    companion object {
        /** Encodes a grid-resolution pixel buffer into its hex form. */
        fun fromImage(image: PixelImage): ReferenceGrid {
            val sb = StringBuilder(image.size * 8)
            for (p in image.pixels) {
                sb.append(Integer.toHexString(p).padStart(8, '0'))
            }
            return ReferenceGrid(image.width, image.height, sb.toString())
        }
    }
}

/** Encodes this grid-resolution image as a [ReferenceGrid]. */
fun PixelImage.toReferenceGrid(): ReferenceGrid = ReferenceGrid.fromImage(this)
