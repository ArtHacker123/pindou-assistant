package com.pindou.app.state

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pindou.app.Screen
import com.pindou.app.data.AssetPaletteProvider
import com.pindou.app.data.SourceHasher
import com.pindou.core.generate.GenerateConfig
import com.pindou.core.generate.PatternGenerator
import com.pindou.core.imaging.ImageType
import com.pindou.core.model.Palette
import com.pindou.core.model.PindouProject
import com.pindou.core.model.PixelImage
import com.pindou.core.palette.PaletteMeta
import com.pindou.core.palette.PaletteRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Holds the generation parameters, the debounced regeneration loop, and the
 * cached grid-resolution reference. Writes its result into the [DocumentStore]
 * and navigates through [NavigationState].
 *
 * The reference image depends only on the working image, image-type and grid
 * size, so it is cached under exactly those inputs and reused across changes
 * that only affect the color count / palette.
 */
class GenerationSession(
    context: Context,
    private val scope: CoroutineScope,
    private val documents: DocumentStore,
    private val nav: NavigationState,
    private val references: ReferenceService,
    private val generator: PatternGenerator = PatternGenerator(),
) {

    private val TAG = "GenerationSession"
    private val palettes = PaletteRegistry(AssetPaletteProvider(context))

    val paletteMetas: List<PaletteMeta> = palettes.available()

    // Generation parameters (two-way bound by the merged parameter/pattern screen).
    var imageType by mutableStateOf(ImageType.CARTOON)
    var paletteName by mutableStateOf(paletteMetas.firstOrNull()?.name)
    var colorCountAuto by mutableStateOf(true)
    var colorCountTarget by mutableStateOf(16)
    var gridWidth by mutableStateOf(52)
    var gridHeight by mutableStateOf(52)

    private var regenJob: Job? = null
    private var genSeq = 0

    private data class ReferenceKey(
        val image: PixelImage,
        val imageType: String,
        val gridWidth: Int,
        val gridHeight: Int,
    )
    private var referenceKey: ReferenceKey? = null
    private var cachedReference: PixelImage? = null

    fun palette(): Palette? = paletteName?.let { palettes.load(it) }

    /** Applies the parameters embedded in an opened project without regenerating. */
    fun applyFromProject(project: PindouProject) {
        paletteName = project.meta.paletteName
        imageType = ImageType.fromId(project.meta.imageType)
        colorCountAuto = false
        colorCountTarget = project.meta.colorCountTarget
        gridWidth = project.meta.gridWidth
        gridHeight = project.meta.gridHeight
    }

    fun updateImageType(type: ImageType) {
        if (imageType != type) {
            imageType = type
            scheduleRegen()
        }
    }

    fun updatePalette(name: String) {
        if (paletteName != name) {
            paletteName = name
            scheduleRegen()
        }
    }

    fun updateGridSize(size: Int) {
        if (gridWidth != size || gridHeight != size) {
            gridWidth = size
            gridHeight = size
            scheduleRegen()
        }
    }

    fun updateColorCountSelection(isAuto: Boolean, count: Int) {
        val changed = colorCountAuto != isAuto || colorCountTarget != count
        colorCountAuto = isAuto
        colorCountTarget = count
        if (changed) scheduleRegen()
    }

    /** Debounced regeneration: coalesces rapid parameter changes. */
    private fun scheduleRegen() {
        if (documents.workingImage == null) return
        regenJob?.cancel()
        regenJob = scope.launch {
            delay(250)
            runGenerate()
        }
    }

    fun generate() {
        if (nav.screen != Screen.Editor) nav.go(Screen.Editor)
        runGenerate()
    }

    fun runGenerate() {
        val image = documents.workingImage ?: return
        val palette = palette() ?: return
        nav.markBusy(true)
        nav.clearError()
        val seq = ++genSeq
        val type = imageType
        val gw = gridWidth
        val gh = gridHeight
        val colorCount = colorCountTarget
        scope.launch {
            try {
                val (key, reference, result) = withContext(Dispatchers.Default) {
                    val newKey = ReferenceKey(image, type.id, gw, gh)
                    val reuse = if (newKey == referenceKey) cachedReference else null
                    val processed = references.preprocess(image, type)
                    val reference = reuse ?: references.reference(processed, gw, gh)
                    val hash = SourceHasher.hash(image)
                    val generated = generator.generate(
                        processed,
                        GenerateConfig(
                            gridWidth = gw,
                            gridHeight = gh,
                            palette = palette,
                            colorCountTarget = colorCount,
                            imageType = type,
                            seed = hash.hashCode().toLong(),
                            createdAt = System.currentTimeMillis(),
                            sourceHash = hash,
                        ),
                        reference = reference,
                    )
                    Triple(newKey, reference, generated)
                }
                if (seq == genSeq) {
                    referenceKey = key
                    cachedReference = reference
                    documents.updateProject(result)
                    nav.go(Screen.Editor)
                }
            } catch (e: Exception) {
                Log.w(TAG, "generate failed", e)
                if (seq == genSeq) nav.showError(e.message ?: "生成失败")
            } finally {
                if (seq == genSeq) nav.markBusy(false)
            }
        }
    }
}
