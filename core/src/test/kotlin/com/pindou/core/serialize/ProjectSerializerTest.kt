package com.pindou.core.serialize

import com.pindou.core.TestPalettes
import com.pindou.core.model.Grid
import com.pindou.core.model.PindouProject
import com.pindou.core.model.ProjectMeta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ProjectSerializerTest {

    private fun project(): PindouProject = PindouProject(
        meta = ProjectMeta(
            gridWidth = 2,
            gridHeight = 2,
            imageType = "cartoon",
            paletteName = "test",
            paletteVersion = "1.0.0",
            colorCountTarget = 3,
            createdAt = 1000L,
            name = "sample",
        ),
        paletteSource = TestPalettes.basic(),
        paletteUsed = listOf(TestPalettes.black, TestPalettes.white),
        grid = Grid(2, 2, listOf("C1", "C2", "C2", "C1")),
    )

    @Test
    fun `round trips a project`() {
        val original = project()
        val text = ProjectSerializer.encode(original)
        val decoded = ProjectSerializer.decode(text)
        assertEquals(original, decoded)
    }

    @Test
    fun `rejects malformed json`() {
        assertThrows(ProjectFormatException::class.java) {
            ProjectSerializer.decode("{ not json at all")
        }
    }

    @Test
    fun `rejects unsupported version`() {
        val text = ProjectSerializer.encode(project().copy(version = "2.0.0"))
        assertThrows(ProjectFormatException::class.java) {
            ProjectSerializer.decode(text)
        }
    }

    @Test
    fun `rejects structurally invalid grid`() {
        // cells count does not match width*height -> constructor throws internally.
        val text = """
            {
              "meta": {
                "gridWidth": 2, "gridHeight": 2, "imageType": "cartoon",
                "paletteName": "test", "paletteVersion": "1.0.0",
                "colorCountTarget": 2, "createdAt": 0
              },
              "paletteSource": { "name": "test", "items": [
                {"code":"C1","r":0,"g":0,"b":0}
              ]},
              "paletteUsed": [],
              "grid": { "width": 2, "height": 2, "cells": ["C1"] },
              "version": "1.0.0"
            }
        """.trimIndent()
        assertThrows(ProjectFormatException::class.java) {
            ProjectSerializer.decode(text)
        }
    }
}
