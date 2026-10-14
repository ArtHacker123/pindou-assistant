package com.pindou.core.match

import com.pindou.core.color.ColorSpace
import com.pindou.core.color.LabColorSpace
import com.pindou.core.imaging.GridSamples
import com.pindou.core.imaging.Sampler
import com.pindou.core.imaging.UniformSampler
import com.pindou.core.model.Palette
import com.pindou.core.model.PaletteItem
import com.pindou.core.model.PixelImage
import com.pindou.core.quant.QuantConfig
import com.pindou.core.quant.QuantizerRegistry

/**
 * The original color selection: sample the working image uniformly, extract
 * representative colors with a quantizer (K-means by default), then map those to
 * real beads. Kept as a selectable alternative to [ReferenceDirectStrategy].
 */
class KMeansStrategy(
    private val quantizers: QuantizerRegistry = QuantizerRegistry.default(),
    private val sampler: Sampler = UniformSampler(),
    space: ColorSpace = LabColorSpace,
) : ColorStrategy {

    private val mapper = PaletteMapper(space)

    override val id = ID
    override val displayName = "K-means 聚类"

    override fun select(
        image: PixelImage,
        samples: GridSamples,
        palette: Palette,
        config: ColorSelectConfig,
    ): List<PaletteItem> {
        if (palette.items.isEmpty()) return emptyList()
        val pixels = sampler.sample(image, config.maxSamples)
        val requested = if (config.auto) DEFAULT_AUTO_COUNT else config.colorCountTarget
        val k = requested.coerceIn(1, palette.size)
        val centers = quantizers.get(config.quantizerId).quantize(pixels, k, QuantConfig(seed = config.seed))
        return mapper.mapCenters(centers, palette).distinctBy { it.code }
    }

    companion object {
        const val ID = "kmeans"
        const val DEFAULT_AUTO_COUNT = 16
    }
}
