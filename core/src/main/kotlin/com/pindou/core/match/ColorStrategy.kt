package com.pindou.core.match

import com.pindou.core.imaging.GridSamples
import com.pindou.core.model.Palette
import com.pindou.core.model.PaletteItem
import com.pindou.core.model.PixelImage

/**
 * Tuning inputs shared by all color-selection strategies.
 *
 * [auto] is the user's "automatic" choice: for reference-direct it means "keep
 * every bead that appears"; for K-means it means the default count. The
 * remaining fields are only consulted by K-means.
 */
data class ColorSelectConfig(
    val colorCountTarget: Int,
    val auto: Boolean,
    val seed: Long? = null,
    val maxSamples: Int = 8000,
    val quantizerId: String = "kmeans",
)

/**
 * Chooses the set of bead colors a pattern may use. This is the "which beads do
 * we end up with" stage, pluggable so a new approach (e.g. selecting directly
 * from the reference image) can be swapped in without touching the pipeline.
 *
 * A strategy returns the distinct bead items the pattern may use; the pipeline
 * then snaps every grid cell to the nearest of those beads.
 */
interface ColorStrategy {
    val id: String
    val displayName: String

    /**
     * @param image the preprocessed working image (used by K-means sampling)
     * @param samples the grid-resolution cell samples (used by reference-direct)
     */
    fun select(
        image: PixelImage,
        samples: GridSamples,
        palette: Palette,
        config: ColorSelectConfig,
    ): List<PaletteItem>
}
