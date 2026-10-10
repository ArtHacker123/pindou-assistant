package com.pindou.app

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pindou.app.data.AssetPaletteProvider
import com.pindou.app.data.ExportManager
import com.pindou.app.data.ImageLoader
import com.pindou.app.data.SourceHasher
import com.pindou.app.render.Form1BitmapRenderer
import com.pindou.core.generate.GenerateConfig
import com.pindou.core.generate.PatternGenerator
import com.pindou.core.model.Palette
import com.pindou.core.model.PindouProject
import com.pindou.core.model.PixelImage
import com.pindou.core.palette.PaletteMeta
import com.pindou.core.palette.PaletteRegistry
import com.pindou.core.processor.ImageType
import com.pindou.core.serialize.ProjectFormatException
import com.pindou.core.serialize.ProjectSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** The screens the app can show. */
sealed interface Screen {
    data object Home : Screen
    data object Crop : Screen
    data object Editor : Screen
    data object Export : Screen
}

/**
 * Holds all mutable app state and orchestrates loading, generation, export and
 * sharing. Compose observes its `mutableStateOf` properties directly.
 */
class PindouController(private val context: Context) {

    private val TAG = "PindouController"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val palettes = PaletteRegistry(AssetPaletteProvider(context))
    private val generator = PatternGenerator()
    private val exportManager = ExportManager(context)

    val paletteMetas: List<PaletteMeta> = palettes.available()

    var screen by mutableStateOf<Screen>(Screen.Home)
        private set
    var originalBitmap by mutableStateOf<Bitmap?>(null)
        private set
    var workingImage by mutableStateOf<PixelImage?>(null)
        private set
    var project by mutableStateOf<PindouProject?>(null)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var statusMessage by mutableStateOf<String?>(null)
        private set
    var busy by mutableStateOf(false)
        private set

    // Generation parameters (two-way bound by the merged parameter/pattern screen).
    var imageType by mutableStateOf(ImageType.CARTOON)
    var paletteName by mutableStateOf(paletteMetas.firstOrNull()?.name)
    var colorCountAuto by mutableStateOf(true)
    var colorCountTarget by mutableStateOf(16)
    var gridWidth by mutableStateOf(52)
    var gridHeight by mutableStateOf(52)
    var exportCellSize by mutableStateOf(64)

    private var regenJob: Job? = null
    private var genSeq = 0

    fun palette(): Palette? = paletteName?.let { palettes.load(it) }

    // ---- Navigation -------------------------------------------------------

    fun pickImage(uri: Uri) {
        errorMessage = null
        try {
            originalBitmap = ImageLoader.loadBitmap(context, uri)
            screen = Screen.Crop
        } catch (e: Exception) {
            Log.w(TAG, "pickImage failed for $uri", e)
            errorMessage = "${e.message ?: "图片导入失败"}"
        }
    }

    fun backFromCrop() {
        originalBitmap = null
        screen = Screen.Home
    }

    fun onCropConfirmed(cropped: PixelImage) {
        workingImage = cropped.paddedToSquare()
        screen = Screen.Editor
        runGenerate()
    }

    fun openProject(parsed: PindouProject) {
        project = parsed
        paletteName = parsed.meta.paletteName
        imageType = ImageType.fromId(parsed.meta.imageType)
        colorCountAuto = false
        colorCountTarget = parsed.meta.colorCountTarget
        gridWidth = parsed.meta.gridWidth
        gridHeight = parsed.meta.gridHeight
        screen = Screen.Editor
    }

    fun openPindou(uri: Uri) {
        try {
            val text = context.contentResolver.openInputStream(uri)
                ?.bufferedReader()?.use { it.readText() }
                ?: throw ProjectFormatException("无法读取文件")
            openProject(ProjectSerializer.decode(text))
        } catch (e: Exception) {
            Log.w(TAG, "openPindou failed for $uri", e)
            errorMessage = e.message ?: "无法打开 .pindou 文件"
        }
    }

    fun goEditor() { if (project != null) screen = Screen.Editor }
    fun goExport() { if (project != null) screen = Screen.Export }
    fun goHome() { screen = Screen.Home }
    fun clearError() { errorMessage = null }
    fun clearStatus() { statusMessage = null }

    // ---- Generation -------------------------------------------------------

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
        if (workingImage == null) return
        regenJob?.cancel()
        regenJob = scope.launch {
            delay(250)
            runGenerate()
        }
    }

    fun generate() {
        if (screen != Screen.Editor) screen = Screen.Editor
        runGenerate()
    }

    private fun runGenerate() {
        val image = workingImage ?: return
        val palette = palette() ?: return
        busy = true
        errorMessage = null
        val seq = ++genSeq
        scope.launch {
            try {
                val result = withContext(Dispatchers.Default) {
                    val hash = SourceHasher.hash(image)
                    generator.generate(
                        image,
                        GenerateConfig(
                            gridWidth = gridWidth,
                            gridHeight = gridHeight,
                            palette = palette,
                            colorCountTarget = colorCountTarget,
                            imageType = imageType,
                            seed = hash.hashCode().toLong(),
                            createdAt = System.currentTimeMillis(),
                            sourceHash = hash,
                        ),
                    )
                }
                if (seq == genSeq) {
                    project = result
                    screen = Screen.Editor
                }
            } catch (e: Exception) {
                Log.w(TAG, "generate failed", e)
                if (seq == genSeq) errorMessage = e.message ?: "生成失败"
            } finally {
                if (seq == genSeq) busy = false
            }
        }
    }

    // ---- Export & share ---------------------------------------------------

    fun renderForm1(cellSize: Int = exportCellSize): Bitmap? {
        val p = project ?: return null
        return Form1BitmapRenderer.render(p, cellSize)
    }

    fun exportPng(share: Boolean) {
        val p = project ?: return
        busy = true
        errorMessage = null
        scope.launch {
            try {
                val file = withContext(Dispatchers.Default) {
                    val bitmap = Form1BitmapRenderer.render(p, exportCellSize)
                    exportManager.savePng(bitmap, baseName(p))
                }
                finishExport(file, ExportManager.MIME_PNG, share, "分享拼豆图纸")
            } catch (e: Exception) {
                Log.w(TAG, "exportPng failed", e)
                errorMessage = e.message ?: "导出失败"
            } finally {
                busy = false
            }
        }
    }

    fun exportPindou(share: Boolean) {
        val p = project ?: return
        busy = true
        errorMessage = null
        scope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    exportManager.saveProject(p, baseName(p))
                }
                finishExport(file, ExportManager.MIME_PINDOU, share, "分享交互图纸")
            } catch (e: Exception) {
                Log.w(TAG, "exportPindou failed", e)
                errorMessage = e.message ?: "导出失败"
            } finally {
                busy = false
            }
        }
    }

    private fun finishExport(file: File, mime: String, share: Boolean, title: String) {
        if (share) {
            exportManager.share(file, mime, title)
        } else {
            statusMessage = "已保存到 ${file.absolutePath}"
        }
    }

    private fun baseName(p: PindouProject): String {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "pindou_${p.grid.width}x${p.grid.height}_$stamp"
    }
}