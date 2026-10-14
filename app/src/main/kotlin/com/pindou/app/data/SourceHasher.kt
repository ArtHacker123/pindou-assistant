package com.pindou.app.data

import com.pindou.core.model.PixelImage
import java.security.MessageDigest

/** Produces a stable fingerprint of the working image, used as the default seed. */
object SourceHasher {

    /**
     * Bytes fed to the digest per flush. A multiple of 4 so a pixel never
     * straddles a flush. Batching is byte-for-byte identical to feeding the
     * digest one pixel at a time, but turns millions of tiny `update` calls into
     * a few dozen, which is the difference between seconds and milliseconds on
     * device.
     */
    private const val CHUNK_BYTES = 64 * 1024

    fun hash(image: PixelImage): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(CHUNK_BYTES)
        var j = 0
        for (pixel in image.pixels) {
            buffer[j++] = (pixel ushr 24).toByte()
            buffer[j++] = (pixel ushr 16).toByte()
            buffer[j++] = (pixel ushr 8).toByte()
            buffer[j++] = pixel.toByte()
            if (j == CHUNK_BYTES) {
                digest.update(buffer)
                j = 0
            }
        }
        if (j > 0) digest.update(buffer, 0, j)
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
