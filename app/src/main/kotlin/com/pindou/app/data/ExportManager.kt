package com.pindou.app.data

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.pindou.core.model.PindouProject
import com.pindou.core.serialize.ProjectSerializer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/** Writes exported artifacts and shares them via the system share sheet. */
class ExportManager(private val context: Context) {

    private val authority: String get() = "${context.packageName}.fileprovider"

    /** Directory shown in the system gallery for saved PNGs (API 29+ MediaStore path). */
    private val galleryRelativePath = "Pictures/PindouAssistant"

    /** Directory shown in the system Documents folder for saved .pindou files (API 29+ MediaStore path). */
    private val documentsRelativePath = "Documents/PindouAssistant"

    /** Files land in the app's external files dir so FileProvider can share them. */
    fun exportDirectory(): File {
        val dir = File(context.getExternalFilesDir(null), "export")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun savePng(bitmap: Bitmap, baseName: String): File {
        val file = File(exportDirectory(), "$baseName.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return file
    }

    fun saveProject(project: PindouProject, baseName: String): File {
        val file = File(exportDirectory(), "$baseName.pindou")
        file.writeText(ProjectSerializer.encode(project))
        return file
    }

    /** Persists a PNG to the system gallery (visible in the Photos app). */
    fun savePngToGallery(bitmap: Bitmap, baseName: String): Uri {
        if (Build.VERSION.SDK_INT >= 29) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$baseName.png")
                put(MediaStore.Images.Media.MIME_TYPE, MIME_PNG)
                put(MediaStore.Images.Media.RELATIVE_PATH, galleryRelativePath)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val uri = resolver.insert(collection, values)
                ?: throw IOException("MediaStore 相册插入失败")
            try {
                resolver.openOutputStream(uri)?.use {
                    check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) { "PNG 编码失败" }
                } ?: throw IOException("无法写入相册内容")
                resolver.update(uri, ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                }, null, null)
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                throw e
            }
            return uri
        }
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        check(dir.exists() || dir.mkdirs()) { "无法创建相册目录" }
        val file = File(dir, "$baseName.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return Uri.fromFile(file)
    }

    /** Persists a .pindou project into the public Documents directory (user-accessible). */
    fun saveProjectToDocuments(project: PindouProject, baseName: String): Uri {
        if (Build.VERSION.SDK_INT >= 29) {
            val resolver = context.contentResolver
            val displayName = "$baseName.pindou"
            val existing = findDocument(displayName)
            if (existing != null) {
                resolver.openOutputStream(existing, "wt")?.use {
                    it.write(ProjectSerializer.encode(project).toByteArray(Charsets.UTF_8))
                } ?: throw IOException("无法覆盖文档内容")
                return existing
            }
            val values = ContentValues().apply {
                put(MediaStore.Files.FileColumns.DISPLAY_NAME, displayName)
                put(MediaStore.Files.FileColumns.MIME_TYPE, MIME_PINDOU)
                put(MediaStore.Files.FileColumns.RELATIVE_PATH, documentsRelativePath)
                put(MediaStore.Files.FileColumns.IS_PENDING, 1)
            }
            val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val uri = resolver.insert(collection, values)
                ?: throw IOException("MediaStore 文档插入失败")
            try {
                resolver.openOutputStream(uri)?.use {
                    it.write(ProjectSerializer.encode(project).toByteArray(Charsets.UTF_8))
                } ?: throw IOException("无法写入文档内容")
                resolver.update(uri, ContentValues().apply {
                    put(MediaStore.Files.FileColumns.IS_PENDING, 0)
                }, null, null)
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                throw e
            }
            return uri
        }
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        check(dir.exists() || dir.mkdirs()) { "无法创建文档目录" }
        val file = File(dir, "$baseName.pindou")
        file.writeText(ProjectSerializer.encode(project))
        return Uri.fromFile(file)
    }

    /** Finds an already-saved `.pindou` with [displayName] in [documentsRelativePath] so re-saves overwrite it. */
    private fun findDocument(displayName: String): Uri? {
        val resolver = context.contentResolver
        val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val selection = "${MediaStore.Files.FileColumns.RELATIVE_PATH} LIKE ? AND " +
            "${MediaStore.Files.FileColumns.DISPLAY_NAME}=?"
        val args = arrayOf("%$documentsRelativePath%", displayName)
        resolver.query(collection, arrayOf(MediaStore.Files.FileColumns._ID), selection, args, null)?.use { c ->
            if (c.moveToFirst()) {
                return ContentUris.withAppendedId(collection, c.getLong(0))
            }
        }
        return null
    }

    /** A cache file the camera can write into; shared via FileProvider. */
    fun newCameraFile(): File {
        val dir = File(context.cacheDir, "share")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "camera_${System.currentTimeMillis()}.jpg")
    }

    fun uriFor(file: File): Uri = FileProvider.getUriForFile(context, authority, file)

    /** Copies an already-saved document (content or file uri) into the share dir for FileProvider sharing. */
    fun copyToShare(uri: Uri, displayName: String): File {
        val name = displayName.takeIf { it.isNotBlank() } ?: "pattern.pindou"
        val fileName = if (name.endsWith(".pindou", ignoreCase = true)) name else "$name.pindou"
        val file = File(exportDirectory(), fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        } ?: throw IOException("无法读取文件")
        return file
    }

    fun share(file: File, mimeType: String, chooserTitle: String, fallbackMimeType: String? = null) {
        val uri = uriFor(file)
        val send = sendIntent(uri, mimeType)
        val chosen = if (fallbackMimeType != null &&
            send.resolveActivity(context.packageManager) == null
        ) {
            sendIntent(uri, fallbackMimeType)
        } else {
            send
        }
        context.startActivity(
            Intent.createChooser(chosen, chooserTitle)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    private fun sendIntent(uri: Uri, type: String): Intent = Intent(Intent.ACTION_SEND).apply {
        this.type = type
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    companion object {
        const val MIME_PNG = "image/png"
        const val MIME_PINDOU = "application/x-pindou"

        /** Generic binary MIME; used when sharing `.pindou` since no app registers the custom type. */
        const val MIME_BINARY = "application/octet-stream"

        /** Last-resort wildcard so the share sheet always has targets. */
        const val MIME_ANY = "*/*"
    }
}
