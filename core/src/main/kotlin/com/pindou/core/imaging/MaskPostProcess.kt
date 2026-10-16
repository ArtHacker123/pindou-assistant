package com.pindou.core.imaging

/**
 * Post-processing for cutout masks: turns a soft saliency map into a
 * single-subject mask without any object detector, driven by [MaskOptions].
 */
object MaskPostProcess {

    /**
     * Runs the configured pipeline over [mask]: optional opening (remove
     * background speckle), optional closing (fill holes), then subject reduction.
     * With default [MaskOptions] the result equals [keepLargestComponent].
     */
    fun process(mask: FloatMask, options: MaskOptions): FloatMask {
        var m = mask
        if (options.openRadius > 0) m = MaskFilters.open(m, options.openRadius)
        if (options.closeRadius > 0) m = MaskFilters.close(m, options.closeRadius)
        return reduceSubject(m, options.foregroundThreshold, options.connectivity, options.subjectMode)
    }

    /**
     * Keeps only the largest 4-connected region of the mask at or above
     * [threshold] (ties go to the first region found scanning row-major) and
     * zeroes every other value, including values below the threshold.
     */
    fun keepLargestComponent(mask: FloatMask, threshold: Float = 0.5f): FloatMask =
        reduceSubject(mask, threshold, MaskConnectivity.FOUR, SubjectMode.LARGEST)

    /**
     * Groups the foreground (values at or above [threshold]) into regions under
     * [connectivity] and keeps the ones selected by [mode], zeroing the rest.
     */
    private fun reduceSubject(
        mask: FloatMask,
        threshold: Float,
        connectivity: MaskConnectivity,
        mode: SubjectMode,
    ): FloatMask {
        val w = mask.width
        val h = mask.height
        val n = mask.size
        val seen = BooleanArray(n)
        val stack = ArrayDeque<Int>()
        val components = ArrayList<IntArray>()
        for (start in 0 until n) {
            if (seen[start] || mask.values[start] < threshold) continue
            val members = ArrayList<Int>()
            seen[start] = true
            stack.addLast(start)
            while (stack.isNotEmpty()) {
                val cur = stack.removeLast()
                members.add(cur)
                val x = cur % w
                val y = cur / w
                if (x > 0) pushIf(mask, seen, stack, cur - 1, threshold)
                if (x < w - 1) pushIf(mask, seen, stack, cur + 1, threshold)
                if (y > 0) pushIf(mask, seen, stack, cur - w, threshold)
                if (y < h - 1) pushIf(mask, seen, stack, cur + w, threshold)
                if (connectivity == MaskConnectivity.EIGHT) {
                    if (x > 0 && y > 0) pushIf(mask, seen, stack, cur - w - 1, threshold)
                    if (x < w - 1 && y > 0) pushIf(mask, seen, stack, cur - w + 1, threshold)
                    if (x > 0 && y < h - 1) pushIf(mask, seen, stack, cur + w - 1, threshold)
                    if (x < w - 1 && y < h - 1) pushIf(mask, seen, stack, cur + w + 1, threshold)
                }
            }
            components.add(members.toIntArray())
        }
        val kept = when (mode) {
            SubjectMode.ALL -> components
            // maxByOrNull returns the first maximum, matching the previous
            // "strictly greater" tie-breaking.
            SubjectMode.LARGEST -> components.maxByOrNull { it.size }?.let { listOf(it) } ?: emptyList()
        }
        val keep = BooleanArray(n)
        for (component in kept) for (i in component) keep[i] = true
        val out = FloatArray(n)
        for (i in 0 until n) if (keep[i]) out[i] = mask.values[i]
        return FloatMask(w, h, out)
    }

    private fun pushIf(mask: FloatMask, seen: BooleanArray, stack: ArrayDeque<Int>, index: Int, threshold: Float) {
        if (!seen[index] && mask.values[index] >= threshold) {
            seen[index] = true
            stack.addLast(index)
        }
    }
}
