package com.pindou.core.imaging

import com.pindou.core.color.ColorSpace
import com.pindou.core.color.OklabColorSpace
import com.pindou.core.model.Palette
import com.pindou.core.model.Rgb

/**
 * A fast RGB -> nearest-palette-index lookup, used by the per-cell **mode**
 * reduction. Each channel is quantized to 5 bits (32 levels) and the nearest
 * palette entry for every one of the 32^3 bins is precomputed once using
 * [space]. Bins are only used to *vote*: the winning index is always a real
 * palette entry, never a synthesized color, which is exactly what keeps cartoon
 * line art from picking invented mid-tones.
 */
class PaletteLut private constructor(private val indices: IntArray) {

    /** Nearest palette entry index (into the palette's item list) for an sRGB color. */
    fun indexOf(r: Int, g: Int, b: Int): Int = indices[bin(r, g, b)]

    companion object {
        /** Bits per channel; 5 keeps the table at 32*32*32 ints (~128 KB). */
        const val BITS = 5
        private const val LEVELS = 1 shl BITS
        private const val SHIFT = 8 - BITS
        private const val CENTER = 1 shl (SHIFT - 1)

        fun build(palette: Palette, space: ColorSpace = OklabColorSpace): PaletteLut {
            require(palette.items.isNotEmpty()) { "Palette must not be empty" }
            val encoded = palette.items.map { space.encode(it.rgb) }
            val indices = IntArray(LEVELS * LEVELS * LEVELS)
            var i = 0
            for (r in 0 until LEVELS) {
                val rr = expand(r)
                for (g in 0 until LEVELS) {
                    val gg = expand(g)
                    for (b in 0 until LEVELS) {
                        val bb = expand(b)
                        indices[i++] = nearest(space.encode(Rgb(rr, gg, bb)), encoded)
                    }
                }
            }
            return PaletteLut(indices)
        }

        /** Mid-point of a 5-bit bin, so each level stands for its own range. */
        private fun expand(level: Int): Int = (level shl SHIFT) or CENTER

        private fun bin(r: Int, g: Int, b: Int): Int =
            ((r shr SHIFT) shl (BITS * 2)) or ((g shr SHIFT) shl BITS) or (b shr SHIFT)

        private fun nearest(target: DoubleArray, candidates: List<DoubleArray>): Int {
            var best = 0
            var bestDistance = Double.MAX_VALUE
            for (i in candidates.indices) {
                val c = candidates[i]
                val dx = target[0] - c[0]
                val dy = target[1] - c[1]
                val dz = target[2] - c[2]
                val d = dx * dx + dy * dy + dz * dz
                if (d < bestDistance) {
                    bestDistance = d
                    best = i
                }
            }
            return best
        }
    }
}
