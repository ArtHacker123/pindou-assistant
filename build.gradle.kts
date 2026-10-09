plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("org.jetbrains.kotlin.jvm") version "1.9.20" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20" apply false
}

// The project lives under a non-ASCII path ("拼豆助手"), which breaks the
// Windows test worker's classpath handling. Redirect all build outputs to an
// ASCII-only location to keep tooling happy without moving the sources.
val asciiBuildRoot = File("G:/pindou-build")

allprojects {
    layout.buildDirectory.set(File(asciiBuildRoot, project.name))
}

