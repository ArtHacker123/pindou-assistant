package com.pindou.app

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.pindou.app.data.ImageLoader
import com.pindou.app.render.Form1BitmapRenderer
import com.pindou.app.state.DocumentStore
import com.pindou.app.state.ExportService
import com.pindou.app.state.GenerationSession
import com.pindou.app.state.NavigationState
import com.pindou.app.state.ReferenceService
import com.pindou.core.imaging.ImageType
import com.pindou.core.model.Palette
import com.pindou.core.model.PindouProject
import com.pindou.core.model.PixelImage
import com.pindou.core.palette.PaletteMeta
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** The screens the app can show. */
sealed interface Screen {
    data object Home : Screen
    data object Crop : Screen
    data object Editor : Screen
    data object Export : Screen
}

/**
 * Thin facade over the app's state holders and services. Compose observes the
 * delegated `mutableStateOf` properties directly; the screen composables are
 * unchanged. Orchestration that spans holders (navigation, document lifecycle)
 * lives here.
 */
class PindouController(private val context: Context) {

    private val TAG = "PindouController"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val nav = NavigationState()
    private val documents = DocumentStore(context)
    private val references = ReferenceService()
    private val generation = GenerationSession(context, scope, documents, nav, references)
    private val export = ExportService(context, scope, nav)

    // ---- Observed state ---------------------------------------------------

    val paletteMetas: List<PaletteMeta> get() = generation.paletteMetas

    val screen: Screen get() = nav.screen
    val originalBitmap: Bitmap? get() = documents.originalBitmap
    val workingImage: PixelImage? get() = documents.workingImage
    val project: PindouProject? get() = documents.project
    val errorMessage: String? get() = nav.errorMessage
    val statusMessage: String? get() = nav.statusMessage
    val busy: Boolean get() = nav.busy

    val imageType: ImageType get() = generation.imageType
    val paletteName: String? get() = generation.paletteName
    val colorCountAuto: Boolean get() = generation.colorCountAuto
    val colorCountTarget: Int get() = generation.colorCountTarget
    val gridWidth: Int get() = generation.gridWidth
    val gridHeight: Int get() = generation.gridHeight

    var exportSize: ExportSize
        get() = export.exportSize
        set(value) { export.exportSize = value }

    /** Whether saved/shared `.pindou` files embed the grid reference image (on by default). */
    var includeReference: Boolean
        get() = export.includeReference
        set(value) { export.includeReference = value }

    fun palette(): Palette? = generation.palette()

    fun showError(message: String) = nav.showError(message)

    // ---- Navigation -------------------------------------------------------

    fun pickImage(uri: Uri) {
        nav.clearError()
        try {
            documents.setOriginal(ImageLoader.loadBitmap(context, uri))
            nav.go(Screen.Crop)
        } catch (e: Exception) {
            Log.w(TAG, "pickImage failed for $uri", e)
            nav.showError("${e.message ?: "图片导入失败"}")
        }
    }

    fun backFromCrop() {
        documents.setOriginal(null)
        nav.go(Screen.Home)
    }

    fun onCropConfirmed(cropped: PixelImage) {
        documents.setWorking(cropped.paddedToSquare())
        nav.go(Screen.Editor)
        generation.runGenerate()
    }

    fun openProject(parsed: PindouProject) {
        documents.updateProject(parsed)
        generation.applyFromProject(parsed)
        nav.go(Screen.Editor)
    }

    fun openPindou(uri: Uri) {
        try {
            openProject(documents.open(uri))
        } catch (e: Exception) {
            Log.w(TAG, "openPindou failed for $uri", e)
            nav.showError(e.message ?: "无法打开 .pindou 文件")
        }
    }

    fun goEditor() { if (documents.project != null) nav.go(Screen.Editor) }
    fun goExport() { if (documents.project != null) nav.go(Screen.Export) }
    fun goHome() { nav.go(Screen.Home) }
    fun clearError() = nav.clearError()
    fun clearStatus() = nav.clearStatus()

    // ---- Generation -------------------------------------------------------

    fun updateImageType(type: ImageType) = generation.updateImageType(type)
    fun updatePalette(name: String) = generation.updatePalette(name)
    fun updateGridSize(size: Int) = generation.updateGridSize(size)
    fun updateColorCountSelection(isAuto: Boolean, count: Int) =
        generation.updateColorCountSelection(isAuto, count)

    fun generate() = generation.generate()

    // ---- Export & share ---------------------------------------------------

    fun renderForm1(cellSize: Int? = null): Bitmap? = export.renderForm1(documents.project, cellSize)

    /** Resolves the chosen export size to px/cell and the resulting PNG dimensions. */
    fun resolvedExportPlan(p: PindouProject): Form1BitmapRenderer.ExportPlan =
        export.resolvedExportPlan(p)

    fun resolvedExportCellSize(p: PindouProject): Int = export.resolvedExportCellSize(p)

    fun exportPng(share: Boolean) = export.exportPng(documents.project, share)

    fun exportPindou(share: Boolean) = export.exportPindou(documents.project, share)
}

/** Export size presets: A4 single-page auto-fit, a target output width, or the max the device can hold. */
enum class ExportSize(val label: String) {
    A4("A4 整页(自适应)"),
    P1080("1080P"),
    UHD4K("4K"),
    MAX("最大尺寸"),
}
