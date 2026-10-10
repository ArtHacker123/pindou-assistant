package com.pindou.core.generate

import com.pindou.core.color.ColorSpace
import com.pindou.core.color.LabColorSpace
import com.pindou.core.match.PaletteMapper
import com.pindou.core.model.Grid
import com.pindou.core.model.PindouProject
import com.pindou.core.model.PixelImage
import com.pindou.core.model.ProjectMeta
import com.pindou.core.processor.ImageProcessorRegistry
import com.pindou.core.quant.QuantConfig
import com.pindou.core.quant.QuantizerRegistry
import com.pindou.core.sampler.Sampler
import com.pindou.core.sampler.UniformSampler
import com.pindou.core.util.sortedNaturallyBy

/**
 * Turns a working image into a [PindouProject]:
 *
 *  1. preprocess according to the image-type preset
 *  2. sample pixels and extract N representative colors (quantizer)
 *  3. map each representative color to a real bead color (palette) — this fixes
 *     the set of beads the pattern may use
 *  4. map every grid cell directly to the perceptually closest bead among that
 *     used set
 *
 * Every stage is pluggable, so new processors, quantizers, samplers or color
 * spaces can be added without modifying this class.
 */
class PatternGenerator(
    private val quantizers: QuantizerRegistry = QuantizerRegistry.default(),
    private val processors: ImageProcessorRegistry = ImageProcessorRegistry.default(),
    private val sampler: Sampler = UniformSampler(),
    private val space: ColorSpace = LabColorSpace,
) {

    private val mapper = PaletteMapper(space)

    fun generate(image: PixelImage, config: GenerateConfig): PindouProject {
        require(config.gridWidth in 1..MAX_GRID) { "gridWidth must be 1..$MAX_GRID" }
        require(config.gridHeight in 1..MAX_GRID) { "gridHeight must be 1..$MAX_GRID" }
        require(config.palette.items.isNotEmpty()) { "Palette must not be empty" }

        val processed = processors.get(config.imageType.id).process(image)
        val samples = sampler.sample(processed, config.maxSamples)
        val k = config.colorCountTarget.coerceIn(1, config.palette.size)
        val centers = quantizers.get(config.quantizerId)
            .quantize(samples, k, QuantConfig(seed = config.seed))

        // Stage 3 decides which beads the pattern may use.
        val usedBeads = mapper.mapCenters(centers, config.palette).distinctBy { it.code }
        val usedRgb = usedBeads.map { it.rgb }

        val cells = ArrayList<String?>(config.gridWidth * config.gridHeight)
        for (row in 0 until config.gridHeight) {
            for (col in 0 until config.gridWidth) {
                if (AreaColor.coverage(processed, config.gridWidth, config.gridHeight, row, col) < MIN_CELL_VALID) {
                    cells.add(null)
                    continue
                }
                // Stage 4: snap the cell straight to its closest used bead. Going
                // via the representative center instead can commit a cell to a
                // visibly farther bead, so we skip that indirection.
                val avg = AreaColor.average(processed, config.gridWidth, config.gridHeight, row, col)
                val idx = space.nearestIndex(avg, usedRgb)
                cells.add(if (idx >= 0) usedBeads[idx].code else config.palette.items.first().code)
            }
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
        )
    }

    companion object {
        const val MAX_GRID = 128
        const val MIN_GRID = 1

        /** A cell becomes an empty hole when its source region is less than this fraction valid. */
        const val MIN_CELL_VALID = 0.5f
    }
}
