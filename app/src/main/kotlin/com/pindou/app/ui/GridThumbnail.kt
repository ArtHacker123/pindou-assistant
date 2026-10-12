package com.pindou.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.pindou.core.model.PindouProject

private val EMPTY_BG = Color(0xFFF5F5F5)

/**
 * Draws a pattern's grid as a small thumbnail directly on a Canvas (no bitmap
 * allocation). The grid is centered and scaled to fit; empty cells use a light
 * background. Used by the home "recent" row and the history grid.
 */
@Composable
fun GridThumbnail(project: PindouProject?, modifier: Modifier = Modifier) {
    Box(modifier) {
        if (project == null) return@Box
        val colorByCode = remember(project) {
            project.paletteSource.items.associate { it.code to Color(it.rgb.toArgb()) }
        }
        Canvas(Modifier.fillMaxSize()) {
            val grid = project.grid
            val cell = minOf(size.width / grid.width, size.height / grid.height)
            val ox = (size.width - cell * grid.width) / 2f
            val oy = (size.height - cell * grid.height) / 2f
            drawRect(EMPTY_BG, Offset(ox, oy), Size(cell * grid.width, cell * grid.height))
            for (r in 0 until grid.height) {
                for (c in 0 until grid.width) {
                    val code = grid[r, c] ?: continue
                    drawRect(
                        color = colorByCode[code] ?: EMPTY_BG,
                        topLeft = Offset(ox + c * cell, oy + r * cell),
                        size = Size(cell, cell),
                    )
                }
            }
        }
    }
}
