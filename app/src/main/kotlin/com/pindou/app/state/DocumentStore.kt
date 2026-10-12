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
    }

    /** Sets or clears the grid-resolution reference image. */
    fun setReferenceGrid(reference: ReferenceGrid?) {
        val p = project ?: return
        project = p.copy(referenceGrid = reference)
        needsSave = true
    }

    private fun usedItems(palette: Palette, grid: Grid): List<PaletteItem> =
        grid.distinctCodes().mapNotNull { palette.item(it) }.sortedNaturallyBy { it.code }

    /** Reads and decodes a `.pindou` document from [uri]. */
    fun open(uri: Uri): PindouProject {
        val text = context.contentResolver.openInputStream(uri)
            ?.bufferedReader()?.use { it.readText() }
            ?: throw ProjectFormatException("无法读取文件")
        return ProjectSerializer.decode(text)
    }
}
