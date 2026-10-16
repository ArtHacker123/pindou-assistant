package com.pindou.core.imaging

import com.pindou.core.model.PixelImage

/** A positive or negative click used to prompt a prompted segmentation source. */
data class PromptPoint(val x: Int, val y: Int, val positive: Boolean = true)

/** A region-of-interest box used to prompt a prompted segmentation source. */
data class PromptBox(val left: Int, val top: Int, val right: Int, val bottom: Int)

/**
 * Prompts for a [MaskSource]. Empty for automatic (class-agnostic saliency)
 * segmentation; points and boxes are the extension point for future instance
 * segmentation.
 */
class Prompts(
    val points: List<PromptPoint> = emptyList(),
    val boxes: List<PromptBox> = emptyList(),
) {
    val isEmpty: Boolean get() = points.isEmpty() && boxes.isEmpty()

    companion object {
        val NONE = Prompts()
    }
}

/** Per-image state produced by [MaskSource.encode]. */
interface MaskHandle

/**
 * Produces the foreground [FloatMask] for an image.
 *
 * The encode/decode split mirrors prompted segmentation: [encode] is the
 * expensive per-image step and [decode] is the cheap per-prompt step, so a
 * future instance source can encode once and decode per tap. Automatic
 * saliency uses [Prompts.NONE].
 */
interface MaskSource {
    fun encode(image: PixelImage): MaskHandle
    fun decode(handle: MaskHandle, prompts: Prompts = Prompts.NONE): FloatMask
}
