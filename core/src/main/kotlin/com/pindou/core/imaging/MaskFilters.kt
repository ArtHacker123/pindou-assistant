package com.pindou.core.imaging

/**
 * Grayscale morphology on a soft [FloatMask].
 *
 * Operating on the probability map (rather than a binarized one) keeps soft
 * subject edges instead of cutting them at a hard threshold first. All operators
 * are pure and JVM-testable, and compose: [open] and [close] are built from
 * [erode] and [dilate].
 */
object MaskFilters {

    /** Minimum over a square neighborhood of [radius] (shrinks foreground). */
    fun erode(mask: FloatMask, radius: Int): FloatMask =
        neighborhood(mask, radius, Float.MAX_VALUE) { a, b -> minOf(a, b) }

    /** Maximum over a square neighborhood of [radius] (grows foreground). */
    fun dilate(mask: FloatMask, radius: Int): FloatMask =
        neighborhood(mask, radius, -Float.MAX_VALUE) { a, b -> maxOf(a, b) }

    /** Erosion then dilation: removes foreground regions smaller than the radius. */
    fun open(mask: FloatMask, radius: Int): FloatMask = dilate(erode(mask, radius), radius)

    /** Dilation then erosion: fills background holes smaller than the radius. */
    fun close(mask: FloatMask, radius: Int): FloatMask = erode(dilate(mask, radius), radius)

    /** Applies [op] to every pixel over a square window, clamped at the borders. */
    private inline fun neighborhood(
        mask: FloatMask,
        radius: Int,
        identity: Float,
        op: (Float, Float) -> Float,
    ): FloatMask {
        require(radius >= 1) { "radius must be >= 1" }
        val w = mask.width
        val h = mask.height
        val src = mask.values
        val out = FloatArray(w * h)
        for (y in 0 until h) {
            val y0 = (y - radius).coerceAtLeast(0)
            val y1 = (y + radius).coerceAtMost(h - 1)
            for (x in 0 until w) {
                val x0 = (x - radius).coerceAtLeast(0)
                val x1 = (x + radius).coerceAtMost(w - 1)
                var acc = identity
                for (yy in y0..y1) {
                    val row = yy * w
                    for (xx in x0..x1) {
                        acc = op(acc, src[row + xx])
                    }
                }
                out[y * w + x] = acc
            }
        }
        return FloatMask(w, h, out)
    }
}
