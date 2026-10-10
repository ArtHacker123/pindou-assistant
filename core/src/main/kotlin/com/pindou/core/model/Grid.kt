package com.pindou.core.model

import kotlinx.serialization.Serializable

/**
 * A dense [width] x [height] grid of bead color codes stored row-major in
 * [cells]. `null` marks a cell with **no bead** (an empty / invalid region:
 * padded margins, or future background segmentation). The origin (0, 0) is the
 * top-left cell, matching the user-facing coordinate (row 1, column 1).
 */
@Serializable
data class Grid(
    val width: Int,
    val height: Int,
    val cells: List<String?>,
) {
    init {
        require(width > 0 && height > 0) { "Grid dimensions must be positive" }
        require(cells.size == width * height) { "cells size must equal width*height" }
    }

    operator fun get(row: Int, col: Int): String? = cells[row * width + col]

    /** Distinct bead color codes appearing in the grid, in first-seen order. */
    fun distinctCodes(): List<String> = LinkedHashSet(cells.filterNotNull()).toList()

    companion object {
        fun of(width: Int, height: Int, cells: List<String?>): Grid = Grid(width, height, cells)
    }
}
