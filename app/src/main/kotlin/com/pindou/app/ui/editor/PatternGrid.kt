package com.pindou.app.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
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
import com.pindou.core.model.PixelImage
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

private const val MAX_CELL_PX = 128f
private const val GROUP = 10
private const val HALF_GROUP = GROUP / 2
private const val EMPTY_ALPHA = 8
private val GROUP_CHIP = Color(0xECFFFFFF)
private val SPOTLIGHT_DIM = Color(0x8C000000)
private val SELECT_ACCENT = Color(0xFFFFA000)
private val EMPTY_CELL_BG = Color(0xFFF5F5F5)
private val EMPTY_CELL_LINES = Color(0x59000000)

private fun DrawScope.drawEmptyCell(x: Float, y: Float, cell: Float) {
    drawRect(color = EMPTY_CELL_BG, topLeft = Offset(x, y), size = Size(cell, cell))
    if (cell >= 8f) {
        val hatch = max(1f, cell * 0.05f)
        drawLine(EMPTY_CELL_LINES, Offset(x, y), Offset(x + cell, y + cell), strokeWidth = hatch)
        drawLine(EMPTY_CELL_LINES, Offset(x + cell, y), Offset(x, y + cell), strokeWidth = hatch)
    }
}

/**
 * The Form 2 interactive canvas: a pan/zoom grid that draws only the visible
 * cells (viewport culling), with a thick guide every 10 cells and a finer
 * guide every 5 cells. When [selectedCode] is set, cells of that code get an
 * inset accent outline while cells of other codes get dimmed by a black
 * spotlight veil.
 *
 * [showReference] paints the grid-resolution [reference] as a base layer under
 * every cell (empty holes stay transparent); [showPattern] then paints the bead
 * layer opaquely on top, so the reference remains visible only where there is
 * no bead — the model used for editing/creating a pattern over a reference.
 */
