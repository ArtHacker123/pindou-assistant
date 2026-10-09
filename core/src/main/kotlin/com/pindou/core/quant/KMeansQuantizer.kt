package com.pindou.core.quant

import com.pindou.core.color.ColorDistance
import com.pindou.core.color.RgbEuclideanDistance
import com.pindou.core.model.Rgb
import kotlin.random.Random

/**
 * K-means color quantization with k-means++ seeding.
 *
 * The algorithm is deterministic when a [QuantConfig.seed] is supplied, which
 * lets the app reproduce the exact same pattern for the same input.
 */
class KMeansQuantizer(
    private val distance: ColorDistance = RgbEuclideanDistance,
) : Quantizer {

    override val id: String = ID
    override val displayName: String = "K-means"

    override fun quantize(samples: List<Rgb>, k: Int, config: QuantConfig): List<Rgb> {
        if (samples.isEmpty()) return emptyList()
        val target = k.coerceIn(1, samples.size)
        val random = config.seed?.let { Random(it) } ?: Random.Default

        val centers = seedCenters(samples, target, random).toMutableList()

        repeat(config.maxIterations) {
            val sumR = LongArray(target)
            val sumG = LongArray(target)
            val sumB = LongArray(target)
            val counts = IntArray(target)

            for (sample in samples) {
                val idx = nearestIndex(sample, centers)
                sumR[idx] = sumR[idx] + sample.r.toLong()
                sumG[idx] = sumG[idx] + sample.g.toLong()
                sumB[idx] = sumB[idx] + sample.b.toLong()
                counts[idx] = counts[idx] + 1
            }

            var totalMovement = 0.0
            for (c in centers.indices) {
                if (counts[c] == 0) continue
                val nr = (sumR[c] / counts[c]).toInt()
                val ng = (sumG[c] / counts[c]).toInt()
                val nb = (sumB[c] / counts[c]).toInt()
                totalMovement += distance.squaredDistance(centers[c], Rgb(nr, ng, nb)).toDouble()
                centers[c] = Rgb(nr, ng, nb)
            }

            if (totalMovement <= config.convergenceEpsilon) return centers
        }
        return centers
    }

    private fun nearestIndex(target: Rgb, centers: List<Rgb>): Int {
        var best = 0
        var bestDistance = Long.MAX_VALUE
        for (i in centers.indices) {
            val d = distance.squaredDistance(target, centers[i])
            if (d < bestDistance) {
                bestDistance = d
                best = i
            }
        }
        return best
    }

    /** k-means++ seeding: spread initial centers out for stable, good results. */
    private fun seedCenters(samples: List<Rgb>, k: Int, random: Random): List<Rgb> {
        val centers = ArrayList<Rgb>(k)
        centers.add(samples[random.nextInt(samples.size)])
        val minDist = DoubleArray(samples.size) { Double.MAX_VALUE }

        while (centers.size < k) {
            val last = centers.last()
            var total = 0.0
            for (i in samples.indices) {
                val d = distance.squaredDistance(samples[i], last).toDouble()
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
