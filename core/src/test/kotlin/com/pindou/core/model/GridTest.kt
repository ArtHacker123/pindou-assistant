package com.pindou.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GridTest {

    @Test
    fun `indexes row major`() {
        val grid = Grid(width = 3, height = 2, cells = listOf("A", "B", "C", "D", "E", "F"))
        assertEquals("A", grid[0, 0])
        assertEquals("C", grid[0, 2])
        assertEquals("D", grid[1, 0])
        assertEquals("F", grid[1, 2])
    }

    @Test
    fun `distinct codes keeps first-seen order`() {
        val grid = Grid(2, 2, listOf("A", "B", "A", "C"))
        assertEquals(listOf("A", "B", "C"), grid.distinctCodes())
    }

    @Test
    fun `rejects mismatched cell count`() {
        assertThrows(IllegalArgumentException::class.java) {
            Grid(width = 2, height = 2, cells = listOf("A", "B", "C"))
        }
    }
}
