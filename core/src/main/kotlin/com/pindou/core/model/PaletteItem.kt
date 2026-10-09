package com.pindou.core.model

import kotlinx.serialization.Serializable

/**
 * A single market bead color.
 *
 * [code] (for example `C12`) is the stable identity used for grid values,
 * cell labels, highlight grouping and cross-device reconstruction. [id] is
 * only informational.
 */
@Serializable
data class PaletteItem(
    val code: String,
    val r: Int,
    val g: Int,
    val b: Int,
    val id: Int = 0,
    val name: String? = null,
    val brand: String? = null,
) {
    val rgb: Rgb get() = Rgb(r, g, b)
}
