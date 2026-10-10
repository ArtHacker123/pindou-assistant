package com.pindou.core.render

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Form1LayoutCalculatorTest {

    private fun metrics(tableRows: Int = 5) = Form1Metrics(
        gridWidth = 10,
        gridHeight = 8,
        cellOutPx = 60,
        rowLabelWidth = 40,
        colLabelHeight = 30,
        tableWidth = 200,
        tableRowHeight = 20,
        tableRows = tableRows,
        padding = 10,
        tableGap = 12,
    )

    @Test
    fun `grid is offset by gutter and header`() {
        val layout = Form1LayoutCalculator.compute(metrics())
        assertEquals(50, layout.gridRect.left) // padding + rowLabelWidth
        assertEquals(40, layout.gridRect.top) // padding + colLabelHeight
        assertEquals(600, layout.gridRect.width) // 10 * 60
        assertEquals(480, layout.gridRect.height) // 8 * 60
    }

    @Test
    fun `color table sits to the right of the grid`() {
        val layout = Form1LayoutCalculator.compute(metrics())
        assertEquals(layout.gridRect.right + 12, layout.tableRect.left)
        assertEquals(200, layout.tableRect.width)
        assertEquals(100, layout.tableRect.height) // 5 rows * 20
    }

    @Test
    fun `canvas grows when the table is taller than the grid`() {
        val tallTable = Form1LayoutCalculator.compute(metrics(tableRows = 100))
        assertTrue(tallTable.canvasHeight >= 100 * 20)
    }

    @Test
    fun `small exports stay under the pixel guard`() {
        assertFalse(Form1LayoutCalculator.exceedsLimit(Form1LayoutCalculator.compute(metrics())))
    }

    @Test
    fun `huge exports trip the pixel guard`() {
        val huge = Form1Metrics(
            gridWidth = 128,
            gridHeight = 128,
            cellOutPx = 200,
            rowLabelWidth = 40,
            colLabelHeight = 30,
            tableWidth = 200,
            tableRowHeight = 20,
            tableRows = 10,
            padding = 10,
            tableGap = 12,
        )
        assertTrue(Form1LayoutCalculator.exceedsLimit(Form1LayoutCalculator.compute(huge)))
    }

    @Test
    fun `largestFitCell respects both page limits`() {
        val fitted = Form1LayoutCalculator.largestFitCell(maxCell = 96) { cell ->
            cell * 11 <= 1000 && cell * 8 <= 900
        }
        assertEquals(90, fitted) // min(1000/11, 900/8) = min(90, 112) = 90
    }

    @Test
    fun `largestFitCell returns 1 when nothing fits`() {
        val fitted = Form1LayoutCalculator.largestFitCell(maxCell = 96) { cell ->
            cell * 11 <= 10 && cell * 8 <= 10
        }
        assertEquals(1, fitted)
    }

    @Test
    fun `largestFitCell honors a pixel-limit predicate`() {
        val budget = 24_000_000L
        val fitted = Form1LayoutCalculator.largestFitCell(maxCell = 1000) { cell ->
            (cell.toLong() * 11) * (cell.toLong() * 8) <= budget
        }
        assertEquals(522, fitted) // sqrt(24_000_000 / 88) ≈ 522
    }
}
