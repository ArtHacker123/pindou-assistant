package com.pindou.core.quant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class QuantizerRegistryTest {

    @Test
    fun `resolves kmeans by default id`() {
        val registry = QuantizerRegistry.default()
        assertEquals("kmeans", registry.get(QuantizerRegistry.DEFAULT_ID).id)
    }

    @Test
    fun `throws for unknown id`() {
        assertThrows(IllegalStateException::class.java) {
            QuantizerRegistry.default().get("median-cut")
        }
    }
}
