package com.pindou.core.color

import com.pindou.core.model.Rgb
import kotlin.math.cbrt

/**
 * Oklab (Björn Ottosson) with a plain Euclidean metric.
 *
 * Oklab is a perceptual color space whose Euclidean distance tracks perceived
 * difference better than raw sRGB and, unlike CIELAB with the CIE76 metric,
 * stays reasonably uniform for saturated colors and blues. Because it is a
 * true Euclidean space it drops straight into [ColorSpace], so nearest-color
 * lookups, greedy color selection and k-means need no changes. It also mixes
 * (averages) more cleanly than CIELAB, which is a bonus for k-means centroids.
 */
object OklabColorSpace : ColorSpace {
    override val id = "oklab"

    override fun encode(rgb: Rgb): DoubleArray {
        val r = Srgb.toLinear(rgb.r)
        val g = Srgb.toLinear(rgb.g)
        val b = Srgb.toLinear(rgb.b)

        val l = 0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b
        val m = 0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b
        val s = 0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b

        val lc = cbrt(l)
        val mc = cbrt(m)
        val sc = cbrt(s)

        return doubleArrayOf(
            0.2104542553 * lc + 0.7936177850 * mc - 0.0040720468 * sc,
            1.9779984951 * lc - 2.4285922050 * mc + 0.4505937099 * sc,
            0.0259040371 * lc + 0.7827717662 * mc - 0.8086757660 * sc,
        )
    }

    override fun decode(coord: DoubleArray): Rgb {
        val l = coord[0]
        val a = coord[1]
        val b = coord[2]

        val lc = l + 0.3963377774 * a + 0.2158037573 * b
        val mc = l - 0.1055613458 * a - 0.0638541728 * b
        val sc = l - 0.0894841775 * a - 1.2914855480 * b

        val l3 = lc * lc * lc
        val m3 = mc * mc * mc
        val s3 = sc * sc * sc

        val r = 4.0767416621 * l3 - 3.3077115913 * m3 + 0.2309699292 * s3
        val g = -1.2684380046 * l3 + 2.6097574011 * m3 - 0.3413193965 * s3
        val bl = -0.0041960863 * l3 - 0.7034186147 * m3 + 1.7076147010 * s3

        return Rgb(Srgb.fromLinear(r), Srgb.fromLinear(g), Srgb.fromLinear(bl))
    }
}
