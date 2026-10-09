package com.pindou.core.processor

import com.pindou.core.model.PixelImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
}
