package com.pindou.core.quant

import com.pindou.core.color.ColorSpace
import com.pindou.core.color.OklabColorSpace
import com.pindou.core.model.Rgb
import kotlin.random.Random

/**
 * K-means color quantization with k-means++ seeding.
 *
 * Clustering runs in a [ColorSpace] (Oklab by default) so that centroids follow
 * perceptual grouping rather than raw sRGB arithmetic. The algorithm is
 * deterministic when a [QuantConfig.seed] is supplied, which lets the app
 * reproduce the exact same pattern for the same input.
 */
class KMeansQuantizer(
    private val space: ColorSpace = OklabColorSpace,
) : Quantizer {

    override val id: String = ID
    override val displayName: String = "K-means"

    override fun quantize(samples: List<Rgb>, k: Int, config: QuantConfig): List<Rgb> {
        if (samples.isEmpty()) return emptyList()
        val target = k.coerceIn(1, samples.size)
        val random = config.seed?.let { Random(it) } ?: Random.Default

        val encoded = samples.map { space.encode(it) }
        val centers = seedCenters(encoded, target, random).toMutableList()

        repeat(config.maxIterations) {
            val sums = Array(target) { DoubleArray(3) }
            val counts = IntArray(target)

            for (sample in encoded) {
                val idx = nearestIndex(sample, centers)
                sums[idx][0] += sample[0]
                sums[idx][1] += sample[1]
                sums[idx][2] += sample[2]
                counts[idx] = counts[idx] + 1
            }

            var totalMovement = 0.0
            for (c in centers.indices) {
                if (counts[c] == 0) continue
                val mean = doubleArrayOf(
                    sums[c][0] / counts[c],
                    sums[c][1] / counts[c],
                    sums[c][2] / counts[c],
                )
                totalMovement += space.squaredDistance(centers[c], mean)
                centers[c] = mean
            }

            if (totalMovement <= config.convergenceEpsilon) return centers.map { space.decode(it) }
        }
        return centers.map { space.decode(it) }
    }

    private fun nearestIndex(target: DoubleArray, centers: List<DoubleArray>): Int {
        var best = 0
        var bestDistance = Double.MAX_VALUE
        for (i in centers.indices) {
            val d = space.squaredDistance(target, centers[i])
            if (d < bestDistance) {
                bestDistance = d
                best = i
            }
        }
        return best
    }

    /** k-means++ seeding: spread initial centers out for stable, good results. */
    private fun seedCenters(samples: List<DoubleArray>, k: Int, random: Random): List<DoubleArray> {
        val centers = ArrayList<DoubleArray>(k)
        centers.add(samples[random.nextInt(samples.size)])
        val minDist = DoubleArray(samples.size) { Double.MAX_VALUE }

        while (centers.size < k) {
            val last = centers.last()
            var total = 0.0
            for (i in samples.indices) {
                val d = space.squaredDistance(samples[i], last)
                if (d < minDist[i]) minDist[i] = d
                total += minDist[i]
            }
            if (total <= 0.0) {
                while (centers.size < k) centers.add(samples[random.nextInt(samples.size)])
                break
            }
            var threshold = random.nextDouble() * total
            var chosen = samples.size - 1
            for (i in samples.indices) {
                threshold -= minDist[i]
                if (threshold <= 0.0) {
                    chosen = i
                    break
                }
            }
            centers.add(samples[chosen])
        }
        return centers
    }

    companion object {
        const val ID = "kmeans"
    }
}
