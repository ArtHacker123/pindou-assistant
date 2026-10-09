package com.pindou.core.quant

import com.pindou.core.model.Rgb

/** Tuning options shared by all quantizers. */
data class QuantConfig(
    val maxIterations: Int = 20,
    /** When set, quantization is deterministic and reproducible. */
    val seed: Long? = null,
    /** Stop early when total center movement drops below this value. */
    val convergenceEpsilon: Double = 0.5,
)

/**
 * Extracts [k] representative image colors from a sample set. This is the
 * "how much do we simplify the image" stage and is intentionally independent of
 * the bead palette (which is applied later).
 */
interface Quantizer {
    val id: String
    val displayName: String
    fun quantize(samples: List<Rgb>, k: Int, config: QuantConfig = QuantConfig()): List<Rgb>
}
