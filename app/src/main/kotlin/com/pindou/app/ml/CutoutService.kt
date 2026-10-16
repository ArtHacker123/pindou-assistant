package com.pindou.app.ml

import android.content.Context
import com.pindou.core.imaging.MaskOptions
import com.pindou.core.imaging.SaliencyPreprocess
import com.pindou.core.model.PixelImage
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Owns the saliency model's lifecycle and bounds every inference with a timeout.
 *
 * The model is created lazily on the first use so app startup is unaffected, and
 * all work runs on a single dedicated thread so a stuck inference can be timed
 * out (and the coroutine that requested it freed) without blocking generation.
 */
class CutoutService(private val context: Context) : AutoCloseable {

    private val executor = Executors.newSingleThreadExecutor { runnable -> Thread(runnable, "pindou-saliency") }

    @Volatile
    private var source: SaliencyMaskSource? = null

    /** Mask post-process configuration applied to the next [cutout]. */
    @Volatile
    var options: MaskOptions = MaskOptions()

    private fun source(): SaliencyMaskSource =
        source ?: SaliencyMaskSource.create(context).also { source = it }

    /**
     * Times one representative inference for the per-model capability gate. A run
     * that does not finish within [GATE_MEASURE_MAX_MS] is reported as [Long.MAX_VALUE]
     * (unambiguously over budget) rather than blocking the caller.
     */
    fun measureInferenceMs(): Long {
        val future = executor.submit<Long> {
            val saliency = source()
            val side = SaliencyPreprocess.INPUT_SIZE
            val dummy = PixelImage.filled(side, side, 0xFF808080.toInt())
            val start = System.currentTimeMillis()
            saliency.decode(saliency.encode(dummy))
            System.currentTimeMillis() - start
        }
        return try {
            future.get(GATE_MEASURE_MAX_MS, TimeUnit.MILLISECONDS)
        } catch (e: TimeoutException) {
            future.cancel(true)
            Long.MAX_VALUE
        } catch (e: Exception) {
            Long.MAX_VALUE
        }
    }

    /**
     * Returns [image] with the cutout mask attached, or `null` when inference
     * fails or exceeds [timeoutMs] — the caller then generates without a cutout.
     */
    fun cutout(image: PixelImage, timeoutMs: Long = RUNTIME_TIMEOUT_MS): PixelImage? {
        val opts = options
        val future = executor.submit<PixelImage?> {
            val saliency = source()
            saliency.options = opts
            image.withMask(saliency.decode(saliency.encode(image)).toBooleanMask(opts.hardThreshold))
        }
        return try {
            future.get(timeoutMs, TimeUnit.MILLISECONDS)
        } catch (e: TimeoutException) {
            future.cancel(true)
            null
        } catch (e: Exception) {
            null
        }
    }

    override fun close() {
        executor.shutdownNow()
        source?.close()
        source = null
    }

    /**
     * Milliseconds of the most recent successful [cutout], split as
     * (JVM pre-process, native inference, JVM post-process), so callers can see
     * whether Kotlin math or ONNX Runtime dominates. Zero until the model has run.
     */
    fun lastTimingMs(): Triple<Long, Long, Long> = source?.lastTimingMs ?: Triple(0, 0, 0)

    companion object {
        const val BUDGET_MS = 3000L
        const val RUNTIME_TIMEOUT_MS = 3000L
        private const val GATE_MEASURE_MAX_MS = 8000L
    }
}
