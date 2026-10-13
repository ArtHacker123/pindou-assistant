package com.pindou.app

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.pindou.app.data.ImageLoader
import com.pindou.app.data.PatternEntry
import com.pindou.app.data.PindouTemplate
import com.pindou.app.render.Form1BitmapRenderer
import com.pindou.app.state.DocumentStore
import com.pindou.app.state.CropPurpose
import com.pindou.app.state.EditTool
import com.pindou.app.state.EditingSession
import com.pindou.app.state.ExportService
import com.pindou.app.state.GenerationSession
import com.pindou.app.state.HistorySession
import com.pindou.app.state.NavigationState
import com.pindou.app.state.ReferenceService
import com.pindou.core.imaging.ImageType
import com.pindou.core.match.PaletteMapper
import com.pindou.core.model.Grid
import com.pindou.core.model.Palette
import com.pindou.core.model.PindouProject
import com.pindou.core.model.PixelImage
import com.pindou.core.model.ProjectMeta
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
    data object History : Screen
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
    private val generation = GenerationSession(
        context, scope, documents, nav, references,
        onRegenerated = { editing.clearHistory() },
    )
    private val editing = EditingSession(documents, references)
    private val export = ExportService(context, scope, nav)
    private val history = HistorySession(context, scope)

    // ---- Observed state ---------------------------------------------------

    val paletteMetas: List<PaletteMeta> get() = generation.paletteMetas

    val screen: Screen get() = nav.screen
    val originalBitmap: Bitmap? get() = documents.originalBitmap
    val workingImage: PixelImage? get() = documents.workingImage
    val project: PindouProject? get() = documents.project
    val errorMessage: String? get() = nav.errorMessage
    val statusMessage: String? get() = nav.statusMessage
    val busy: Boolean get() = nav.busy

    /** Saved patterns for the home "recent" row and the history screen, newest first. */
    val historyItems: List<PatternEntry> get() = history.items
    val historyLoading: Boolean get() = history.loading

    val imageType: ImageType get() = generation.imageType
    val paletteName: String? get() = generation.paletteName
    val colorCountAuto: Boolean get() = generation.colorCountAuto
    val colorCountTarget: Int get() = generation.colorCountTarget
    val gridWidth: Int get() = generation.gridWidth
    val gridHeight: Int get() = generation.gridHeight

    val editMode: Boolean get() = editing.editMode
    val editTool: EditTool get() = editing.tool
    val brushCode: String? get() = editing.brushCode

    /** The cell under the finger in edit mode (cursor highlight), or null. */
    val editCursor: Pair<Int, Int>? get() = editing.cursor

    /** Whether the grid carries manual edits since the last generation / open. */
    val edited: Boolean get() = documents.edited

    /** True when the project has a source image it can be regenerated from. */
    val hasSource: Boolean get() = documents.workingImage != null

    /** True while the current project is a free creation (started from 自由创作). */
    val isFreeCreation: Boolean get() = documents.isFreeCreation

    /** Whether the current project has unsaved changes since the last `.pindou` save. */
    val needsSave: Boolean get() = documents.needsSave

    /** Whether the current project was opened from an external file (not yet in our Documents). */
    val imported: Boolean get() = documents.imported

    /** True when the grid has no bead at all (nothing to save or export). */
    val projectIsEmpty: Boolean
        get() = documents.project?.grid?.cells?.all { it == null } ?: true

    /** Whether the grid reference image is shown over the pattern. */
    val showReference: Boolean get() = nav.showReference

    fun setShowReference(value: Boolean) = nav.updateShowReference(value)

    var exportSize: ExportSize
        get() = export.exportSize
        set(value) { export.exportSize = value }

    fun palette(): Palette? = generation.palette()

    fun showError(message: String) = nav.showError(message)

    // ---- Navigation -------------------------------------------------------

    fun pickImage(uri: Uri) {
        nav.clearError()
        try {
            documents.setOriginal(ImageLoader.loadBitmap(context, uri))
            nav.beginCrop(CropPurpose.SOURCE)
            nav.go(Screen.Crop)
        } catch (e: Exception) {
            Log.w(TAG, "pickImage failed for $uri", e)
            nav.showError("${e.message ?: "图片导入失败"}")
        }
    }

    /** Picks an image to use as a reference; it goes through the shared crop/scale screen. */
    fun pickReference(uri: Uri) {
        nav.clearError()
        try {
            documents.setOriginal(ImageLoader.loadBitmap(context, uri))
            nav.beginCrop(CropPurpose.REFERENCE)
            nav.go(Screen.Crop)
        } catch (e: Exception) {
            Log.w(TAG, "pickReference failed for $uri", e)
            nav.showError("${e.message ?: "参考图导入失败"}")
        }
    }

    fun backFromCrop() {
        val purpose = nav.cropPurpose
        documents.setOriginal(null)
        nav.beginCrop(CropPurpose.SOURCE)
        nav.go(if (purpose == CropPurpose.REFERENCE) Screen.Editor else Screen.Home)
    }

    fun onCropConfirmed(cropped: PixelImage) {
        val purpose = nav.cropPurpose
        documents.setOriginal(null)
        nav.beginCrop(CropPurpose.SOURCE)
        when (purpose) {
            CropPurpose.SOURCE -> {
                documents.setWorking(cropped.paddedToSquare())
                documents.markGenerated()
                editing.exit()
                nav.updateShowReference(false)
                nav.go(Screen.Editor)
                generation.runGenerate()
            }
            CropPurpose.REFERENCE -> {
                editing.loadReference(cropped.paddedToSquare(), generation.imageType)
                nav.updateShowReference(true)
                nav.go(Screen.Editor)
            }
        }
    }

    fun openProject(parsed: PindouProject) {
        documents.updateProject(parsed)
        documents.markSaved()
        documents.markGenerated()
        editing.clearHistory()
        editing.exit()
        generation.applyFromProject(parsed)
        nav.updateShowReference(false)
        nav.go(Screen.Editor)
    }

    fun openPindou(uri: Uri, external: Boolean = false) {
        val name = documents.displayName(uri)
        try {
            openProject(documents.open(uri))
            documents.markImported(external)
        } catch (e: Exception) {
            Log.w(TAG, "openPindou failed for $uri", e)
            nav.showError(
                if (name != null && !name.endsWith(".pindou", ignoreCase = true)) {
                    "这不是拼豆图纸文件（.pindou）"
                } else {
                    "无法打开：文件可能已损坏或版本不受支持"
                },
            )
        }
    }

    fun goEditor() { if (documents.project != null) nav.go(Screen.Editor) }
    fun goExport() { if (documents.project != null) nav.go(Screen.Export) }
    fun goHome() { nav.go(Screen.Home) }
    fun goHistory() { nav.go(Screen.History) }
    fun refreshHistory() = history.refresh()
    fun deleteHistory(entry: PatternEntry) = history.delete(entry)
    fun clearError() = nav.clearError()
    fun clearStatus() = nav.clearStatus()

    // ---- Generation -------------------------------------------------------

    fun updateImageType(type: ImageType) = generation.updateImageType(type)
    fun updatePalette(name: String) = generation.updatePalette(name)
    fun updateGridSize(size: Int) = generation.updateGridSize(size)
    fun updateColorCountSelection(isAuto: Boolean, count: Int) =
        generation.updateColorCountSelection(isAuto, count)

    fun generate() = generation.generate()

    // ---- Editing ----------------------------------------------------------

    /** Enters edit mode; the project is no longer the pristine imported file, so drop the import prompt. */
    fun enterEdit() {
        documents.markImported(false)
        editing.enter()
    }

    fun exitEdit() = editing.exit()
    fun selectEditTool(tool: EditTool) = editing.selectTool(tool)
    fun selectBrush(code: String?) = editing.selectBrush(code)

    /** Reports the cell under the finger so the editor can draw the cursor highlight. */
    fun updateEditCursor(row: Int, col: Int) = editing.setCursor(row, col)

    fun clearEditCursor() = editing.clearCursor()

    /** Applies the active editing tool at a grid cell. */
    fun applyEdit(row: Int, col: Int, preferReference: Boolean = false) =
        editing.applyAt(row, col, preferReference)

    fun beginStroke() = editing.beginStroke()
    fun endStroke() = editing.endStroke()

    val canUndo: Boolean get() = editing.canUndo
    val canRedo: Boolean get() = editing.canRedo
    fun undoEdit() = editing.undo()
    fun redoEdit() = editing.redo()

    fun paletteFor(name: String): Palette? = generation.paletteFor(name)

    /** Confirmed palette change: remap every bead to the nearest new-palette color. */
    fun remapPalette(name: String) {
        val palette = generation.paletteFor(name) ?: return
        editing.remapTo(palette)
        generation.applyPaletteName(name)
    }

    /** Confirmed blank-resize: clear the grid to the new size (no source image). */
    fun clearGrid(size: Int) {
        editing.resizeTo(size)
        generation.applyGridSize(size)
    }

    /** Regenerates the pattern at a new size (source-image projects, keeps the source). */
    fun resizeGridRegenerate(size: Int) = generation.updateGridSize(size)

    /** Starts a blank pattern (no source image) and opens it directly in edit mode. */
    fun startFreeCreation(paletteName: String, size: Int) {
        val palette = generation.paletteFor(paletteName) ?: return
        openBlankProject(palette, Grid(size, size, List(size * size) { null }), editMode = true)
    }

    /** Starts a pattern from a built-in template, keeping the template's own size, in view mode. */
    fun startTemplateCreation(template: PindouTemplate) {
        val paletteName = paletteMetas.firstOrNull()?.name ?: return
        val palette = generation.paletteFor(paletteName) ?: return
        val w = template.width
        val h = template.height
        val cells = MutableList<String?>(w * h) { null }
        val mapper = PaletteMapper()
        template.sprite.forEachIndexed { r, row ->
            row.forEachIndexed { c, ch ->
                val rgb = template.colors[ch] ?: return@forEachIndexed
                cells[r * w + c] = mapper.nearestItem(rgb, palette.items).code
            }
        }
        openBlankProject(palette, Grid(w, h, cells), editMode = false)
    }

    /** Opens a blank (no source image) project, optionally entering edit mode. */
    private fun openBlankProject(palette: Palette, grid: Grid, editMode: Boolean) {
        val meta = ProjectMeta(
            gridWidth = grid.width,
            gridHeight = grid.height,
            imageType = ImageType.CARTOON.id,
            paletteName = palette.name,
            paletteVersion = palette.version,
            colorCountTarget = 0,
            createdAt = System.currentTimeMillis(),
        )
        documents.setWorking(null)
        documents.updateProject(PindouProject(meta, palette, emptyList(), grid, referenceGrid = null))
        documents.markFreeCreation()
        editing.clearHistory()
        generation.applyFree(palette.name, grid.width)
        if (editMode) editing.enter() else editing.exit()
        nav.updateShowReference(false)
        nav.go(Screen.Editor)
    }

    // ---- Export & share ---------------------------------------------------

    fun renderForm1(cellSize: Int? = null): Bitmap? = export.renderForm1(documents.project, cellSize)

    /** Resolves the chosen export size to px/cell and the resulting PNG dimensions. */
    fun resolvedExportPlan(p: PindouProject): Form1BitmapRenderer.ExportPlan =
        export.resolvedExportPlan(p)

    fun resolvedExportCellSize(p: PindouProject): Int = export.resolvedExportCellSize(p)

    fun exportPng(share: Boolean) = export.exportPng(documents.project, share)

    fun exportPindou(share: Boolean, onSaved: (() -> Unit)? = null) {
        export.exportPindou(documents.project, share, onSaved = {
            if (!share) {
                documents.markSaved()
                history.refresh()
                onSaved?.invoke()
            }
        })
    }

    /** Copies an externally opened `.pindou` into our Documents folder, regardless of the save flag. */
    fun importToDocuments() {
        exportPindou(share = false, onSaved = { documents.markImported(false) })
    }

    /** One-tap save to Documents that always embeds the reference image. */
    fun saveProject(onSaved: (() -> Unit)? = null) {
        // Nothing changed since the last save / open: skip the file write and its dialog.
        if (!documents.needsSave) {
            onSaved?.invoke()
            return
        }
        export.exportPindou(documents.project, share = false, onSaved = {
            documents.markSaved()
            history.refresh()
            onSaved?.invoke()
        })
    }
}

/** Export size presets: the default (largest size the device can hold) or an A4 single-page auto-fit. */
enum class ExportSize(val label: String) {
    MAX("默认尺寸"),
    A4("A4 自适应"),
}
