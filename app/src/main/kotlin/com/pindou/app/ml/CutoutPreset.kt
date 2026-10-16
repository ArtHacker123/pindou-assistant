package com.pindou.app.ml

import com.pindou.core.imaging.MaskConnectivity
import com.pindou.core.imaging.MaskOptions

/**
 * Named mask-tuning presets offered next to the cutout control. Each maps to a
 * [MaskOptions]. The preset is stored by [id], so an unknown or legacy value
 * falls back to [STANDARD] and new presets stay additive.
 */
enum class CutoutPreset(val id: String, val label: String, val options: MaskOptions) {
    /** Current behavior: a single largest subject, no morphology. */
    STANDARD("standard", "标准", MaskOptions()),

    /** Drops background residue: a stricter threshold plus an opening. */
    TIGHTEN_BACKGROUND(
        "tighten_background",
        "收紧背景",
        MaskOptions(foregroundThreshold = 0.6f, openRadius = 1),
    ),

    /** Keeps thin/eroded subject parts: a looser threshold, 8-connectivity, and a closing. */
    PRESERVE_SUBJECT(
        "preserve_subject",
        "保留主体",
        MaskOptions(
            foregroundThreshold = 0.4f,
            connectivity = MaskConnectivity.EIGHT,
            closeRadius = 1,
        ),
    ),
    ;

    companion object {
        fun fromId(id: String?): CutoutPreset = entries.firstOrNull { it.id == id } ?: STANDARD
    }
}
