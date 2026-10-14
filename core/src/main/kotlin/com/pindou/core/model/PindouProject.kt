package com.pindou.core.model

import kotlinx.serialization.Serializable

/** Generation context stored alongside the grid for display and re-generation. */
@Serializable
data class ProjectMeta(
    val gridWidth: Int,
    val gridHeight: Int,
    val imageType: String,
    val paletteName: String,
    val paletteVersion: String,
    val colorCountTarget: Int,
    /** Color-selection strategy id used for generation; mirrors `ColorStrategyRegistry.DEFAULT_ID`. */
    val colorStrategy: String = "reference",
    val createdAt: Long,
    val name: String = "",
    val sourceHash: String? = null,
)

/**
 * The full, self-contained pattern document. This is what gets serialized to a
 * `.pindou` file: it carries the grid plus the palette needed to render and
 * interact with the pattern on another device without the original image.
 */
@Serializable
data class PindouProject(
    val meta: ProjectMeta,
    val paletteSource: Palette,
    val paletteUsed: List<PaletteItem>,
    val grid: Grid,
    /**
     * Optional grid-resolution snapshot of the source colors (one pixel per
     * cell, pre-snap). Enables source-vs-pattern comparison and editing against
     * the original colors, including when the `.pindou` file is opened on
     * another device without the source image.
     */
    val referenceGrid: ReferenceGrid? = null,
    val version: String = CURRENT_VERSION,
) {
    companion object {
        const val CURRENT_VERSION = "1.1.0"
    }
}
