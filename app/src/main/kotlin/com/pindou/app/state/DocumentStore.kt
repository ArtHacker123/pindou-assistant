package com.pindou.app.state

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pindou.core.model.Grid
import com.pindou.core.model.Palette
import com.pindou.core.model.PaletteItem
import com.pindou.core.model.PindouProject
import com.pindou.core.model.PixelImage
import com.pindou.core.model.ReferenceGrid
import com.pindou.core.serialize.ProjectFormatException
import com.pindou.core.serialize.ProjectSerializer
import com.pindou.core.util.sortedNaturallyBy

/**
 * Owns the user's document: the originally picked bitmap, the working image
 * derived from it (cropped/padded), and the current [PindouProject]. Also
 * handles decoding a `.pindou` file from a content [Uri].
 */
class DocumentStore(private val context: Context) {

    var originalBitmap by mutableStateOf<Bitmap?>(null)
        private set

    var workingImage by mutableStateOf<PixelImage?>(null)
        private set

    var project by mutableStateOf<PindouProject?>(null)
        private set

    /** Whether the current grid carries manual edits since the last generation/open. */
    var edited by mutableStateOf(false)
        private set

    /** Whether the current project was started from "自由创作" (no source image). */
    var isFreeCreation by mutableStateOf(false)
        private set

    /** Whether the project has changes that have not yet been saved to a `.pindou` document. */
    var needsSave by mutableStateOf(false)
        private set

    /** Whether the project was opened from an external source (e.g. a shared `.pindou`). */
    var imported by mutableStateOf(false)
        private set

    /** A transient suggested name (e.g. a template name) used until the project is named or replaced. */
    var suggestedName by mutableStateOf<String?>(null)
        private set

    /** Base name of the `.pindou` in Documents that this project owns (null until saved/opened here). */
    var ownPindouName by mutableStateOf<String?>(null)
        private set

    /** Base name of the PNG in the gallery that this project owns (null until saved here). */
    var ownPngName by mutableStateOf<String?>(null)
        private set

    fun markSuggestedName(name: String?) {
        suggestedName = name?.takeIf { it.isNotBlank() }
    }

    /** Records the `.pindou` base name this project is responsible for (so re-saves don't warn). */
    fun markPindouSaved(baseName: String) {
        ownPindouName = baseName.takeIf { it.isNotBlank() }
    }

    /** Records the PNG base name this project is responsible for (so re-saves don't warn). */
    fun markPngSaved(baseName: String) {
        ownPngName = baseName.takeIf { it.isNotBlank() }
    }

    /** Records whether the project came from an external file (drives the "save to my patterns" prompt). */
    fun markImported(value: Boolean) {
        imported = value
    }

    fun markFreeCreation() {
        isFreeCreation = true
    }

    fun markGenerated() {
        isFreeCreation = false
    }

    /** Records that the current project was just persisted to a `.pindou` document. */
    fun markSaved() {
        needsSave = false
    }

    /** A committed baseline (content + flags) that "discard changes" reverts to. */
    private data class Snapshot(
        val project: PindouProject,
        val edited: Boolean,
        val needsSave: Boolean,
        val isFreeCreation: Boolean,
        val imported: Boolean,
        val suggestedName: String?,
        val ownPindouName: String?,
        val ownPngName: String?,
    )

    private var committed: Snapshot? = null

    /** Records the current project as the baseline that discarding unsaved changes reverts to. */
    fun markCommitted() {
        val p = project
        committed = if (p == null) {
            null
        } else {
            Snapshot(
                project = p,
                edited = edited,
                needsSave = needsSave,
                isFreeCreation = isFreeCreation,
                imported = imported,
                suggestedName = suggestedName,
                ownPindouName = ownPindouName,
                ownPngName = ownPngName,
            )
        }
    }

    /** Reverts to the last committed baseline, discarding all unsaved changes. */
    fun revertToCommitted() {
        val s = committed ?: return
        project = s.project
        edited = s.edited
        needsSave = s.needsSave
        isFreeCreation = s.isFreeCreation
        imported = s.imported
        suggestedName = s.suggestedName
        ownPindouName = s.ownPindouName
        ownPngName = s.ownPngName
    }

    fun setOriginal(bitmap: Bitmap?) {
        originalBitmap = bitmap
    }

    fun setWorking(image: PixelImage?) {
        workingImage = image
    }

    fun updateProject(value: PindouProject?) {
        project = value
        edited = false
        needsSave = true
        imported = false
        suggestedName = null
        ownPindouName = null
        ownPngName = null
    }

    /** Replaces the grid in place (an edit), keeping the palette and reference when still aligned. */
    fun replaceGrid(grid: Grid) {
        val p = project ?: return
        project = p.copy(
            grid = grid,
            paletteUsed = usedItems(p.paletteSource, grid),
            meta = p.meta.copy(gridWidth = grid.width, gridHeight = grid.height),
            referenceGrid = p.referenceGrid?.takeIf { it.width == grid.width && it.height == grid.height },
        )
        edited = true
        needsSave = true
        imported = false
    }

    /** Replaces the grid and the palette together (a palette remap). */
    fun replaceGridAndPalette(grid: Grid, palette: Palette) {
        val p = project ?: return
        project = p.copy(
            grid = grid,
            paletteSource = palette,
            paletteUsed = usedItems(palette, grid),
            meta = p.meta.copy(
                paletteName = palette.name,
                paletteVersion = palette.version,
                gridWidth = grid.width,
                gridHeight = grid.height,
            ),
            referenceGrid = p.referenceGrid?.takeIf { it.width == grid.width && it.height == grid.height },
        )
        edited = true
        needsSave = true
        imported = false
    }

    /** Sets or clears the grid-resolution reference image. */
    fun setReferenceGrid(reference: ReferenceGrid?) {
        val p = project ?: return
        project = p.copy(referenceGrid = reference)
        needsSave = true
    }

    /** Applies a user-chosen pattern name (embedded in the next `.pindou` save). */
    fun updateName(name: String) {
        val p = project ?: return
        if (p.meta.name == name) return
        project = p.copy(meta = p.meta.copy(name = name))
        needsSave = true
    }

    /** Palette items actually present in [grid], naturally sorted by code (for the color table). */
    internal fun usedItems(palette: Palette, grid: Grid): List<PaletteItem> =
        grid.distinctCodes().mapNotNull { palette.item(it) }.sortedNaturallyBy { it.code }

    /** Reads and decodes a `.pindou` document from [uri]. */
    fun open(uri: Uri): PindouProject {
        val text = context.contentResolver.openInputStream(uri)
            ?.bufferedReader()?.use { it.readText() }
            ?: throw ProjectFormatException("无法读取文件")
        return ProjectSerializer.decode(text)
    }

    /** Best-effort display name for a content [uri], used to sanity-check an opened file. */
    fun displayName(uri: Uri): String? = try {
        context.contentResolver.query(
            uri,
            arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && index >= 0) cursor.getString(index) else null
        }
    } catch (e: Exception) {
        null
    }
}
