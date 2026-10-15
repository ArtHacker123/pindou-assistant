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
import com.pindou.core.match.ColorStrategy
import com.pindou.core.match.ColorStrategyRegistry
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

/** Color count used when the target is "automatic" or otherwise unset. */
private const val AUTO_COLOR_COUNT = 16

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
    private val onRegenerated: () -> Unit = {},
) {

    private val TAG = "GenerationSession"
    private val palettes = PaletteRegistry(AssetPaletteProvider(context))
    private val strategies = ColorStrategyRegistry.default()

    val paletteMetas: List<PaletteMeta> = palettes.available()

    /** Color-selection strategies offered to the user. */
    val colorStrategies: List<ColorStrategy> = strategies.all()

    // Generation parameters (two-way bound by the merged parameter/pattern screen).
    var imageType by mutableStateOf(ImageType.CARTOON)
    var paletteName by mutableStateOf(paletteMetas.firstOrNull()?.name)
    var colorStrategy by mutableStateOf(ColorStrategyRegistry.DEFAULT_ID)
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

    // Preprocessing depends only on the working image and image type, so its
    // result is reused across every regeneration that keeps the same source and
    // preset (palette / color-count / strategy changes included). The image
    // object is stable between imports, so identity comparison is the key.
    private var preprocessImage: PixelImage? = null
    private var preprocessType: String? = null
    private var cachedProcessed: PixelImage? = null

    // The source hash only depends on the working image; it is expensive enough
    // (a full-image SHA-256) to reuse instead of recomputing on every change.
    private var hashImage: PixelImage? = null
    private var cachedHash: String? = null

    fun palette(): Palette? = paletteName?.let { palettes.load(it) }

    /** Loads a palette by name (used by free creation and palette remaps). */
    fun paletteFor(name: String): Palette? = runCatching { palettes.load(name) }.getOrNull()

    /** Syncs the palette parameter without triggering regeneration (used after a remap). */
    fun applyPaletteName(name: String) {
        paletteName = name
    }

    /** Syncs the grid-size parameter without triggering regeneration (used after a resize). */
    fun applyGridSize(size: Int) {
        gridWidth = size
        gridHeight = size
    }

    /** Switches the session to a blank free-creation document's parameters. */
    fun applyFree(paletteName: String, size: Int) {
        this.paletteName = paletteName
        imageType = ImageType.CARTOON
        colorStrategy = ColorStrategyRegistry.DEFAULT_ID
        colorCountAuto = true
        colorCountTarget = AUTO_COLOR_COUNT
        gridWidth = size
        gridHeight = size
    }

    /**
     * Applies the parameters embedded in an opened project without regenerating.
     *
     * Blank free-creation and template projects store `colorCountTarget = 0` (they skip
     * quantization). Such a value must not leak into the shared session, or a later gallery/camera
     * generation would inherit it and produce a single-color pattern; fall back to automatic.
     */
    fun applyFromProject(project: PindouProject) {
        paletteName = project.meta.paletteName
        imageType = ImageType.fromId(project.meta.imageType)
        colorStrategy = project.meta.colorStrategy
        val target = project.meta.colorCountTarget
        colorCountAuto = target <= 0
        colorCountTarget = if (target > 0) target else AUTO_COLOR_COUNT
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

    fun updateColorStrategy(id: String) {
        if (colorStrategy != id) {
            colorStrategy = id
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

    /** Applies the image-type preset, reusing the result while source and type are unchanged. */
    private fun preprocessFor(image: PixelImage, type: ImageType): PixelImage {
        if (preprocessImage === image && preprocessType == type.id) {
            cachedProcessed?.let { return it }
        }
        val processed = references.preprocess(image, type)
        preprocessImage = image
        preprocessType = type.id
        cachedProcessed = processed
        return processed
    }

    /** Fingerprints the working image, reusing the result while the source is unchanged. */
    private fun hashFor(image: PixelImage): String {
        if (hashImage === image) {
            cachedHash?.let { return it }
        }
        val value = SourceHasher.hash(image)
        hashImage = image
        cachedHash = value
        return value
    }

    fun runGenerate() {
        val image = documents.workingImage ?: return
        val palette = palette() ?: return
        nav.markBusy(true)
        nav.clearError()
        val seq = ++genSeq
        val type = imageType
        val strategy = colorStrategy
        val auto = colorCountAuto
        val gw = gridWidth
        val gh = gridHeight
        val colorCount = colorCountTarget.coerceAtLeast(1)
        scope.launch {
            try {
                val (key, reference, result) = withContext(Dispatchers.Default) {
                    val t0 = System.currentTimeMillis()
                    val newKey = ReferenceKey(image, type.id, gw, gh)
                    val reuse = if (newKey == referenceKey) cachedReference else null
                    val t1 = System.currentTimeMillis()
                    val processed = preprocessFor(image, type)
                    val t2 = System.currentTimeMillis()
                    // Cartoons reduce each cell to its palette mode, which cannot be
                    // recovered from an averaged reference, so no reference reuse.
                    val reference = if (type == ImageType.CARTOON) {
                        null
                    } else {
                        reuse ?: references.reference(processed, gw, gh)
                    }
                    val t3 = System.currentTimeMillis()
                    val hash = hashFor(image)
                    val t4 = System.currentTimeMillis()
                    val generated = generator.generate(
                        processed,
                        GenerateConfig(
                            gridWidth = gw,
                            gridHeight = gh,
                            palette = palette,
                            colorCountTarget = colorCount,
                            colorStrategy = strategy,
                            colorCountAuto = auto,
                            imageType = type,
                            seed = hash.hashCode().toLong(),
                            createdAt = System.currentTimeMillis(),
                            sourceHash = hash,
                        ),
                        reference = reference,
                    )
                    val t5 = System.currentTimeMillis()
                    Log.i(
                        TAG,
                        "gen seq=$seq img=${image.width}x${image.height} grid=${gw}x${gh} " +
                            "type=${type.id} strat=$strategy reuse=${reuse != null} auto=$auto count=$colorCount " +
                            "| prepend=${t1 - t0} preprocess=${t2 - t1} reference=${t3 - t2} " +
                            "hash=${t4 - t3} generate=${t5 - t4} total=${t5 - t0}ms",
                    )
                    Triple(newKey, reference, generated)
                }
                if (seq == genSeq) {
                    referenceKey = key
                    cachedReference = reference
                    documents.updateProject(result)
                    documents.markCommitted()
                    onRegenerated()
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
