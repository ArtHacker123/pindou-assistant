package com.pindou.core.generate

import com.pindou.core.color.ColorSpace
import com.pindou.core.color.LabColorSpace
import com.pindou.core.imaging.GridSampler
import com.pindou.core.imaging.ReferenceBuilder
import com.pindou.core.imaging.Sampler
import com.pindou.core.imaging.UniformSampler
import com.pindou.core.match.PaletteMapper
import com.pindou.core.model.Grid
import com.pindou.core.model.PindouProject
import com.pindou.core.model.PixelImage
import com.pindou.core.model.ProjectMeta
import com.pindou.core.model.toReferenceGrid
import com.pindou.core.quant.QuantConfig
import com.pindou.core.quant.QuantizerRegistry
import com.pindou.core.util.sortedNaturallyBy

/**
 * Turns a **preprocessed** working image into a [PindouProject]:
 *
 *  1. sample pixels and extract N representative colors (quantizer)
 *  2. map each representative color to a real bead color (palette) — this fixes
 *     the set of beads the pattern may use
 *  3. map every grid cell directly to the perceptually closest bead among that
 *     used set
 *
 * Preprocessing is intentionally **not** done here: it belongs to the shared
 * imaging path (`ReferenceService` / `ImageProcessorRegistry`) so the same
 * preprocessed image feeds both the reference and the pattern without running
 * the image-type preset twice. Every remaining stage is pluggable.
 */
class PatternGenerator(
    private val quantizers: QuantizerRegistry = QuantizerRegistry.default(),
    private val sampler: Sampler = UniformSampler(),
    private val space: ColorSpace = LabColorSpace,
) {

    private val mapper = PaletteMapper(space)

    /**
     * Turns the already-preprocessed [image] into a [PindouProject]. Pass an
     * existing grid-resolution [reference] (built by `ReferenceService` for the
     * same preprocessed image and grid size) to skip the per-cell averaging
     * pass: it is reused for both the emitted reference image and cell snapping.
     * It is ignored when its dimensions do not match the requested grid.
     */
    fun generate(image: PixelImage, config: GenerateConfig, reference: PixelImage? = null): PindouProject {
        require(config.gridWidth in 1..MAX_GRID) { "gridWidth must be 1..$MAX_GRID" }
        require(config.gridHeight in 1..MAX_GRID) { "gridHeight must be 1..$MAX_GRID" }
        require(config.palette.items.isNotEmpty()) { "Palette must not be empty" }

        val samples = sampler.sample(image, config.maxSamples)
        val k = config.colorCountTarget.coerceIn(1, config.palette.size)
        val centers = quantizers.get(config.quantizerId)
            .quantize(samples, k, QuantConfig(seed = config.seed))

        // Stage 2 decides which beads the pattern may use.
        val usedBeads = mapper.mapCenters(centers, config.palette).distinctBy { it.code }
        val usedRgb = usedBeads.map { it.rgb }

        val reusable = reference?.takeIf {
            it.width == config.gridWidth && it.height == config.gridHeight
        }
        val gridSamples = if (reusable != null) {
            GridSampler.fromReference(reusable)
        } else {
            GridSampler.sample(image, config.gridWidth, config.gridHeight)
        }

        val cells = ArrayList<String?>(gridSamples.size)
        for (i in 0 until gridSamples.size) {
            if (!gridSamples.valid[i]) {
                cells.add(null)
                continue
            }
            val avg = gridSamples.avgRgb[i]
            // Stage 3: snap the cell straight to its closest used bead. Going
            // via the representative center instead can commit a cell to a
            // visibly farther bead, so we skip that indirection.
            val idx = space.nearestIndex(avg, usedRgb)
            cells.add(if (idx >= 0) usedBeads[idx].code else config.palette.items.first().code)
        }

        val grid = Grid(config.gridWidth, config.gridHeight, cells)
        val usedItems = grid.distinctCodes()
            .mapNotNull { config.palette.item(it) }
            .sortedNaturallyBy { it.code }

        val meta = ProjectMeta(
            gridWidth = config.gridWidth,
            gridHeight = config.gridHeight,
            imageType = config.imageType.id,
            paletteName = config.palette.name,
            paletteVersion = config.palette.version,
            colorCountTarget = config.colorCountTarget,
            createdAt = config.createdAt,
            name = config.name,
            sourceHash = config.sourceHash,
        )

        return PindouProject(
            meta = meta,
            paletteSource = config.palette,
            paletteUsed = usedItems,
            grid = grid,
            referenceGrid = ReferenceBuilder.build(gridSamples).toReferenceGrid(),
        )
    }

    companion object {
        const val MAX_GRID = 128
        const val MIN_GRID = 1
    }
}
