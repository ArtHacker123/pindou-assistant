package com.pindou.core.model

import kotlinx.serialization.Serializable

/**
 * A full market bead palette. Multiple palettes may be bundled; the user picks
 * one, and it becomes the target pool for mapping representative image colors.
 */
@Serializable
data class Palette(
    val name: String,
    val items: List<PaletteItem>,
    val version: String = "1.0.0",
    val displayName: String = name,
) {
    private val byCode: Map<String, PaletteItem> by lazy { items.associateBy { it.code } }

    val size: Int get() = items.size

    fun item(code: String): PaletteItem? = byCode[code]
}
