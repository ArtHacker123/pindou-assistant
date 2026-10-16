package com.pindou.app.ml

import android.content.Context
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtException
import ai.onnxruntime.OrtSession
import com.pindou.core.imaging.FloatMask
import com.pindou.core.imaging.MaskHandle
import com.pindou.core.imaging.MaskOptions
import com.pindou.core.imaging.MaskPostProcess
import com.pindou.core.imaging.MaskSource
import com.pindou.core.imaging.Prompts
import com.pindou.core.imaging.SaliencyPreprocess
import com.pindou.core.model.PixelImage
import java.nio.FloatBuffer

/**
 * On-device [MaskSource] backed by the bundled `u2netp.onnx` saliency model and
 * ONNX Runtime. It is class-agnostic (no prompts today) and returns a
 * single-subject [FloatMask] at the working-image resolution.
 *
 * Input/output names are read off the graph, and the pure resize/normalize math
 * lives in [SaliencyPreprocess] so it can be unit tested on the JVM.
 */
class SaliencyMaskSource private constructor(
    private val env: OrtEnvironment,
    private val session: OrtSession,
    private val inputName: String,
    private val outputName: String,
) : MaskSource, AutoCloseable {

    /**
     * Milliseconds for the most recent [decode], split as
     * (JVM pre-process, native inference, JVM post-process). Read after a run to
     * see how much of the cutout cost is Kotlin math versus ONNX Runtime.
     */
    @Volatile
    var lastTimingMs: Triple<Long, Long, Long> = Triple(0, 0, 0)
        private set

    /** Mask post-process configuration applied by [decode]. */
    @Volatile
    var options: MaskOptions = MaskOptions()

    private class Handle(val image: PixelImage) : MaskHandle

    override fun encode(image: PixelImage): MaskHandle = Handle(image)

    override fun decode(handle: MaskHandle, prompts: Prompts): FloatMask {
        val image = (handle as? Handle)?.image ?: error("Unknown mask handle for SaliencyMaskSource")
        val side = SaliencyPreprocess.INPUT_SIZE
        val tPreStart = System.currentTimeMillis()
        val input = SaliencyPreprocess.toModelInput(image, side)
        val shape = longArrayOf(1, 3, side.toLong(), side.toLong())
        val saliency = FloatArray(side * side)
        OnnxTensor.createTensor(env, FloatBuffer.wrap(input), shape).use { tensor ->
            val tPreEnd = System.currentTimeMillis()
            session.run(mapOf(inputName to tensor)).use { result ->
                val output = result.get(outputName)
                    .orElseThrow { OrtException("Model produced no '$outputName' output") }
                @Suppress("UNCHECKED_CAST")
                val map = output.getValue() as Array<Array<Array<FloatArray>>>
                for (y in 0 until side) {
                    System.arraycopy(map[0][0][y], 0, saliency, y * side, side)
                }
                val tRunEnd = System.currentTimeMillis()
                lastTimingMs = Triple(tPreEnd - tPreStart, tRunEnd - tPreEnd, 0)
            }
        }
        val tPostStart = System.currentTimeMillis()
        val soft = SaliencyPreprocess.toWorkingMask(saliency, side, image.width, image.height)
        val out = MaskPostProcess.process(soft, options)
        lastTimingMs = Triple(lastTimingMs.first, lastTimingMs.second, System.currentTimeMillis() - tPostStart)
        return out
    }

    override fun close() {
        session.close()
    }

    companion object {
        const val MODEL_ASSET = "u2netp.onnx"

        fun create(context: Context): SaliencyMaskSource {
            val env = OrtEnvironment.getEnvironment()
            val model = context.assets.open(MODEL_ASSET).use { it.readBytes() }
            val session = OrtSession.SessionOptions().use { options -> env.createSession(model, options) }
            return SaliencyMaskSource(
                env = env,
                session = session,
                inputName = session.inputNames.first(),
                outputName = session.outputNames.first(),
            )
        }
    }
}
