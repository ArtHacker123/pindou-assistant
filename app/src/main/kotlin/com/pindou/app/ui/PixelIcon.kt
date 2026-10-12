package com.pindou.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Draws a pixel-art sprite as fused perler beads directly on a Canvas (no bitmap
 * allocation). Each sprite row is a string; a character maps to a color via
 * [palette] and '.' (or any unmapped character) is transparent. Beads are drawn
 * as rounded cells with a soft top highlight and bottom shade so they read as
 * melted beads. Used for the home action icons.
 */
@Composable
fun PixelIcon(
    sprite: List<String>,
    palette: Map<Char, Color>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        val rows = sprite.size
        if (rows == 0) return@Canvas
        val cols = sprite.maxOf { it.length }
        if (cols == 0) return@Canvas

        val cell = minOf(size.width / cols, size.height / rows)
        val gap = cell * 0.07f
        val bead = cell - gap * 2f
        val radius = CornerRadius(bead * 0.32f, bead * 0.32f)
        val originX = (size.width - cell * cols) / 2f
        val originY = (size.height - cell * rows) / 2f

        sprite.forEachIndexed { r, row ->
            row.forEachIndexed { c, ch ->
                val base = palette[ch] ?: return@forEachIndexed
                val top = originY + r * cell + gap
                val left = originX + c * cell + gap
                val brush = Brush.verticalGradient(
                    colors = listOf(
                        lerp(base, Color.White, 0.38f),
                        base,
                        lerp(base, Color.Black, 0.18f),
                    ),
                    startY = top,
                    endY = top + bead,
                )
                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(left, top),
                    size = Size(bead, bead),
                    cornerRadius = radius,
                )
            }
        }
    }
}
