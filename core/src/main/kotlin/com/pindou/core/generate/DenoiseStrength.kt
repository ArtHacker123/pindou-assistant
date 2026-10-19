package com.pindou.core.generate

/**
 * How aggressively [GridRefiner] merges speckle. A higher strength increases both
 * the largest region it will merge ([maxArea]) and the largest perceived colour
 * difference it will merge across ([maxDelta], an Oklab Euclidean distance).
 *
 * [OFF] performs no pass; it is the value [PatternGenerator] receives when denoise
 * is disabled and is not offered as a strength choice (the UI exposes a switch
 * plus [SELECTABLE]).
 */
enum class DenoiseStrength(
    val id: String,
    val displayName: String,
    val maxArea: Int,
    val maxDelta: Double,
) {
    OFF("off", "关", 0, 0.0),
    WEAK("weak", "弱", 1, 0.03),
    MEDIUM("medium", "中", 2, 0.06),
    STRONG("strong", "强", 4, 0.10),
    ;

    /** [maxDelta] squared, so gating compares plain squared distances. */
    val maxDeltaSq: Double get() = maxDelta * maxDelta

    companion object {
        /** The strengths offered to the user (everything except [OFF]). */
        val SELECTABLE: List<DenoiseStrength> = listOf(WEAK, MEDIUM, STRONG)

        /** Default when enabled. */
        val DEFAULT: DenoiseStrength = MEDIUM

        fun fromId(id: String): DenoiseStrength = values().firstOrNull { it.id == id } ?: DEFAULT
    }
}
