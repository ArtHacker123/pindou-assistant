package com.pindou.core.imaging

import com.pindou.core.model.PixelImage
import com.pindou.core.model.Rgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * End-to-end check of the cutout seam without any ML: a fake [MaskSource] keeps
 * the left half of the image, and the existing sampler/reference honor it. This
 * pins the "mask -> coverage -> reference" path before a real model is wired in.
 */
class SubjectCutoutPipelineTest {

    /** Marks the left half of the encoded image as foreground. */
    private class LeftHalfMaskSource : MaskSource {
        private var image: PixelImage? = null

        override fun encode(image: PixelImage): MaskHandle {
            this.image = image
            return object : MaskHandle {}
        }

        override fun decode(handle: MaskHandle, prompts: Prompts): FloatMask {
            val img = requireNotNull(image)
            val half = img.width / 2
            return FloatMask(img.width, img.height, FloatArray(img.size) { i ->
                if (i % img.width < half) 1f else 0f
            })
        }
    }

    private fun source(): PixelImage = PixelImage(
        width = 4,
        height = 1,
        pixels = intArrayOf(
            0xFFFF0000.toInt(), 0xFFFF0000.toInt(), // left: red (subject)
            0xFF0000FF.toInt(), 0xFF0000FF.toInt(), // right: blue (background)
        ),
    )

    private fun maskedSource(): PixelImage {
        val img = source()
        val source = LeftHalfMaskSource()
        val mask = source.decode(source.encode(img))
        return img.withMask(mask.toBooleanMask())
    }

    @Test
    fun `background pixels are excluded from coverage and the average`() {
        val image = maskedSource()
        val sample = AreaColor.cell(image, 1, 1, 0, 0)
        assertEquals(0.5f, sample.coverage, 1e-6f)
        assertEquals(Rgb(255, 0, 0), sample.rgb) // blue background does not contribute
    }

    @Test
    fun `cells fully inside, outside, and partially overlapping report the right coverage`() {
        val image = maskedSource()
        assertEquals(1f, AreaColor.cell(image, 4, 1, 0, 0).coverage, 1e-6f)
        assertEquals(1f, AreaColor.cell(image, 4, 1, 0, 1).coverage, 1e-6f)
        assertEquals(0f, AreaColor.cell(image, 4, 1, 0, 2).coverage, 1e-6f)
        assertEquals(0f, AreaColor.cell(image, 4, 1, 0, 3).coverage, 1e-6f)
    }

    @Test
    fun `the reference image reflects the mask as holes`() {
        val samples = GridSampler.sample(maskedSource(), 4, 1)
        val reference = ReferenceBuilder.build(samples)
        assertTrue("subject cell should be opaque", reference.alpha(0, 0) >= 8)
        assertTrue("subject cell should be opaque", reference.alpha(1, 0) >= 8)
        assertEquals(0, reference.alpha(2, 0)) // background cell -> hole
        assertEquals(0, reference.alpha(3, 0))
    }

    @Test
    fun `the mask survives a preset and still governs validity`() {
        val processed = ImageProcessorRegistry.default().get(ImageType.CARTOON.id).process(maskedSource())
        val sample = AreaColor.cell(processed, 1, 1, 0, 0)
        assertEquals(0.5f, sample.coverage, 1e-6f)
    }
}
