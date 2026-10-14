package com.pindou.core.match

/**
 * Resolves color-selection strategies by id so the chosen approach can be
 * swapped without touching the generation pipeline. Unknown ids fall back to the
 * default (reference-direct), which keeps persisted metadata forward-compatible.
 */
class ColorStrategyRegistry(strategies: List<ColorStrategy>) {

    private val byId: Map<String, ColorStrategy> = strategies.associateBy { it.id }

    fun get(id: String): ColorStrategy = byId[id] ?: byId.getValue(DEFAULT_ID)

    fun all(): List<ColorStrategy> = byId.values.toList()

    companion object {
        const val DEFAULT_ID = ReferenceDirectStrategy.ID

        fun default(): ColorStrategyRegistry =
            ColorStrategyRegistry(listOf(ReferenceDirectStrategy(), KMeansStrategy()))
    }
}
