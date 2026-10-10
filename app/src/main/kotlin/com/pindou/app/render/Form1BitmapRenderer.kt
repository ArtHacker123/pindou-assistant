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

    // Form 2 group-grid palette (matches PatternGrid.kt).
    private const val BASE_LINE_COLOR = 0x55000000.toInt()
    private const val MID_LINE_COLOR = 0x77000000.toInt()
    private const val GROUP_LINE_COLOR = 0x99000000.toInt()
    private const val GROUP_COL_COLOR = 0xFF1565C0.toInt()
    private const val GROUP_ROW_COLOR = 0xFFC62828.toInt()

    /** A4 single-page defaults: 300 DPI with 10mm margins on both axes. */
    private const val A4_DPI = 300
    private const val A4_MARGIN_MM = 10

    /** 1..maxCell for the A4 auto-fit; the old UI cap was 96 px/cell. */
    private const val MAX_CELL_PX = 96

    /** A non-translated plan result: chosen px/cell and the projected PNG size. */
    data class ExportPlan(
        val cellOutPx: Int,
        val canvasWidth: Int,
        val canvasHeight: Int,
    )

    private class Form1Paints(
        val codePaint: Paint,
        val strokePaint: Paint,
        val labelPaint: Paint,
        val tablePaint: Paint,
        val colGroupPaint: Paint,
        val rowGroupPaint: Paint,
    )

    fun render(project: PindouProject, cellOutPx: Int): Bitmap {
        val grid = project.grid
        val itemByCode = project.paletteSource.items.associateBy { it.code }
        val paints = paintsFor(cellOutPx)
        val metrics = metricsFor(project, cellOutPx, paints)
        val layout = Form1LayoutCalculator.compute(metrics)
        check(!Form1LayoutCalculator.exceedsLimit(layout)) {
            "Export too large: ${layout.canvasWidth}x${layout.canvasHeight}"
        }

        val bitmap = Bitmap.createBitmap(layout.canvasWidth, layout.canvasHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        drawColumnLabels(canvas, layout, grid.width, paints.labelPaint, paints.colGroupPaint)
        drawRowLabels(canvas, layout, grid.height, paints.labelPaint, paints.rowGroupPaint)
        drawGrid(canvas, layout, project, itemByCode, paints.codePaint, paints.strokePaint)
        drawColorTable(canvas, layout, project, paints.tablePaint)

        return bitmap
    }

    /**
     * Largest px/cell so the rendered Form 1 fits one A4 page at [A4_DPI] with
     * [A4_MARGIN_MM] margins (portrait: 210×297mm).
     */
    fun fitToA4(project: PindouProject): ExportPlan {
        val mmToPx = A4_DPI / 25.4f
        val maxW = ((210 - 2 * A4_MARGIN_MM) * mmToPx).toInt()
        val maxH = ((297 - 2 * A4_MARGIN_MM) * mmToPx).toInt()
        return fitPlan(project, maxW, maxH)
    }

    /** Largest px/cell whose PNG width stays at or below [maxWidthPx]. */
    fun fitToWidth(project: PindouProject, maxWidthPx: Int): ExportPlan =
        fitPlan(project, maxWidthPx, Int.MAX_VALUE)

    /** Largest px/cell the device can safely allocate, bounded only by the pixel guard. */
    fun fitMax(project: PindouProject): ExportPlan =
        fitPlan(project, Int.MAX_VALUE, Int.MAX_VALUE)

    private fun fitPlan(project: PindouProject, maxWidthPx: Int, maxHeightPx: Int): ExportPlan {
        val cell = Form1LayoutCalculator.largestFitCell(MAX_CELL_PX) { c ->
            val layout = Form1LayoutCalculator.compute(metricsFor(project, c, paintsFor(c)))
            !Form1LayoutCalculator.exceedsLimit(layout) &&
                layout.canvasWidth <= maxWidthPx && layout.canvasHeight <= maxHeightPx
        }
        val layout = Form1LayoutCalculator.compute(metricsFor(project, cell, paintsFor(cell)))
        return ExportPlan(cell, layout.canvasWidth, layout.canvasHeight)
    }

    private fun paintsFor(cellOutPx: Int): Form1Paints {
        val codeTextSize = (cellOutPx * 0.45f).coerceAtLeast(9f)
        val labelTextSize = (cellOutPx * 0.45f).coerceAtLeast(11f)
        val tableTextSize = (cellOutPx * 0.45f).coerceAtLeast(12f)

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
        val colGroupPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = labelTextSize
            color = GROUP_COL_COLOR
            typeface = Typeface.DEFAULT_BOLD
        }
        val rowGroupPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = labelTextSize
            color = GROUP_ROW_COLOR
            typeface = Typeface.DEFAULT_BOLD
        }
        return Form1Paints(codePaint, strokePaint, labelPaint, tablePaint, colGroupPaint, rowGroupPaint)
    }

    private fun metricsFor(project: PindouProject, cellOutPx: Int, paints: Form1Paints = paintsFor(cellOutPx)): Form1Metrics {
        val grid = project.grid
        val used = project.paletteUsed
        val labelPaint = paints.labelPaint
        val tablePaint = paints.tablePaint
        val labelMetrics = labelPaint.fontMetrics
        val colLabelHeight = (labelMetrics.descent - labelMetrics.ascent).toInt() + PADDING * 2
        val rowLabelWidth = maxRowLabelWidth(grid.height, labelPaint) + PADDING * 2

        val swatchSize = (cellOutPx * 0.55f).toInt().coerceAtLeast(16)
        val maxCodeWidth = used.maxOfOrNull { tablePaint.measureText(it.code) } ?: 0f
        val maxNameWidth = used.maxOfOrNull { item -> item.name?.let { tablePaint.measureText(it) } ?: 0f } ?: 0f
        val tableTextBlock = maxCodeWidth + if (maxNameWidth > 0f) maxNameWidth + tablePaint.textSize else 0f
        val tableWidth = (swatchSize + tablePaint.textSize + tableTextBlock).toInt() + PADDING * 4
        val tableRowHeight = maxOf(swatchSize, (tablePaint.fontMetrics.descent - tablePaint.fontMetrics.ascent).toInt()) + PADDING * 2

        return Form1Metrics(
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
    }

    private fun maxRowLabelWidth(height: Int, paint: Paint): Int {
        var max = 0f
        for (row in 1..height) max = maxOf(max, paint.measureText(row.toString()))
        return max.toInt()
    }

    private fun drawColumnLabels(
        canvas: Canvas,
        layout: Form1Layout,
        gridWidth: Int,
        paint: Paint,
        groupPaint: Paint,
    ) {
        val baseline = (layout.gridRect.top - PADDING).toFloat()
        for (col in 0 until gridWidth) {
            val x = (layout.gridRect.left + col * layout.cellSize + layout.cellSize / 2).toFloat()
            val label = (col + 1).toString()
            if ((col + 1) % 10 == 0) {
                canvas.drawText(label, x, baseline, groupPaint)
            } else {
                canvas.drawText(label, x, baseline, paint)
            }
        }
    }

    private fun drawRowLabels(
        canvas: Canvas,
        layout: Form1Layout,
        gridHeight: Int,
        paint: Paint,
        groupPaint: Paint,
    ) {
        val x = layout.gridRect.left / 2f
        val fm = paint.fontMetrics
        val centerOffset = -(fm.ascent + fm.descent) / 2f
        for (row in 0 until gridHeight) {
            val y = layout.gridRect.top + row * layout.cellSize + layout.cellSize / 2f + centerOffset
            val label = (row + 1).toString()
            if ((row + 1) % 10 == 0) {
                canvas.drawText(label, x, y, groupPaint)
            } else {
                canvas.drawText(label, x, y, paint)
            }
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
        val diag = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GRID_LINE_COLOR
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        val base = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = BASE_LINE_COLOR
            strokeWidth = maxOf(1f, cell * 0.03f)
            style = Paint.Style.STROKE
        }
        val mid = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = MID_LINE_COLOR
            strokeWidth = maxOf(1.5f, cell * 0.06f)
            style = Paint.Style.STROKE
        }
        val group = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GROUP_LINE_COLOR
            strokeWidth = maxOf(2f, cell * 0.12f)
            style = Paint.Style.STROKE
        }
        fun linePaint(index: Int): Paint = when {
            index % 10 == 0 -> group
            index % 5 == 0 -> mid
            else -> base
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
                    canvas.drawLine(left.toFloat(), top.toFloat(), (left + cell).toFloat(), (top + cell).toFloat(), diag)
                    canvas.drawLine((left + cell).toFloat(), top.toFloat(), left.toFloat(), (top + cell).toFloat(), diag)
                    continue
                }
                val rgb = itemByCode[code]?.rgb ?: Rgb(255, 255, 255)
                fill.color = rgb.toArgb()
                canvas.drawRect(left.toFloat(), top.toFloat(), (left + cell).toFloat(), (top + cell).toFloat(), fill)

                if (cell >= 24) {
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

        // Outer border plus per-cell lines, with 5/10 group lines like Form 2.
        for (col in 0..grid.width) {
            val x = (layout.gridRect.left + col * cell).toFloat()
            canvas.drawLine(x, layout.gridRect.top.toFloat(), x, layout.gridRect.bottom.toFloat(), linePaint(col))
        }
        for (row in 0..grid.height) {
            val y = (layout.gridRect.top + row * cell).toFloat()
            canvas.drawLine(layout.gridRect.left.toFloat(), y, layout.gridRect.right.toFloat(), y, linePaint(row))
        }
    }

    private fun drawColorTable(
        canvas: Canvas,
        layout: Form1Layout,
        project: PindouProject,
        paint: Paint,
    ) {
        val swatchSize = (layout.cellSize * 0.55f).toInt().coerceAtLeast(16)
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
