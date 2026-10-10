package com.pindou.core.generate

import com.pindou.core.TestPalettes
import com.pindou.core.model.PixelImage
import com.pindou.core.processor.ImageProcessor
import com.pindou.core.processor.ImageProcessorRegistry
import com.pindou.core.processor.ImageType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PatternGeneratorTest {

    private val noOpProcessors = ImageProcessorRegistry(
        listOf(object : ImageProcessor {
            override val id = ImageType.CARTOON.id
            override val displayName = "noop"
            override fun process(image: PixelImage) = image
        })
    )

    /** Two vertical color bands: white then red. */
    private fun bandsImage(): PixelImage {
        val width = 8
        val height = 4
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] =
                    if (x < width / 2) 0xFF000000.toInt() else 0xFFFF0000.toInt()
            }
        }
        return PixelImage(width, height, pixels)
    }

    @Test
    fun `maps each band to its bead color`() {
        val generator = PatternGenerator(processors = noOpProcessors)
        val project = generator.generate(
            bandsImage(),
            GenerateConfig(
                gridWidth = 2,
                gridHeight = 1,
                palette = TestPalettes.basic(),
                colorCountTarget = 2,
                imageType = ImageType.CARTOON,
                seed = 1,
                createdAt = 123L,
            ),
        )
        assertEquals("C1", project.grid[0, 0]) // black band
        assertEquals("C3", project.grid[0, 1]) // red band
        assertEquals(2, project.paletteUsed.size)
    }

    @Test
    fun `paletteUsed is ordered naturally and bounded by N`() {
        val generator = PatternGenerator(processors = noOpProcessors)
        val project = generator.generate(
            bandsImage(),
            GenerateConfig(
                gridWidth = 4,
                gridHeight = 4,
                palette = TestPalettes.withMany(),
                colorCountTarget = 3,
                seed = 5,
            ),
        )
        assertTrue("used count must be <= N", project.paletteUsed.size <= 3)
        val codes = project.paletteUsed.map { it.code }
        assertEquals(codes.sortedWith(com.pindou.core.util.NaturalOrder), codes)
    }

    @Test
    fun `is reproducible for the same seed`() {
        val generator = PatternGenerator(processors = noOpProcessors)
        val config = GenerateConfig(
            gridWidth = 8,
            gridHeight = 8,
            palette = TestPalettes.withMany(),
            colorCountTarget = 4,
            seed = 99,
        )
        val a = generator.generate(bandsImage(), config)
        val b = generator.generate(bandsImage(), config)
        assertEquals(a.grid.cells, b.grid.cells)
    }

    @Test
    fun `rejects oversized grids`() {
        val generator = PatternGenerator(processors = noOpProcessors)
        assertThrows(IllegalArgumentException::class.java) {
            generator.generate(
                bandsImage(),
                GenerateConfig(
                    gridWidth = 129,
                    gridHeight = 1,
                    palette = TestPalettes.basic(),
                    colorCountTarget = 2,
                ),
            )
        }
    }

    @Test
    fun `marks fully invalid cells as empty holes`() {
        // Left half transparent, right half red.
        val width = 4
        val height = 2
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (x < width / 2) 0x00000000 else 0xFFFF0000.toInt()
            }
        }
        val generator = PatternGenerator(processors = noOpProcessors)
        val project = generator.generate(
            PixelImage(width, height, pixels),
            GenerateConfig(
                gridWidth = 2,
                gridHeight = 1,
                palette = TestPalettes.basic(),
                colorCountTarget = 2,
                imageType = ImageType.CARTOON,
                seed = 1,
                createdAt = 123L,
            ),
        )
        assertEquals(null, project.grid[0, 0])
        assertEquals("C3", project.grid[0, 1])
        assertEquals(1, project.paletteUsed.size)
    }

    @Test
    fun `stores generation context in meta`() {
        val generator = PatternGenerator(processors = noOpProcessors)
        val project = generator.generate(
            bandsImage(),
            GenerateConfig(
                gridWidth = 2,
                gridHeight = 1,
                palette = TestPalettes.basic(),
                colorCountTarget = 2,
                imageType = ImageType.PHOTO,
                seed = 3,
                createdAt = 777L,
                sourceHash = "abc",
            ),
        )
        assertEquals("photo", project.meta.imageType)
        assertEquals(777L, project.meta.createdAt)
        assertEquals("abc", project.meta.sourceHash)
        assertEquals("test", project.meta.paletteName)
        assertEquals("1.0.0", project.version)
    }
}
