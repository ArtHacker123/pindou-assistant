package com.pindou.core.serialize

import com.pindou.core.model.PindouProject
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Raised when a `.pindou` file cannot be parsed or is an unsupported version. */
class ProjectFormatException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** Reads and writes the self-contained `.pindou` JSON document. */
object ProjectSerializer {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun encode(project: PindouProject): String =
        json.encodeToString(PindouProject.serializer(), project)

    fun decode(text: String): PindouProject {
        val project = try {
            json.decodeFromString(PindouProject.serializer(), text)
        } catch (e: SerializationException) {
            throw ProjectFormatException("Not a valid .pindou file", e)
        } catch (e: IllegalArgumentException) {
            throw ProjectFormatException("Not a valid .pindou file", e)
        }
        if (project.version.substringBefore('.').toIntOrNull() != SUPPORTED_MAJOR) {
            throw ProjectFormatException("Unsupported .pindou version: ${project.version}")
        }
        return project
    }

    private const val SUPPORTED_MAJOR = 1
}
