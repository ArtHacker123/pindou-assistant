package com.pindou.app.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import com.pindou.core.model.PindouProject
import com.pindou.core.serialize.ProjectSerializer
import java.io.File
import java.io.FileOutputStream

/** Writes exported artifacts and shares them via the system share sheet. */
class ExportManager(private val context: Context) {

    private val authority: String get() = "${context.packageName}.fileprovider"

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

    /** A cache file the camera can write into; shared via FileProvider. */
    fun newCameraFile(): File {
        val dir = File(context.cacheDir, "share")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "camera_${System.currentTimeMillis()}.jpg")
    }

    fun uriFor(file: File): Uri = FileProvider.getUriForFile(context, authority, file)

    fun share(file: File, mimeType: String, chooserTitle: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uriFor(file))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }

    companion object {
        const val MIME_PNG = "image/png"
        const val MIME_PINDOU = "application/x-pindou"
    }
}
