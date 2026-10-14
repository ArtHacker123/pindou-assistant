package com.pindou.app

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pindou.app.data.ExportManager
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

    /** Whether the eraser is in region-erase mode. */
    val eraseConnected: Boolean get() = editing.eraseConnected

    /** The label for the active tool, reflecting the eraser's region-erase mode. */
    val editToolLabel: String
        get() = if (editing.tool == EditTool.ERASE && editing.eraseConnected) {
            "橡皮·区域擦除"
        } else {
            editing.tool.label
        }

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

    /** Whether the current project already carries a name (so the save prompt can be skipped). */
    val hasName: Boolean get() = documents.project?.meta?.name?.isNotBlank() == true

    /** Whether the current project was opened from an external file (not yet in our Documents). */
    val imported: Boolean get() = documents.imported

    /** A save name that already exists as a different file and awaits an overwrite confirmation. */
    var overwriteName by mutableStateOf<String?>(null)
        private set
    private var overwriteAction: (() -> Unit)? = null

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
                documents.clearProject()
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
        documents.markCommitted()
    }

    fun openPindou(uri: Uri, external: Boolean = false) {
        val name = documents.displayName(uri)
        try {
            val parsed = documents.open(uri)
            openProject(parsed)
            documents.markImported(external)
            // A file opened from our own Documents is what this project owns, so a re-save won't warn.
            if (!external && parsed.meta.name.isNotBlank()) {
                val base = ExportManager.safeFileName(parsed.meta.name)
                documents.markPindouSaved(base)
                documents.markPngSaved(base)
            }
            documents.markCommitted()
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
    fun shareHistory(entry: PatternEntry) = export.shareStoredPindou(entry.uri, entry.displayName)
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

    /** Toggles the eraser between single-cell and region erase. */
    fun toggleEraseConnected() = editing.toggleEraseConnected()

    /** Discards all unsaved changes, reverting the project to the last committed state. */
    fun discardEdits() {
        documents.revertToCommitted()
        documents.project?.let { generation.applyFromProject(it) }
        editing.clearHistory()
    }

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
        openBlankProject(palette, Grid(w, h, cells), editMode = false, suggestedName = template.name)
    }

    /** Opens a blank (no source image) project, optionally entering edit mode. */
    private fun openBlankProject(
        palette: Palette,
        grid: Grid,
        editMode: Boolean,
        suggestedName: String = "",
    ) {
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
        documents.updateProject(
            PindouProject(meta, palette, documents.usedItems(palette, grid), grid, referenceGrid = null),
        )
        documents.markSuggestedName(suggestedName)
        documents.markFreeCreation()
        editing.clearHistory()
        generation.applyFree(palette.name, grid.width)
        if (editMode) editing.enter() else editing.exit()
        nav.updateShowReference(false)
        nav.go(Screen.Editor)
        documents.markCommitted()
    }

    // ---- Export & share ---------------------------------------------------

    fun renderForm1(cellSize: Int? = null): Bitmap? = export.renderForm1(documents.project, cellSize)

    /** Resolves the chosen export size to px/cell and the resulting PNG dimensions. */
    fun resolvedExportPlan(p: PindouProject): Form1BitmapRenderer.ExportPlan =
        export.resolvedExportPlan(p)

    fun resolvedExportCellSize(p: PindouProject): Int = export.resolvedExportCellSize(p)

    fun exportPng(share: Boolean, name: String) {
        if (!share && pngNameConflicts(name)) {
            overwriteName = name
            overwriteAction = { exportPngConfirmed(share = false, name = name) }
            return
        }
        exportPngConfirmed(share, name)
    }

    private fun exportPngConfirmed(share: Boolean, name: String) {
        documents.updateName(name)
        val base = ExportManager.safeFileName(name)
        export.exportPng(documents.project, share, name, onSaved = {
            if (!share) documents.markPngSaved(base)
        })
    }

    fun exportPindou(share: Boolean, name: String, onSaved: (() -> Unit)? = null) {
        if (!share && pindouNameConflicts(name)) {
            overwriteName = name
            overwriteAction = { exportPindouConfirmed(share = false, name = name, onSaved = onSaved) }
            return
        }
        exportPindouConfirmed(share, name, onSaved)
    }

    private fun exportPindouConfirmed(
        share: Boolean,
        name: String,
        onSaved: (() -> Unit)?,
    ) {
        documents.updateName(name)
        val base = ExportManager.safeFileName(name)
        export.exportPindou(documents.project, share, name, onSaved = {
            if (!share) {
                documents.markSaved()
                documents.markPindouSaved(base)
                documents.markCommitted()
                history.refresh()
                onSaved?.invoke()
            }
        })
    }

    /** True when saving under [name] would overwrite a `.pindou` that belongs to a different file. */
    fun pindouNameConflicts(name: String): Boolean {
        val base = ExportManager.safeFileName(name)
        return documents.ownPindouName != base && export.pindouExists(base)
    }

    /** True when saving a PNG under [name] would overwrite an image that belongs to a different file. */
    fun pngNameConflicts(name: String): Boolean {
        val base = ExportManager.safeFileName(name)
        return documents.ownPngName != base && export.pngExists(base)
    }

    /** Clears the pending overwrite prompt after the user confirms; runs the guarded save/export. */
    fun confirmOverwrite() {
        val action = overwriteAction
        overwriteName = null
        overwriteAction = null
        action?.invoke()
    }

    /** Clears the pending overwrite prompt after the user cancels; nothing is written. */
    fun dismissOverwrite() {
        overwriteName = null
        overwriteAction = null
    }

    /** Copies an externally opened `.pindou` into our Documents folder, regardless of the save flag. */
    fun importToDocuments(name: String) {
        exportPindou(share = false, name = name, onSaved = { documents.markImported(false) })
    }

    /** One-tap save to Documents under the chosen [name], embedding the reference image. */
    fun saveProject(name: String, onSaved: (() -> Unit)? = null) {
        exportPindou(share = false, name = name, onSaved = onSaved)
    }

    /** Default name shown in the save dialog: the current name, then a suggestion, then a size default. */
    fun suggestName(): String {
        val p = documents.project
        val existing = p?.meta?.name?.takeIf { it.isNotBlank() }
        if (existing != null) return existing
        documents.suggestedName?.let { return it }
        val w = p?.grid?.width ?: 0
        val h = p?.grid?.height ?: 0
        return if (w > 0 && h > 0) "拼豆 $w×$h" else "拼豆图纸"
    }
}

/** Export size presets: the default (largest size the device can hold) or an A4 single-page auto-fit. */
enum class ExportSize(val label: String) {
    MAX("默认尺寸"),
    A4("A4 自适应"),
}
