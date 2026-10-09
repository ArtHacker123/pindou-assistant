package com.pindou.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NaturalOrderTest {

    @Test
    fun `sorts single digit codes before double digit codes`() {
        val input = listOf("C10", "C2", "C1", "C21", "C3")
        val sorted = input.sortedWith(NaturalOrder)
        assertEquals(listOf("C1", "C2", "C3", "C10", "C21"), sorted)
    }

    @Test
    fun `handles differing prefixes`() {
        val sorted = listOf("H2", "C9", "C10", "H1").sortedWith(NaturalOrder)
        assertEquals(listOf("C9", "C10", "H1", "H2"), sorted)
    }

    @Test
    fun `sorts with helper extension`() {
        val data = listOf("C10", "C1", "C2")
        assertEquals(listOf("C1", "C2", "C10"), data.sortedNaturallyBy { it })
    }

    @Test
    fun `equal strings compare equal`() {
        assertEquals(0, NaturalOrder.compare("C7", "C7"))
        assertTrue(NaturalOrder.compare("C7", "C8") < 0)
    }
}
