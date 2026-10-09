package com.pindou.core.color

import com.pindou.core.model.Rgb

/**
 * A color difference metric. Implementations return a squared distance so the
 * hot path can avoid square roots. Swapping in a perceptual metric (for example
 * CIELAB dE) later requires no change to the matching pipeline.
 */
fun interface ColorDistance {
    fun squaredDistance(a: Rgb, b: Rgb): Long
}

/** Plain RGB Euclidean distance. Fast and adequate for the first version. */
object RgbEuclideanDistance : ColorDistance {
    override fun squaredDistance(a: Rgb, b: Rgb): Long {
        val dr = (a.r - b.r).toLong()
        val dg = (a.g - b.g).toLong()
        val db = (a.b - b.b).toLong()
        return dr * dr + dg * dg + db * db
    }
}
