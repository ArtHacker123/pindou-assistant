package com.pindou.app.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pindou.core.editor.GridEditor
import com.pindou.core.imaging.ImageType
import com.pindou.core.model.Grid
import com.pindou.core.model.Palette
import com.pindou.core.model.PindouProject
import com.pindou.core.model.PixelImage
import com.pindou.core.model.ReferenceGrid
import com.pindou.core.model.toReferenceGrid

/** The editing tools available in edit mode. */
enum class EditTool(val label: String) {
    PAINT("画笔"),
    ERASE("橡皮"),
    EYEDROPPER("取色"),
    BUCKET("油漆桶"),
    MOVE("移动"),
}

/**
 * Owns the editing UI state (mode, active tool, brush color) and applies pure
 * [GridEditor] operations to the document. Every edit is committed to the
 * [DocumentStore] immediately; the app reads the resulting project as the
 * source of truth for rendering, saving and exporting.
 */
class EditingSession(
    private val documents: DocumentStore,
    private val references: ReferenceService,
) {

    var editMode by mutableStateOf(false)
        private set

    var tool by mutableStateOf(EditTool.PAINT)
        private set

    var brushCode by mutableStateOf<String?>(null)
        private set

    /** The cell currently under the finger in edit mode, for the cursor highlight. */
    var cursor by mutableStateOf<Pair<Int, Int>?>(null)
        private set

    var canUndo by mutableStateOf(false)
        private set

    var canRedo by mutableStateOf(false)
        private set

    private val historyLimit = 100
    private val undoStack = ArrayDeque<Grid>()
    private val redoStack = ArrayDeque<Grid>()

    /** Scratch state for coalescing one undo step per drag stroke. */
    private var strokeBase: Grid? = null
    private var strokeCells: MutableList<String?>? = null
    private var strokeDirty = false

    private var refCacheKey: ReferenceGrid? = null
    private var refCacheImage: PixelImage? = null

    fun enter() {
        if (brushCode == null) {
            brushCode = documents.project?.let { defaultBrushCode(it) }
        }
        editMode = true
    }

    fun exit() {
        editMode = false
    }

    fun selectTool(value: EditTool) {
        tool = value
    }

    fun selectBrush(code: String?) {
        brushCode = code
    }

    fun setCursor(row: Int, col: Int) {
        cursor = row to col
    }

    fun clearCursor() {
        cursor = null
    }

    /** The palette color closest to pure black, used as the default brush. */
    fun defaultBrushCode(project: PindouProject): String? =
        project.paletteSource.items.minByOrNull { it.r + it.g + it.b }?.code

    /** Starts a drag stroke (called by the grid on finger-down); edits coalesce into one undo step. */
    fun beginStroke() {
        if (strokeCells != null) return
        val project = documents.project ?: return
        strokeBase = project.grid
        strokeCells = project.grid.cells.toMutableList()
        strokeDirty = false
    }

    /** Completes a drag stroke, committing the scratch grid to the document and one undo entry. */
    fun endStroke() {
        val cells = strokeCells ?: return
        val base = strokeBase ?: run {
            strokeCells = null
            strokeBase = null
            strokeDirty = false
            return
        }
        if (strokeDirty) {
            pushUndo(base)
            documents.replaceGrid(Grid(base.width, base.height, cells))
            redoStack.clear()
        }
        strokeCells = null
        strokeBase = null
        strokeDirty = false
        refreshHistory()
    }

    /**
     * Applies the active tool at a grid cell (an in-stroke move or a tap).
     * [preferReference] makes the eyedropper sample the reference image first,
     * matching the "reference only" peek view. Grid-changing tools write into
     * the current stroke scratch when one is open.
     */
    fun applyAt(row: Int, col: Int, preferReference: Boolean = false) {
        val project = documents.project ?: return
        when (tool) {
            EditTool.PAINT ->
                strokeEdit { g: Grid -> GridEditor.paint(g, row, col, brushCode!!) }

            EditTool.ERASE ->
                strokeEdit { g: Grid -> GridEditor.erase(g, row, col) }

            EditTool.BUCKET ->
                strokeEdit { g: Grid -> GridEditor.floodFill(g, row, col, brushCode!!) }

            EditTool.EYEDROPPER -> {
                val picked = GridEditor.pick(
                    grid = project.grid,
                    reference = referenceImage(project),
                    target = project.paletteSource,
                    preferReference = preferReference,
                    row = row,
                    col = col,
                )
                if (picked != null) {
                    brushCode = picked
                    tool = EditTool.PAINT
                }
            }

            EditTool.MOVE -> Unit
        }
    }

    /** Applies a grid op inside the stroke scratch; a standalone tap begins and ends a mini-stroke. */
    private fun strokeEdit(op: (Grid) -> Grid) {
        val wasInStroke = strokeCells != null
        if (!wasInStroke) beginStroke()
        val cells = strokeCells ?: return
        val base = strokeBase ?: return
        val tmp = Grid(base.width, base.height, cells)
        val next = op(tmp)
        if (next !== tmp) {
            strokeCells = next.cells.toMutableList()
            strokeDirty = true
        }
        if (!wasInStroke) endStroke()
        else refreshHistory()
    }

    /** Undoes the last committed edit (stroke, remap, or resize). */
    fun undo() {
        val current = documents.project ?: return
        val next = undoStack.removeLastOrNull() ?: return
        redoStack.addLast(current.grid)
        documents.replaceGrid(next)
        refreshHistory()
    }

    /** Re-applies the most recently undone edit. */
    fun redo() {
        val current = documents.project ?: return
        val next = redoStack.removeLastOrNull() ?: return
        undoStack.addLast(current.grid)
        documents.replaceGrid(next)
        refreshHistory()
    }

    private fun pushUndo(grid: Grid) {
        undoStack.addLast(grid)
        if (undoStack.size > historyLimit) undoStack.removeFirst()
    }

    /** Drops the edit history (new / opened / regenerated document) plus any in-flight stroke. */
    fun clearHistory() {
        undoStack.clear()
        redoStack.clear()
        strokeBase = null
        strokeCells = null
        strokeDirty = false
        refreshHistory()
    }

    private fun refreshHistory() {
        canUndo = undoStack.isNotEmpty()
        canRedo = redoStack.isNotEmpty()
    }

    /** Confirmed palette change: remap every bead to the nearest new-palette color. */
    fun remapTo(palette: Palette) {
        val project = documents.project ?: return
        pushUndo(project.grid)
        documents.replaceGridAndPalette(
            GridEditor.remap(project.grid, project.paletteSource, palette),
            palette,
        )
        redoStack.clear()
        refreshHistory()
    }

    /** Confirmed blank-resize: clear to an empty grid of [size] x [size] (free creation). */
    fun resizeTo(size: Int) {
        val project = documents.project ?: return
        pushUndo(project.grid)
        documents.replaceGrid(Grid(size, size, List(size * size) { null }))
        redoStack.clear()
        refreshHistory()
    }

    /** Loads a source image and stores its grid-resolution reference on the project. */
    fun loadReference(image: PixelImage, imageType: ImageType) {
        val project = documents.project ?: return
        val reference = references.referenceFrom(image, imageType, project.grid.width, project.grid.height)
        documents.setReferenceGrid(reference.toReferenceGrid())
    }

    private fun referenceImage(project: PindouProject): PixelImage? {
        val grid = project.referenceGrid
        if (grid == null) {
            refCacheKey = null
            refCacheImage = null
            return null
        }
        if (grid !== refCacheKey) {
            refCacheKey = grid
            refCacheImage = grid.toImage()
        }
        return refCacheImage
    }
}
