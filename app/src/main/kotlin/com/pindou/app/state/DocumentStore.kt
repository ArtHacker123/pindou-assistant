package com.pindou.app.state

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pindou.core.model.PindouProject
import com.pindou.core.model.PixelImage
import com.pindou.core.serialize.ProjectFormatException
import com.pindou.core.serialize.ProjectSerializer

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

    fun setOriginal(bitmap: Bitmap?) {
        originalBitmap = bitmap
    }

    fun setWorking(image: PixelImage?) {
        workingImage = image
    }

    fun updateProject(value: PindouProject?) {
        project = value
    }

    /** Reads and decodes a `.pindou` document from [uri]. */
    fun open(uri: Uri): PindouProject {
        val text = context.contentResolver.openInputStream(uri)
            ?.bufferedReader()?.use { it.readText() }
            ?: throw ProjectFormatException("无法读取文件")
        return ProjectSerializer.decode(text)
    }
}
