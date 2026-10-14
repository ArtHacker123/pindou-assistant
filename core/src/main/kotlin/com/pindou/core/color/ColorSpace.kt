package com.pindou.core.color

import com.pindou.core.model.Rgb
import kotlin.math.cbrt
import kotlin.math.roundToInt

/**
 * A color model used for averaging and nearest-color comparison. Coordinates are
 * three doubles in a space whose Euclidean distance approximates perceived
 * difference, so clustering and bead matching line up with human vision instead
 * of raw sRGB numbers.
 */
interface ColorSpace {

    val id: String

    /** Encodes an sRGB color into this space's working coordinates. */
    fun encode(rgb: Rgb): DoubleArray

    /** Decodes working coordinates back into a clipped sRGB color. */
    fun decode(coord: DoubleArray): Rgb

    /** Perceived squared distance between two colors. */
    fun squaredDistance(a: Rgb, b: Rgb): Double = squaredDistance(encode(a), encode(b))

    /** Squared Euclidean distance between two encoded colors. */
    fun squaredDistance(a: DoubleArray, b: DoubleArray): Double {
        val dx = a[0] - b[0]
        val dy = a[1] - b[1]
        val dz = a[2] - b[2]
        return dx * dx + dy * dy + dz * dz
    }

    /** Index of the candidate closest to [target], or -1 when [candidates] is empty. */
    fun nearestIndex(target: Rgb, candidates: List<Rgb>): Int {
        if (candidates.isEmpty()) return -1
        val t = encode(target)
        var best = 0
        var bestDistance = Double.MAX_VALUE
        for (i in candidates.indices) {
            val d = squaredDistance(t, encode(candidates[i]))
            if (d < bestDistance) {
                bestDistance = d
                best = i
            }
        }
        return best
    }

    /**
     * Index of the candidate closest to an already-encoded [target], or -1 when
     * [candidates] is empty. Use this with cached encodings to avoid re-encoding
     * the same colors on every lookup.
     */
    fun nearestIndex(target: DoubleArray, candidates: List<DoubleArray>): Int {
        if (candidates.isEmpty()) return -1
        var best = 0
        var bestDistance = Double.MAX_VALUE
        for (i in candidates.indices) {
            val d = squaredDistance(target, candidates[i])
            if (d < bestDistance) {
                bestDistance = d
                best = i
            }
        }
        return best
    }
}

/** Identity sRGB coordinates with plain Euclidean distance: a fast reference metric. */
object RgbColorSpace : ColorSpace {
    override val id = "rgb"

    override fun encode(rgb: Rgb): DoubleArray =
        doubleArrayOf(rgb.r.toDouble(), rgb.g.toDouble(), rgb.b.toDouble())

    override fun decode(coord: DoubleArray): Rgb = Rgb(
        coord[0].roundToInt().coerceIn(0, 255),
        coord[1].roundToInt().coerceIn(0, 255),
        coord[2].roundToInt().coerceIn(0, 255),
    )
}

/**
 * CIELAB with a D65 white point and the CIE76 difference metric (dE76).
 *
 * This is the pipeline default: equal distances here track human perception far
 * better than raw sRGB, so the chosen nearest bead and the k-means centroids are
 * more faithful. Brightness is deliberately handled in the linear part of the
 * conversion, so averages do not drift dark.
 */
object LabColorSpace : ColorSpace {
    override val id = "lab"

    // D65 reference white (X, Y, Z normalised to Y = 1).
    private const val XN = 0.95047
    private const val YN = 1.00000
    private const val ZN = 1.08883

    private const val DELTA3 = 0.008856 // (6/29)^3
    private const val KAPPA = 7.787     // linear-segment slope

    override fun encode(rgb: Rgb): DoubleArray {
        val r = Srgb.toLinear(rgb.r)
        val g = Srgb.toLinear(rgb.g)
        val b = Srgb.toLinear(rgb.b)
        val x = 0.4124564 * r + 0.3575761 * g + 0.1804375 * b
        val y = 0.2126729 * r + 0.7151522 * g + 0.0721750 * b
        val z = 0.0193339 * r + 0.1191920 * g + 0.9503041 * b
        val fx = f(x / XN)
        val fy = f(y / YN)
        val fz = f(z / ZN)
        return doubleArrayOf(116.0 * fy - 16.0, 500.0 * (fx - fy), 200.0 * (fy - fz))
    }

    override fun decode(coord: DoubleArray): Rgb {
        val fy = (coord[0] + 16.0) / 116.0
        val fx = fy + coord[1] / 500.0
        val fz = fy - coord[2] / 200.0
        val x = XN * fInv(fx)
        val y = YN * fInv(fy)
        val z = ZN * fInv(fz)
        val r = 3.2404542 * x - 1.5371385 * y - 0.4985314 * z
        val g = -0.9692660 * x + 1.8760108 * y + 0.0415560 * z
        val b = 0.0556434 * x - 0.2040259 * y + 1.0572252 * z
        return Rgb(Srgb.fromLinear(r), Srgb.fromLinear(g), Srgb.fromLinear(b))
    }

    private fun f(t: Double): Double =
        if (t > DELTA3) cbrt(t) else KAPPA * t + 16.0 / 116.0

    private fun fInv(t: Double): Double {
        val t3 = t * t * t
        return if (t3 > DELTA3) t3 else (t - 16.0 / 116.0) / KAPPA
    }
}
