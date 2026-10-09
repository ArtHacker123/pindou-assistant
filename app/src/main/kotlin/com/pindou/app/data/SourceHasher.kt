package com.pindou.app.data

import com.pindou.core.model.PixelImage
import java.security.MessageDigest

/** Produces a stable fingerprint of the working image, used as the default seed. */
object SourceHasher {
    fun hash(image: PixelImage): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(4)
        for (pixel in image.pixels) {
            buffer[0] = (pixel ushr 24).toByte()
            buffer[1] = (pixel ushr 16).toByte()
            buffer[2] = (pixel ushr 8).toByte()
            buffer[3] = pixel.toByte()
            digest.update(buffer)
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
