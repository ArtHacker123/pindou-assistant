package com.pindou.core.sampler

import com.pindou.core.model.PixelImage
import com.pindou.core.model.Rgb

/** Produces a bounded set of representative pixels from an image. */
interface Sampler {
    fun sample(image: PixelImage, maxSamples: Int): List<Rgb>
}

/**
 * Uniform stride sampling. Sampling a few thousand pixels instead of the full
 * image keeps quantization fast while preserving the dominant color distribution.
 */
class UniformSampler : Sampler {
    override fun sample(image: PixelImage, maxSamples: Int): List<Rgb> {
        require(maxSamples > 0) { "maxSamples must be positive" }
        val total = image.size
        val step = maxOf(1, total / maxSamples)
        val result = ArrayList<Rgb>(minOf(total, maxSamples) + 1)
        var i = 0
        while (i < total) {
            val argb = image.pixels[i]
            if (((argb ushr 24) and 0xFF) >= 8) {
                result.add(Rgb.fromArgb(argb))
            }
            i += step
        }
        return result
    }
}
