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
        val w = image.width
        var i = 0
        while (i < total) {
            if (image.valid(i % w, i / w)) {
                result.add(Rgb.fromArgb(image.pixels[i]))
            }
            i += step
        }
        return result
    }
}
