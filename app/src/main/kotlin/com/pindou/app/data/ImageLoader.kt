package com.pindou.app.data

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.pindou.core.model.PixelImage
import java.io.ByteArrayInputStream
import java.io.IOException

/** Loads images from a [Uri] and bridges between Android [Bitmap] and core [PixelImage]. */
object ImageLoader {

    private const val TAG = "ImageLoader"

    /** Cap the longest edge to keep memory bounded on very large photos. */
    const val MAX_DIMENSION = 2048

    fun loadBitmap(context: Context, uri: Uri): Bitmap {
        val resolver = context.contentResolver
        val bytes = readAll(resolver, uri)
            ?: throw IOException("Cannot open image (scheme=${uri.scheme})")
        Log.i(TAG, "loaded ${bytes.size} bytes from $uri")

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeStream(ByteArrayInputStream(bytes), null, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw IOException("Unsupported image format")
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, MAX_DIMENSION)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(bytes), null, options)
            ?: throw IOException("Cannot decode image")
        Log.i(TAG, "decoded ${bounds.outWidth}x${bounds.outHeight} as ${bitmap.width}x${bitmap.height}")
        return bitmap
    }

    private fun readAll(resolver: ContentResolver, uri: Uri): ByteArray? {
        val input = try {
            resolver.openInputStream(uri)
        } catch (e: Exception) {
            Log.w(TAG, "openInputStream failed for $uri", e)
            null
        } ?: return null
        try {
            return input.use { it.readBytes() }
        } catch (e: Exception) {
            Log.w(TAG, "read failed for $uri", e)
            return null
        }
    }

    private fun sampleSize(width: Int, height: Int, maxDimension: Int): Int {
        var sample = 1
        var w = width
        var h = height
        while (w > maxDimension || h > maxDimension) {
            w /= 2
            h /= 2
            sample *= 2
        }
        return sample
    }
}

fun Bitmap.toPixelImage(): PixelImage {
    val pixels = IntArray(width * height)
    getPixels(pixels, 0, width, 0, 0, width, height)
    return PixelImage(width, height, pixels)
}

fun PixelImage.toBitmap(): Bitmap =
    Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)