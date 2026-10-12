package com.pindou.core.imaging

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the layering contract: the `imaging` package (image ops, sampling,
 * grid sampling, reference building) must stay a low-level module and never
 * depend on the higher layers (`generate`, `quant`, `match`, `serialize`,
 * `render`). `model`/`color`/`util` are shared leaves and allowed.
 */
class PackageBoundaryTest {

    @Test
    fun `imaging does not depend on higher layers`() {
        assertNoForbiddenImports(
            packageName = "imaging",
            allowedRoots = setOf("model", "color", "util", "imaging"),
        )
    }

    @Test
    fun `editor does not depend on higher layers`() {
        assertNoForbiddenImports(
            packageName = "editor",
            allowedRoots = setOf("model", "color", "util", "editor"),
        )
    }

    private fun assertNoForbiddenImports(packageName: String, allowedRoots: Set<String>) {
        val dir = File("src/main/kotlin/com/pindou/core/$packageName")
        assertTrue("$packageName source dir not found at ${dir.absolutePath}", dir.isDirectory)

        val violations = ArrayList<String>()
        dir.listFiles { f -> f.extension == "kt" }?.sortedBy { it.name }?.forEach { file ->
            file.readLines().forEachIndexed { index, line ->
                val prefix = "import com.pindou.core."
                if (line.startsWith(prefix)) {
                    val pkg = line.removePrefix(prefix).substringBefore('.').trim()
                    if (pkg !in allowedRoots) {
                        violations.add("${file.name}:${index + 1} imports com.pindou.core.$pkg")
                    }
                }
            }
        }

        assertTrue("$packageName must not depend on higher layers:\n${violations.joinToString("\n")}", violations.isEmpty())
    }
}
