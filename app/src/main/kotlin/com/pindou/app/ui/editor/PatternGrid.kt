package com.pindou.app.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import com.pindou.core.model.PindouProject
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

/**
 * The Form 2 interactive canvas: a pan/zoom grid that draws only the visible
 * cells (viewport culling) and outlines every cell matching [selectedCode].
 */
@Composable
fun PatternGrid(
    project: PindouProject,
    selectedCode: String?,
    onCellTap: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val grid = project.grid
    val colorByCode = remember(project) {
        project.paletteSource.items.associate { it.code to it.rgb.toArgb() }
    }
    val inkByCode = remember(project) {
        project.paletteSource.items.associate {
            it.code to if (it.rgb.luminance() >= 140) Color.Black else Color.White
        }
    }

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val textCache = remember(project) { HashMap<String, TextLayoutResult>() }

    var fitScale by remember { mutableFloatStateOf(1f) }
    var scale by remember { mutableFloatStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var container by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(container, grid.width, grid.height) {
        if (container.width > 0 && container.height > 0) {
            val fit = minOf(
                container.width.toFloat() / grid.width,
                container.height.toFloat() / grid.height,
            ) * 0.95f
            fitScale = fit.coerceAtLeast(1f)
            scale = fitScale
            offset = Offset(
                (container.width - grid.width * scale) / 2f,
                (container.height - grid.height * scale) / 2f,
            )
        }
    }

    val minScale = fitScale * 0.6f
    val maxScale = max(fitScale * 8f, 60f)

    Canvas(
        modifier = modifier
            .onSizeChanged { container = it }
            .pointerInput(grid.width, grid.height) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(minScale, maxScale)
                    val k = newScale / scale
                    offset = Offset(
                        centroid.x - (centroid.x - offset.x) * k,
                        centroid.y - (centroid.y - offset.y) * k,
                    ) + pan
                    scale = newScale
                }
            }
            .pointerInput(grid.width, grid.height) {
                detectTapGestures { pos ->
                    val col = floor((pos.x - offset.x) / scale).toInt()
                    val row = floor((pos.y - offset.y) / scale).toInt()
                    onCellTap(row, col)
                }
            },
    ) {
        val cell = scale
        if (cell <= 0f) return@Canvas

        val c0 = floor((0f - offset.x) / cell).toInt().coerceAtLeast(0)
        val c1 = ceil((size.width - offset.x) / cell).toInt().coerceIn(0, grid.width)
        val r0 = floor((0f - offset.y) / cell).toInt().coerceAtLeast(0)
        val r1 = ceil((size.height - offset.y) / cell).toInt().coerceIn(0, grid.height)

        for (r in r0 until r1) {
            for (c in c0 until c1) {
                val code = grid[r, c]
                val x = offset.x + c * cell
                val y = offset.y + r * cell
                drawRect(
                    color = Color(colorByCode[code] ?: 0xFFFFFFFF.toInt()),
                    topLeft = Offset(x, y),
                    size = Size(cell, cell),
                )
                if (selectedCode != null && code == selectedCode) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(x, y),
                        size = Size(cell, cell),
                        style = Stroke(width = max(2f, cell * 0.09f)),
                    )
                }
            }
        }

        if (cell >= 6f) {
            val lineColor = Color(0x55000000)
            val stroke = max(1f, cell * 0.03f)
            for (c in c0..c1) {
                val x = offset.x + c * cell
                drawLine(lineColor, Offset(x, offset.y + r0 * cell), Offset(x, offset.y + r1 * cell), strokeWidth = stroke)
            }
            for (r in r0..r1) {
                val y = offset.y + r * cell
                drawLine(lineColor, Offset(offset.x + c0 * cell, y), Offset(offset.x + c1 * cell, y), strokeWidth = stroke)
            }
        }

        if (cell >= 24f) {
            for (r in r0 until r1) {
                for (c in c0 until c1) {
                    val code = grid[r, c]
                    val textPx = cell * 0.32f
                    val key = "$code#${textPx.toInt()}"
                    val layout = textCache.getOrPut(key) {
                        textMeasurer.measure(
                            text = AnnotatedString(code),
                            style = TextStyle(
                                color = inkByCode[code] ?: Color.Black,
                                fontSize = with(density) { textPx.toSp() },
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }
                    val x = offset.x + c * cell + (cell - layout.size.width) / 2f
                    val y = offset.y + r * cell + (cell - layout.size.height) / 2f
                    drawText(layout, topLeft = Offset(x, y))
                }
            }
        }
    }
}
