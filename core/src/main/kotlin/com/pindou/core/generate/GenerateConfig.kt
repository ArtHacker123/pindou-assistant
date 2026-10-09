package com.pindou.core.generate

import com.pindou.core.model.Palette
import com.pindou.core.processor.ImageType

/** All inputs required to turn a working image into a [PindouProject]. */
data class GenerateConfig(
    val gridWidth: Int,
    val gridHeight: Int,
    val palette: Palette,
    val colorCountTarget: Int,
    val imageType: ImageType = ImageType.CARTOON,
    val quantizerId: String = "kmeans",
    /** When set, generation is reproducible. Usually derived from the source hash. */
    val seed: Long? = null,
    val maxSamples: Int = 8000,
    val name: String = "",
    val createdAt: Long = 0L,
    val sourceHash: String? = null,
)
