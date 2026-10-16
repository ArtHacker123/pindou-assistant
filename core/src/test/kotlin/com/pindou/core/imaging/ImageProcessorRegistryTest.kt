package com.pindou.core.imaging

import com.pindou.core.model.PixelImage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class ImageProcessorRegistryTest {

    private val registry = ImageProcessorRegistry.default()

    @Test
    fun `resolves the built-in presets`() {
        assertNotNull(registry.get(ImageType.CARTOON.id))
        assertNotNull(registry.get(ImageType.PHOTO.id))
    }

    @Test
    fun `unknown id falls back to a registered processor`() {
        assertNotNull(registry.get("does-not-exist"))
    }

    @Test
    fun `processors preserve image dimensions`() {
        val image = PixelImage.filled(8, 6, 0xFF3366CC.toInt())
        for (processor in registry.all()) {
            val out = processor.process(image)
            assertEquals(8, out.width)
            assertEquals(6, out.height)
        }
    }

    @Test
    fun `processors keep transparent padding invalid`() {
        val image = PixelImage(4, 1, IntArray(4) { if (it < 2) 0xFF808080.toInt() else 0x00000000 })
        for (processor in registry.all()) {
            val out = processor.process(image)
            assertTrue("${processor.id} content stays valid", out.valid(0, 0))
            assertFalse("${processor.id} padding x=2 stays invalid", out.valid(2, 0))
            assertFalse("${processor.id} padding x=3 stays invalid", out.valid(3, 0))
        }
    }
}
