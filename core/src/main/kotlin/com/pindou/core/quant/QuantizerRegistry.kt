package com.pindou.core.quant

/**
 * Resolves quantizers by id so new algorithms can be plugged in without
 * touching the generation pipeline.
 */
class QuantizerRegistry(quantizers: List<Quantizer>) {
    private val byId: Map<String, Quantizer> = quantizers.associateBy { it.id }

    val ids: List<String> get() = byId.keys.toList()

    fun get(id: String): Quantizer =
        byId[id] ?: error("Unknown quantizer id: $id")

    fun all(): List<Quantizer> = byId.values.toList()

    companion object {
        const val DEFAULT_ID = KMeansQuantizer.ID

        fun default(): QuantizerRegistry = QuantizerRegistry(listOf(KMeansQuantizer()))
    }
}
