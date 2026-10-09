package com.pindou.core.color

import com.pindou.core.model.Rgb

/** Nearest-neighbor lookup used for both center->palette and cell->center mapping. */
object NearestColor {
    /** Index of the candidate closest to [target], or -1 when [candidates] is empty. */
    fun indexOf(
        target: Rgb,
        candidates: List<Rgb>,
        distance: ColorDistance = RgbEuclideanDistance,
    ): Int {
        var best = -1
        var bestDistance = Long.MAX_VALUE
        for (i in candidates.indices) {
            val d = distance.squaredDistance(target, candidates[i])
            if (d < bestDistance) {
                bestDistance = d
                best = i
            }
        }
        return best
    }
}
