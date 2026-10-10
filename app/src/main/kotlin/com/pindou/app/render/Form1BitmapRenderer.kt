package com.pindou.app.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import com.pindou.core.model.PindouProject
import com.pindou.core.model.Rgb
import com.pindou.core.render.Form1Layout
import com.pindou.core.render.Form1LayoutCalculator
import com.pindou.core.render.Form1Metrics

/**
 * Renders Form 1: a printable image with column/row numbers, per-cell bead codes
 * and a color lookup table. All layout math lives in the pure
 * [Form1LayoutCalculator]; this class only paints.
 */
object Form1BitmapRenderer {

    private const val PADDING = 10
    private const val GRID_LINE_COLOR = 0xFF444444.toInt()
    private const val TABLE_GAP = 20
    private const val EMPTY_CELL_BG = 0xFFF5F5F5.toInt()

    fun render(project: PindouProject, cellOutPx: Int): Bitmap {
        val grid = project.grid
        val itemByCode = project.paletteSource.items.associateBy { it.code }
        val used = project.paletteUsed

        val codeTextSize = (cellOutPx * 0.32f).coerceAtLeast(9f)
        val labelTextSize = (cellOutPx * 0.34f).coerceAtLeast(11f)
        val tableTextSize = (cellOutPx * 0.30f).coerceAtLeast(12f)

        val codePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = codeTextSize
            typeface = Typeface.DEFAULT_BOLD
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = codeTextSize
            typeface = Typeface.DEFAULT_BOLD
            style = Paint.Style.STROKE
            strokeWidth = (codeTextSize * 0.16f).coerceAtLeast(1f)
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = labelTextSize
            color = Color.BLACK
        }
        val tablePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.LEFT
            textSize = tableTextSize
            color = Color.BLACK
        }

        val labelMetrics = labelPaint.fontMetrics
        val colLabelHeight = (labelMetrics.descent - labelMetrics.ascent).toInt() + PADDING * 2
        val rowLabelWidth = maxRowLabelWidth(grid.height, labelPaint) + PADDING * 2

        val swatchSize = (cellOutPx * 0.55f).toInt().coerceAtLeast(16)
        val maxCodeWidth = used.maxOfOrNull { tablePaint.measureText(it.code) } ?: 0f
        val maxNameWidth = used.maxOfOrNull { item -> item.name?.let { tablePaint.measureText(it) } ?: 0f } ?: 0f
        val tableTextBlock = maxCodeWidth + if (maxNameWidth > 0f) maxNameWidth + tableTextSize else 0f
        val tableWidth = (swatchSize + tableTextSize + tableTextBlock).toInt() + PADDING * 4
        val tableRowHeight = maxOf(swatchSize, (tablePaint.fontMetrics.descent - tablePaint.fontMetrics.ascent).toInt()) + PADDING * 2

        val metrics = Form1Metrics(
            gridWidth = grid.width,
            gridHeight = grid.height,
            cellOutPx = cellOutPx,
            rowLabelWidth = rowLabelWidth,
            colLabelHeight = colLabelHeight,
            tableWidth = tableWidth,
            tableRowHeight = tableRowHeight,
            tableRows = used.size,
            padding = PADDING,
            tableGap = TABLE_GAP,
        )
        val layout = Form1LayoutCalculator.compute(metrics)
        check(!Form1LayoutCalculator.exceedsLimit(layout)) {
            "Export too large: ${layout.canvasWidth}x${layout.canvasHeight}"
        }

        val bitmap = Bitmap.createBitmap(layout.canvasWidth, layout.canvasHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        drawColumnLabels(canvas, layout, grid.width, labelPaint)
        drawRowLabels(canvas, layout, grid.height, labelPaint)
        drawGrid(canvas, layout, project, itemByCode, codePaint, strokePaint)
        drawColorTable(canvas, layout, project, swatchSize, tablePaint)

        return bitmap
    }

    private fun maxRowLabelWidth(height: Int, paint: Paint): Int {
        var max = 0f
        for (row in 1..height) max = maxOf(max, paint.measureText(row.toString()))
        return max.toInt()
    }

    private fun drawColumnLabels(canvas: Canvas, layout: Form1Layout, gridWidth: Int, paint: Paint) {
        val baseline = (layout.gridRect.top - PADDING).toFloat()
        for (col in 0 until gridWidth) {
            val x = (layout.gridRect.left + col * layout.cellSize + layout.cellSize / 2).toFloat()
            canvas.drawText((col + 1).toString(), x, baseline, paint)
        }
    }

    private fun drawRowLabels(canvas: Canvas, layout: Form1Layout, gridHeight: Int, paint: Paint) {
        val x = (layout.gridRect.left - PADDING).toFloat()
        val fm = paint.fontMetrics
        val centerOffset = -(fm.ascent + fm.descent) / 2f
        for (row in 0 until gridHeight) {
            val y = layout.gridRect.top + row * layout.cellSize + layout.cellSize / 2f + centerOffset
            canvas.drawText((row + 1).toString(), x, y, paint)
        }
    }

    private fun drawGrid(
        canvas: Canvas,
        layout: Form1Layout,
        project: PindouProject,
        itemByCode: Map<String, com.pindou.core.model.PaletteItem>,
        codePaint: Paint,
        strokePaint: Paint,
    ) {
        val cell = layout.cellSize
        val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GRID_LINE_COLOR
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        val grid = project.grid

        for (row in 0 until grid.height) {
            for (col in 0 until grid.width) {
                val code = grid[row, col]
                val left = layout.gridRect.left + col * cell
                val top = layout.gridRect.top + row * cell
                if (code == null) {
                    fill.color = EMPTY_CELL_BG
                    canvas.drawRect(left.toFloat(), top.toFloat(), (left + cell).toFloat(), (top + cell).toFloat(), fill)
                    canvas.drawLine(left.toFloat(), top.toFloat(), (left + cell).toFloat(), (top + cell).toFloat(), line)
                    canvas.drawLine((left + cell).toFloat(), top.toFloat(), left.toFloat(), (top + cell).toFloat(), line)
                    continue
                }
                val rgb = itemByCode[code]?.rgb ?: Rgb(255, 255, 255)
                fill.color = rgb.toArgb()
                canvas.drawRect(left.toFloat(), top.toFloat(), (left + cell).toFloat(), (top + cell).toFloat(), fill)

                if (cell >= 34) {
                    val cx = left + cell / 2f
                    val fm = codePaint.fontMetrics
                    val cy = top + cell / 2f - (fm.ascent + fm.descent) / 2f
                    val dark = rgb.luminance() >= 140
                    codePaint.color = if (dark) Color.BLACK else Color.WHITE
                    strokePaint.color = if (dark) Color.WHITE else Color.BLACK
                    canvas.drawText(code, cx, cy, strokePaint)
                    canvas.drawText(code, cx, cy, codePaint)
                }
            }
        }

        // Outer border plus per-cell lines.
        for (col in 0..grid.width) {
            val x = (layout.gridRect.left + col * cell).toFloat()
            canvas.drawLine(x, layout.gridRect.top.toFloat(), x, layout.gridRect.bottom.toFloat(), line)
        }
        for (row in 0..grid.height) {
            val y = (layout.gridRect.top + row * cell).toFloat()
            canvas.drawLine(layout.gridRect.left.toFloat(), y, layout.gridRect.right.toFloat(), y, line)
        }
    }

    private fun drawColorTable(
        canvas: Canvas,
        layout: Form1Layout,
        project: PindouProject,
        swatchSize: Int,
        paint: Paint,
    ) {
        val swatchPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GRID_LINE_COLOR
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val x = layout.tableRect.left
        val fm = paint.fontMetrics
        val textBaselineOffset = (swatchSize - (fm.descent - fm.ascent)) / 2f - fm.ascent
        val rowHeight = if (project.paletteUsed.isEmpty()) 0
        else layout.tableRect.height / project.paletteUsed.size

        project.paletteUsed.forEachIndexed { index, item ->
            val top = layout.tableRect.top + index * rowHeight
            val swatchTop = top + PADDING
            val left = x + PADDING
            swatchPaint.color = item.rgb.toArgb()
            canvas.drawRect(
                left.toFloat(),
                swatchTop.toFloat(),
                (left + swatchSize).toFloat(),
                (swatchTop + swatchSize).toFloat(),
                swatchPaint,
            )
            canvas.drawRect(
                left.toFloat(),
                swatchTop.toFloat(),
                (left + swatchSize).toFloat(),
                (swatchTop + swatchSize).toFloat(),
                border,
            )
            val textX = left + swatchSize + PADDING
            val baseline = top + PADDING + textBaselineOffset
            val name = item.name
            val label = if (name.isNullOrBlank()) item.code else "${item.code}  $name"
            canvas.drawText(label, textX.toFloat(), baseline, paint)
        }
    }
}
