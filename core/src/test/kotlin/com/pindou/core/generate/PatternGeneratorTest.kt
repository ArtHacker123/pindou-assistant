package com.pindou.core.generate

import com.pindou.core.TestPalettes
import com.pindou.core.imaging.ImageType
import com.pindou.core.imaging.ReferenceBuilder
import com.pindou.core.model.PixelImage
import com.pindou.core.model.toReferenceGrid
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PatternGeneratorTest {

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
        val generator = PatternGenerator()
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
        val generator = PatternGenerator()
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
        val generator = PatternGenerator()
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
        val generator = PatternGenerator()
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
        val generator = PatternGenerator()
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
        val generator = PatternGenerator()
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
        assertEquals("1.1.0", project.version)
    }

    @Test
    fun `emits a grid-sized reference image`() {
        val generator = PatternGenerator()
        val project = generator.generate(
            bandsImage(),
            GenerateConfig(
                gridWidth = 2,
                gridHeight = 2,
                palette = TestPalettes.basic(),
                colorCountTarget = 2,
                imageType = ImageType.CARTOON,
                seed = 1,
            ),
        )
        val reference = project.referenceGrid
        assertEquals(2, reference?.width)
        assertEquals(2, reference?.height)
        assertEquals(2 * 2 * 8, reference?.argbHex?.length)
    }

    @Test
    fun `reference averages black and white in linear light`() {
        // A single cell covering an equal mix of black and white.
        val image = PixelImage(2, 1, intArrayOf(0xFF000000.toInt(), 0xFFFFFFFF.toInt()))
        val generator = PatternGenerator()
        val project = generator.generate(
            image,
            GenerateConfig(
                gridWidth = 1,
                gridHeight = 1,
                palette = TestPalettes.basic(),
                colorCountTarget = 2,
                imageType = ImageType.CARTOON,
                seed = 1,
            ),
        )
        val argb = project.referenceGrid!!.toImage().argb(0, 0)
        val r = (argb shr 16) and 0xFF
        assertEquals(255, (argb ushr 24) and 0xFF)
        assertTrue("expected a light midtone, got $r", r in 186..190)
    }

    @Test
    fun `cartoon boundary cell snaps to a real bead not a synthesized midtone`() {
        // One cell covering an equal mix of black and white.
        val image = PixelImage(2, 1, intArrayOf(0xFF000000.toInt(), 0xFFFFFFFF.toInt()))
        val project = PatternGenerator().generate(
            image,
            GenerateConfig(
                gridWidth = 1,
                gridHeight = 1,
                palette = TestPalettes.basic(),
                colorCountTarget = 2,
                colorCountAuto = true,
                imageType = ImageType.CARTOON,
                seed = 1,
            ),
        )
        // Mode (tie -> smallest natural code, black C1): a real bead, not the gray.
        assertEquals("C1", project.grid[0, 0])
        // The reference image still averages black+white to a light midtone.
        val r = (project.referenceGrid!!.toImage().argb(0, 0) shr 16) and 0xFF
        assertTrue("reference average ~188, was $r", r in 186..190)
    }

    @Test
    fun `photo boundary cell averages rather than takes the mode`() {
        val image = PixelImage(2, 1, intArrayOf(0xFF000000.toInt(), 0xFFFFFFFF.toInt()))
        val project = PatternGenerator().generate(
            image,
            GenerateConfig(
                gridWidth = 1,
                gridHeight = 1,
                palette = TestPalettes.basic(),
                colorCountTarget = 2,
                colorCountAuto = true,
                imageType = ImageType.PHOTO,
                seed = 1,
            ),
        )
        // Average ~188 is nearer white (C2) than black (C1).
        assertEquals("C2", project.grid[0, 0])
    }

    @Test
    fun `reference marks empty holes transparent`() {
        val width = 4
        val height = 2
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (x < width / 2) 0x00000000 else 0xFFFF0000.toInt()
            }
        }
        val generator = PatternGenerator()
        val project = generator.generate(
            PixelImage(width, height, pixels),
            GenerateConfig(
                gridWidth = 2,
                gridHeight = 1,
                palette = TestPalettes.basic(),
                colorCountTarget = 2,
                imageType = ImageType.CARTOON,
                seed = 1,
            ),
        )
        val reference = project.referenceGrid!!.toImage()
        assertEquals(0, (reference.argb(0, 0) ushr 24) and 0xFF) // hole
        assertEquals(255, (reference.argb(1, 0) ushr 24) and 0xFF) // valid
    }

    @Test
    fun `reuses a provided reference and yields the same grid`() {
        val generator = PatternGenerator()
        val config = GenerateConfig(
            gridWidth = 8,
            gridHeight = 8,
            palette = TestPalettes.withMany(),
            colorCountTarget = 4,
            imageType = ImageType.CARTOON,
            seed = 7,
        )
        val fresh = generator.generate(bandsImage(), config)
        val reused = generator.generate(bandsImage(), config, fresh.referenceGrid!!.toImage())

        assertEquals(fresh.grid.cells, reused.grid.cells)
        assertEquals(fresh.referenceGrid, reused.referenceGrid)
    }

    @Test
    fun `attached reference matches ReferenceBuilder output`() {
        // No-op processor so the reference is built from the raw working image.
        val generator = PatternGenerator()
        val config = GenerateConfig(
            gridWidth = 2,
            gridHeight = 2,
            palette = TestPalettes.basic(),
            colorCountTarget = 2,
            imageType = ImageType.CARTOON,
            seed = 1,
        )
        val image = bandsImage()
        val project = generator.generate(image, config)

        val expected = ReferenceBuilder.build(image, config.gridWidth, config.gridHeight).toReferenceGrid()
        assertEquals(expected, project.referenceGrid)
    }

    @Test
    fun `attached reference matches ReferenceBuilder output with holes`() {
        val width = 4
        val height = 2
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (x < width / 2) 0x00000000 else 0xFFFF0000.toInt()
            }
        }
        val image = PixelImage(width, height, pixels)
        val generator = PatternGenerator()
        val config = GenerateConfig(
            gridWidth = 2,
            gridHeight = 1,
            palette = TestPalettes.basic(),
            colorCountTarget = 2,
            imageType = ImageType.CARTOON,
            seed = 1,
        )
        val project = generator.generate(image, config)

        val expected = ReferenceBuilder.build(image, config.gridWidth, config.gridHeight).toReferenceGrid()
        assertEquals(expected, project.referenceGrid)
    }

    @Test
    fun `reference-direct is the default strategy`() {
        val generator = PatternGenerator()
        val project = generator.generate(
            bandsImage(),
            GenerateConfig(
                gridWidth = 4,
                gridHeight = 4,
                palette = TestPalettes.withMany(),
                colorCountTarget = 2,
                colorCountAuto = false,
                seed = 1,
            ),
        )
        // The two bands map only to black and red; automatic is off so N applies, both fit.
        assertEquals("reference", project.meta.colorStrategy)
        assertEquals(setOf("C1", "C3"), project.paletteUsed.map { it.code }.toSet())
    }

    @Test
    fun `automatic reference-direct keeps every bead that appears`() {
        val generator = PatternGenerator()
        val project = generator.generate(
            bandsImage(),
            GenerateConfig(
                gridWidth = 4,
                gridHeight = 4,
                palette = TestPalettes.withMany(),
                colorCountTarget = 1,
                colorCountAuto = true,
            ),
        )
        // Even with a tiny target, automatic is uncapped so both bands survive.
        assertEquals(setOf("C1", "C3"), project.paletteUsed.map { it.code }.toSet())
    }

    @Test
    fun `k-means strategy remains selectable`() {
        val generator = PatternGenerator()
        val project = generator.generate(
            bandsImage(),
            GenerateConfig(
                gridWidth = 2,
                gridHeight = 1,
                palette = TestPalettes.basic(),
                colorCountTarget = 2,
                colorStrategy = "kmeans",
                colorCountAuto = false,
                seed = 1,
            ),
        )
        assertEquals("kmeans", project.meta.colorStrategy)
        assertEquals(2, project.paletteUsed.size)
    }
}