@Composable
fun PatternGrid(
    project: PindouProject,
    selectedCode: String?,
    selectedPos: Pair<Int, Int>?,
    onCellTap: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier,
    reference: PixelImage? = null,
    showPattern: Boolean = true,
    showReference: Boolean = false,
    editMode: Boolean = false,
    moveMode: Boolean = false,
    onEdit: (row: Int, col: Int) -> Unit = { _, _ -> },
    onEditStart: () -> Unit = {},
    onEditEnd: () -> Unit = {},
    highlightCell: Pair<Int, Int>? = null,
    onEditCursor: (row: Int, col: Int, active: Boolean) -> Unit = { _, _, _ -> },
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
    val cursorColor = MaterialTheme.colorScheme.primary
    val textCache = remember(project) { HashMap<String, TextLayoutResult>() }

    var container by remember { mutableStateOf(IntSize.Zero) }
    var fitScale by remember { mutableFloatStateOf(1f) }
    var scale by remember { mutableFloatStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var refitKey by remember { mutableStateOf<Pair<Pair<Int, Int>, IntSize>?>(null) }

    fun refit() {
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

    fun clampOffset() {
        if (container.width <= 0 || container.height <= 0) return
        val gw = grid.width * scale
        val gh = grid.height * scale
        val x = if (gw >= container.width) {
            offset.x.coerceIn(container.width - gw, 0f)
        } else {
            (container.width - gw) / 2f
        }
        val y = if (gh >= container.height) {
            offset.y.coerceIn(container.height - gh, 0f)
        } else {
            (container.height - gh) / 2f
        }
        offset = Offset(x, y)
    }

    LaunchedEffect(container, grid.width, grid.height) {
        if (container.width <= 0 || container.height <= 0) return@LaunchedEffect
        val dims = grid.width to grid.height
        val prev = refitKey
        refitKey = dims to container
        if (prev == null || prev.first != dims) refit() else clampOffset()
    }

    Canvas(
        modifier = modifier
            .onSizeChanged { container = it }
            .pointerInput(editMode, moveMode, grid.width, grid.height) {
                val touchSlop = viewConfiguration.touchSlop
                val panSlop = touchSlop * 1.6f
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)

                    // A floating overlay (the draggable tool bar) consumed this
                    // touch, so it is not a draw, pan, or cell tap.
                    if (down.isConsumed) {
                        var fingerDown = true
                        while (fingerDown) {
                            val ev = awaitPointerEvent()
                            fingerDown = ev.changes.any { it.pressed }
                        }
                        return@awaitEachGesture
                    }

                    var drawing = editMode && !moveMode
                    var strokeStarted = false
                    var lastEdit = -1
                    var panAccum = Offset.Zero
                    var zoomAccum = 1f
                    var pastSlop = false
                    var canceled = false
                    var cursorRow = Int.MIN_VALUE
                    var cursorCol = Int.MIN_VALUE

                    fun cellAt(position: Offset): Pair<Int, Int>? {
                        if (scale <= 0f) return null
                        val col = floor((position.x - offset.x) / scale).toInt()
                        val row = floor((position.y - offset.y) / scale).toInt()
                        return if (row in 0 until grid.height && col in 0 until grid.width) row to col else null
                    }

                    fun editAt(position: Offset) {
                        val cell = cellAt(position) ?: return
                        val (row, col) = cell
                        val index = row * grid.width + col
                        if (index != lastEdit) {
                            lastEdit = index
                            onEdit(row, col)
                        }
                    }

                    fun startStroke() {
                        if (!strokeStarted) {
                            strokeStarted = true
                            onEditStart()
                        }
                    }

                    fun markCursor(position: Offset) {
                        val cell = cellAt(position)
                        if (cell == null) {
                            if (cursorRow != Int.MIN_VALUE) {
                                cursorRow = Int.MIN_VALUE
                                cursorCol = Int.MIN_VALUE
                                onEditCursor(0, 0, false)
                            }
                        } else if (cell.first != cursorRow || cell.second != cursorCol) {
                            cursorRow = cell.first
                            cursorCol = cell.second
                            onEditCursor(cell.first, cell.second, true)
                        }
                    }

                    if (drawing) {
                        startStroke()
                        editAt(down.position)
                        markCursor(down.position)
                    }

                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.any { it.isConsumed }) {
                            canceled = true
                            break
                        }

                        val minScale = fitScale * 0.6f
                        val maxScale = MAX_CELL_PX

                        val ch = event.changes
                        var panX = 0f
                        var panY = 0f
                        var zoomChange = 1f
                        var cx = 0f
                        var cy = 0f
                        var pressedCount = 0
                        var spanDist = 0f
                        var spanCount = 0
                        for (i in ch.indices) {
                            val c = ch[i]
                            if (c.pressed && c.previousPressed) {
                                panX += c.position.x - c.previousPosition.x
                                panY += c.position.y - c.previousPosition.y
                                for (j in i + 1 until ch.size) {
                                    val o = ch[j]
                                    if (o.pressed && o.previousPressed) {
                                        val before = (c.previousPosition - o.previousPosition).getDistance()
                                        val after = (c.position - o.position).getDistance()
                                        if (before > 0f && after > 0f) zoomChange *= after / before
                                        spanDist += after
                                        spanCount++
                                    }
                                }
                            }
                            if (c.pressed) {
                                cx += c.position.x
                                cy += c.position.y
                                pressedCount++
                            }
                        }
                        val panChange =
                            if (ch.isEmpty()) Offset.Zero else Offset(panX / ch.size, panY / ch.size)
                        val centroid =
                            if (pressedCount > 0) Offset(cx / pressedCount, cy / pressedCount) else null
                        val spanSize = if (spanCount > 0) spanDist / spanCount else 0f

                        // A second finger in edit mode switches from drawing to navigating.
                        if (drawing && pressedCount >= 2) drawing = false

                        if (drawing) {
                            startStroke()
                            ch.firstOrNull { it.pressed }?.let {
                                editAt(it.position)
                                markCursor(it.position)
                            }
                            event.changes.forEach {
                                if (it.position != it.previousPosition) it.consume()
                            }
                        } else {
                            if (!pastSlop) {
                                panAccum += panChange
                                zoomAccum *= zoomChange
                                val zoomMotion = abs(1 - zoomAccum) * spanSize
                                if (zoomMotion > touchSlop || panAccum.getDistance() > panSlop) {
                                    pastSlop = true
                                }
                            }

                            if (pastSlop && scale > 0f && centroid != null) {
                                if (zoomChange != 1f || panChange != Offset.Zero) {
                                    val newScale = (scale * zoomChange).coerceIn(minScale, maxScale)
                                    val k = newScale / scale
                                    offset = Offset(
                                        centroid.x - (centroid.x - offset.x) * k,
                                        centroid.y - (centroid.y - offset.y) * k,
                                    ) + panChange
                                    scale = newScale
                                    clampOffset()
                                }
                                event.changes.forEach {
                                    if (it.position != it.previousPosition) it.consume()
                                }
                            }
                        }

                        if (!event.changes.any { it.pressed }) break
                    }

                    if (cursorRow != Int.MIN_VALUE) onEditCursor(0, 0, false)
                    if (strokeStarted) onEditEnd()

                    if (!editMode && !pastSlop && !canceled && scale > 0f) {
                        val col = floor((down.position.x - offset.x) / scale).toInt()
                        val row = floor((down.position.y - offset.y) / scale).toInt()
                        onCellTap(row, col)
                    }
                }
            },
    ) {
        val cell = scale
        if (cell <= 0f) return@Canvas

        val c0 = floor((0f - offset.x) / cell).toInt().coerceAtLeast(0)
        val c1 = ceil((size.width - offset.x) / cell).toInt().coerceIn(0, grid.width)
        val r0 = floor((0f - offset.y) / cell).toInt().coerceAtLeast(0)
        val r1 = ceil((size.height - offset.y) / cell).toInt().coerceIn(0, grid.height)

        val ref = if (showReference) reference else null
        for (r in r0 until r1) {
            for (c in c0 until c1) {
                val code = grid[r, c]
                val x = offset.x + c * cell
                val y = offset.y + r * cell

                // Background layer: the reference image (holes stay empty), or a
                // neutral backdrop when the reference is hidden.
                if (ref != null && c < ref.width && r < ref.height) {
                    val refArgb = ref.argb(c, r)
                    if (((refArgb ushr 24) and 0xFF) >= EMPTY_ALPHA) {
                        drawRect(color = Color(refArgb), topLeft = Offset(x, y), size = Size(cell, cell))
                    } else {
                        drawEmptyCell(x, y, cell)
                    }
                } else {
                    drawEmptyCell(x, y, cell)
                }

                // Pattern layer: opaque beads sit on top of the reference, so the
                // reference shows through only where there is no bead.
                if (!showPattern || code == null) continue
                val bead = Color(colorByCode[code] ?: 0xFFFFFFFF.toInt())
                drawRect(
                    color = bead,
                    topLeft = Offset(x, y),
                    size = Size(cell, cell),
                )
                if (selectedCode != null) {
                    if (code != selectedCode) {
                        drawRect(
                            color = SPOTLIGHT_DIM,
                            topLeft = Offset(x, y),
                            size = Size(cell, cell),
                        )
                    } else {
                        val inset = cell * 0.08f
                        val stroke = (cell * 0.04f).coerceIn(1.5f, 3f)
                        drawRect(
                            color = SELECT_ACCENT,
                            topLeft = Offset(x + inset, y + inset),
                            size = Size(cell - inset * 2, cell - inset * 2),
                            style = Stroke(width = stroke),
                        )
                        val sel = selectedPos
                        if (sel != null && sel.first == r && sel.second == c) {
                            drawCircle(
                                color = inkByCode[code] ?: Color.Black,
                                radius = cell / 2f - inset,
                                center = Offset(x + cell / 2f, y + cell / 2f),
                                style = Stroke(width = (cell * 0.08f).coerceIn(2f, 5f)),
                            )
                        }
                    }
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

        val groupColor = Color(0x99000000)
        val groupStroke = max(2f, cell * 0.12f)
        var g = ceil(c0 / GROUP.toFloat()).toInt() * GROUP
        while (g <= c1) {
            val x = offset.x + g * cell
            drawLine(groupColor, Offset(x, offset.y + r0 * cell), Offset(x, offset.y + r1 * cell), strokeWidth = groupStroke)
            g += GROUP
        }
        g = ceil(r0 / GROUP.toFloat()).toInt() * GROUP
        while (g <= r1) {
            val y = offset.y + g * cell
            drawLine(groupColor, Offset(offset.x + c0 * cell, y), Offset(offset.x + c1 * cell, y), strokeWidth = groupStroke)
            g += GROUP
        }

        val halfColor = Color(0x77000000)
        val halfStroke = max(1.5f, cell * 0.06f)
        var h = ceil(c0 / HALF_GROUP.toFloat()).toInt() * HALF_GROUP
        while (h <= c1) {
            if (h % GROUP != 0) {
                val x = offset.x + h * cell
                drawLine(halfColor, Offset(x, offset.y + r0 * cell), Offset(x, offset.y + r1 * cell), strokeWidth = halfStroke)
            }
            h += HALF_GROUP
        }
        h = ceil(r0 / HALF_GROUP.toFloat()).toInt() * HALF_GROUP
        while (h <= r1) {
            if (h % GROUP != 0) {
                val y = offset.y + h * cell
                drawLine(halfColor, Offset(offset.x + c0 * cell, y), Offset(offset.x + c1 * cell, y), strokeWidth = halfStroke)
            }
            h += HALF_GROUP
        }

        if (cell >= 6f) {
            val labelPx = (cell * 0.75f).coerceIn(14f, 32f)
            val colLabelColor = Color(0xFF1565C0)
            val rowLabelColor = Color(0xFFC62828)
            fun groupNumber(n: Int, color: Color): TextLayoutResult {
                val key = "G$n$color${labelPx.toInt()}"
                return textCache.getOrPut(key) {
                    textMeasurer.measure(
                        text = AnnotatedString(n.toString()),
                        style = TextStyle(
                            color = color,
                            fontSize = with(density) { labelPx.toSp() },
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }
            val pad = 3f
            g = ceil(c0 / GROUP.toFloat()).toInt() * GROUP
            while (g <= c1) {
                if (g != 0) {
                    val layout = groupNumber(g, colLabelColor)
                    val cx = offset.x + g * cell
                    val ty = max(0f, offset.y - layout.size.height - 2f)
                    drawRoundRect(
                        color = GROUP_CHIP,
                        topLeft = Offset(cx - layout.size.width / 2f - pad, ty - pad),
                        size = Size(layout.size.width + pad * 2, layout.size.height + pad * 2),
                        cornerRadius = CornerRadius(4f, 4f),
                    )
                    drawText(
                        layout,
                        topLeft = Offset(cx - layout.size.width / 2f, ty),
                    )
                }
                g += GROUP
            }
            g = ceil(r0 / GROUP.toFloat()).toInt() * GROUP
            while (g <= r1) {
                if (g != 0) {
                    val layout = groupNumber(g, rowLabelColor)
                    val cy = offset.y + g * cell
                    val tx = max(0f, offset.x - layout.size.width - 2f)
                    drawRoundRect(
                        color = GROUP_CHIP,
                        topLeft = Offset(tx - pad, cy - layout.size.height / 2f - pad),
                        size = Size(layout.size.width + pad * 2, layout.size.height + pad * 2),
                        cornerRadius = CornerRadius(4f, 4f),
                    )
                    drawText(
                        layout,
                        topLeft = Offset(tx, cy - layout.size.height / 2f),
                    )
                }
                g += GROUP
            }
        }

        if (cell >= 24f) {
            for (r in r0 until r1) {
                for (c in c0 until c1) {
                    val code = grid[r, c] ?: continue
                    val textPx = cell * 0.45f
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

        highlightCell?.let { (hr, hc) ->
            if (hr in r0 until r1 && hc in c0 until c1) {
                val x = offset.x + hc * cell
                val y = offset.y + hr * cell
                val stroke = (cell * 0.1f).coerceIn(2f, 6f)
                val inset = stroke * 0.6f
                drawRect(
                    color = Color(0xCC000000),
                    topLeft = Offset(x + inset, y + inset),
                    size = Size(cell - inset * 2, cell - inset * 2),
                    style = Stroke(width = stroke),
                )
                drawRect(
                    color = cursorColor,
                    topLeft = Offset(x + inset * 1.8f, y + inset * 1.8f),
                    size = Size(cell - inset * 3.6f, cell - inset * 3.6f),
                    style = Stroke(width = stroke * 0.7f),
                )
            }
        }
    }
}
