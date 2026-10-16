package com.pindou.core.imaging

/** How neighbouring pixels are connected when grouping a mask into regions. */
enum class MaskConnectivity { FOUR, EIGHT }

/** Which foreground regions survive subject reduction. */
enum class SubjectMode { LARGEST, ALL }

/**
 * Data-driven configuration for [MaskPostProcess].
 *
 * Every default reproduces the pre-tuning behavior — a single largest
 * four-connected region above a 0.5 threshold, a 0.5 hard validity threshold,
 * and no morphology — so callers that pass nothing are unaffected, and adding a
 * knob later only needs a new field with a default.
 */
data class MaskOptions(
    /** Saliency value at or above which a pixel counts as foreground for grouping. */
    val foregroundThreshold: Float = 0.5f,
    /** Saliency value at or above which a foreground pixel becomes a valid pixel. */
    val hardThreshold: Float = 0.5f,
    val connectivity: MaskConnectivity = MaskConnectivity.FOUR,
    val subjectMode: SubjectMode = SubjectMode.LARGEST,
    /** Radius of the opening (erode→dilate) applied before reduction; 0 disables it. */
    val openRadius: Int = 0,
    /** Radius of the closing (dilate→erode) applied before reduction; 0 disables it. */
    val closeRadius: Int = 0,
)
