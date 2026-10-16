package com.pindou.core.imaging

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class FloatMaskTest {

    @Test
    fun `toBooleanMask thresholds at the given value`() {
        val mask = FloatMask(3, 1, floatArrayOf(0.1f, 0.5f, 0.9f))
        assertArrayEquals(booleanArrayOf(false, true, true), mask.toBooleanMask())
        assertArrayEquals(booleanArrayOf(false, false, true), mask.toBooleanMask(threshold = 0.6f))
    }

    @Test
    fun `union takes the max, intersect the min, difference clamps at zero`() {
        val a = FloatMask(2, 1, floatArrayOf(0.2f, 0.9f))
        val b = FloatMask(2, 1, floatArrayOf(0.7f, 0.3f))
        assertArrayEquals(floatArrayOf(0.7f, 0.9f), a.union(b).values, 1e-6f)
        assertArrayEquals(floatArrayOf(0.2f, 0.3f), a.intersect(b).values, 1e-6f)
        assertArrayEquals(floatArrayOf(0f, 0.6f), a.difference(b).values, 1e-6f)
    }

    @Test
    fun `filled produces a uniform mask`() {
        val mask = FloatMask.filled(2, 2, 0.25f)
        assertEquals(0.25f, mask[1, 1], 1e-6f)
        assertEquals(4, mask.size)
    }
}
