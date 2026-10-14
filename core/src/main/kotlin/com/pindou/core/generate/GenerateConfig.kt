package com.pindou.core.generate

import com.pindou.core.model.Palette
import com.pindou.core.match.ColorStrategyRegistry
import com.pindou.core.imaging.ImageType

/** All inputs required to turn a working image into a [PindouProject]. */
data class GenerateConfig(
    val gridWidth: Int,
    val gridHeight: Int,
    val palette: Palette,
    val colorCountTarget: Int,
    val imageType: ImageType = ImageType.CARTOON,
    /** Color-selection strategy id; defaults to reference-direct. */
    val colorStrategy: String = ColorStrategyRegistry.DEFAULT_ID,
    /** Whether the color count is "automatic" (uncapped for reference-direct). */
    val colorCountAuto: Boolean = false,
    val quantizerId: String = "kmeans",
    /** When set, generation is reproducible. Usually derived from the source hash. */
    val seed: Long? = null,
    val maxSamples: Int = 8000,
    val name: String = "",
    val createdAt: Long = 0L,
    val sourceHash: String? = null,
)
