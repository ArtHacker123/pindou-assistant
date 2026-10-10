package com.pindou.core.render

import com.pindou.core.util.RectI

/**
 * The measured pixel sizes needed to lay out Form 1. Text sizes depend on the
 * platform, so the renderer measures them and passes them in, keeping this
 * calculation pure and unit-testable.
 */
data class Form1Metrics(
    val gridWidth: Int,
    val gridHeight: Int,
    val cellOutPx: Int,
    val rowLabelWidth: Int,
    val colLabelHeight: Int,
    val tableWidth: Int,
    val tableRowHeight: Int,
    val tableRows: Int,
    val padding: Int,
    val tableGap: Int,
)

/** Resolved geometry of the exported Form 1 image. */
data class Form1Layout(
    val canvasWidth: Int,
    val canvasHeight: Int,
    val cellSize: Int,
    val gridRect: RectI,
    val tableRect: RectI,
) {
    val totalPixels: Long get() = canvasWidth.toLong() * canvasHeight
}

/**
 * Places the grid on the left and the color lookup table to its right; the grid
 * is offset from the top-left by the row-number gutter and column-number header.
 */
object Form1LayoutCalculator {
    /**
     * Guard against allocating an unreasonably large bitmap.
     * 24M px ≈ 96MB of ARGB_8888 memory, safely under Android's ~100MB
     * hardware-canvas bitmap limit and typical app-heap caps.
     */
    const val MAX_EXPORT_PIXELS: Long = 24_000_000L

    fun compute(metrics: Form1Metrics): Form1Layout {
        val gridWpx = metrics.gridWidth * metrics.cellOutPx
        val gridHpx = metrics.gridHeight * metrics.cellOutPx
        val gridOriginX = metrics.padding + metrics.rowLabelWidth
        val gridOriginY = metrics.padding + metrics.colLabelHeight

        val tableOriginX = gridOriginX + gridWpx + metrics.tableGap
        val tableOriginY = metrics.padding
        val tableHeight = metrics.tableRowHeight * metrics.tableRows

        val canvasWidth = tableOriginX + metrics.tableWidth + metrics.padding
        val canvasHeight = maxOf(gridOriginY + gridHpx, tableOriginY + tableHeight) + metrics.padding

        return Form1Layout(
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
            cellSize = metrics.cellOutPx,
            gridRect = RectI(gridOriginX, gridOriginY, gridOriginX + gridWpx, gridOriginY + gridHpx),
            tableRect = RectI(tableOriginX, tableOriginY, tableOriginX + metrics.tableWidth, tableOriginY + tableHeight),
        )
    }

    fun exceedsLimit(layout: Form1Layout): Boolean = layout.totalPixels > MAX_EXPORT_PIXELS

    /**
     * Largest cell size in 1..maxCell whose measured canvas fits both page limits.
     * Assumes [sizeAt] is monotone non-decreasing in both dimensions.
     */
    fun largestFitCell(
        maxCell: Int,
        maxCanvasWidth: Int,
        maxCanvasHeight: Int,
        sizeAt: (Int) -> Pair<Int, Int>,
    ): Int {
        var lo = 1
        var hi = maxOf(1, maxCell)
        while (lo < hi) {
            val mid = (lo + hi + 1) / 2
            val (w, h) = sizeAt(mid)
            if (w <= maxCanvasWidth && h <= maxCanvasHeight) lo = mid else hi = mid - 1
        }
        return lo
    }
}
