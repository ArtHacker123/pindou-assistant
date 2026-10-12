package com.pindou.app.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.pindou.core.model.PindouProject
import com.pindou.core.serialize.ProjectSerializer
import java.io.File

/** One saved `.pindou` file, with its decoded project (for the thumbnail) when readable. */
data class PatternEntry(
    val uri: Uri,
    val displayName: String,
    val modified: Long,
    val project: PindouProject?,
) {
    val width: Int get() = project?.grid?.width ?: 0
    val height: Int get() = project?.grid?.height ?: 0

    /** Card title: the project name when set, else "width×height", else the file name. */
    val title: String
        get() = project?.meta?.name?.takeIf { it.isNotBlank() }
            ?: project?.let { "${it.grid.width}×${it.grid.height}" }
            ?: displayName

    /** Best available timestamp for sorting/display. */
    val timestamp: Long get() = project?.meta?.createdAt?.takeIf { it > 0L } ?: modified
}

/** Enumerates and deletes saved `.pindou` files under the public Documents/PindouAssistant folder. */
class HistoryStore(private val context: Context) {

    private val relativePath = "Documents/PindouAssistant"

    /** All saved patterns, newest first. Blocking I/O — call off the main thread. */
    fun list(): List<PatternEntry> =
        rawFiles()
            .sortedByDescending { it.modified }
            .map { PatternEntry(it.uri, it.name, it.modified, decode(it.uri)) }

    /** Removes a saved `.pindou` file. */
    fun delete(uri: Uri) {
        if (Build.VERSION.SDK_INT >= 29) {
            context.contentResolver.delete(uri, null, null)
        } else {
            val path = uri.path ?: return
            runCatching { File(path).delete() }
        }
    }

    private data class RawFile(val uri: Uri, val name: String, val modified: Long)

    private fun decode(uri: Uri): PindouProject? = runCatching {
        val text = context.contentResolver.openInputStream(uri)
            ?.bufferedReader()?.use { it.readText() } ?: return@runCatching null
        ProjectSerializer.decode(text)
    }.getOrNull()

    private fun rawFiles(): List<RawFile> =
        if (Build.VERSION.SDK_INT >= 29) listMediaStore() else listLegacy()

    private fun listMediaStore(): List<RawFile> {
        val resolver = context.contentResolver
        val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val selection = "${MediaStore.Files.FileColumns.RELATIVE_PATH} LIKE ? AND " +
            "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?"
        val args = arrayOf("%$relativePath%", "%.pindou")
        val columns = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
        )
        val out = mutableListOf<RawFile>()
        resolver.query(collection, columns, selection, args, null)?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val dateCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            while (c.moveToNext()) {
                val name = c.getString(nameCol) ?: continue
                out += RawFile(
                    uri = ContentUris.withAppendedId(collection, c.getLong(idCol)),
                    name = name,
                    modified = c.getLong(dateCol) * 1000L,
                )
            }
        }
        return out
    }

    private fun listLegacy(): List<RawFile> {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            relativePath,
        )
        val files = dir.listFiles { f -> f.isFile && f.name.endsWith(".pindou") } ?: return emptyList()
        return files.map { RawFile(Uri.fromFile(it), it.name, it.lastModified()) }
    }
}
