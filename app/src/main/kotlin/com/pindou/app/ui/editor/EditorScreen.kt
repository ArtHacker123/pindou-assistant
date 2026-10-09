package com.pindou.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pindou.app.PindouController
import com.pindou.core.model.PindouProject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(controller: PindouController) {
    val project = controller.project ?: return
    val grid = project.grid
    val itemByCode = remember(project) { project.paletteSource.items.associateBy { it.code } }
    val counts = remember(project) { grid.cells.groupingBy { it }.eachCount() }

    var selectedCode by remember(project) { mutableStateOf<String?>(null) }
    var selectedPos by remember(project) { mutableStateOf<Pair<Int, Int>?>(null) }
    var tableExpanded by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("交互图纸 ${grid.width}×${grid.height}") },
                navigationIcon = {
                    IconButton(onClick = { controller.goHome() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(onClick = { controller.goExport() }) { Text("导出") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFEEEEEE)),
            ) {
                PatternGrid(
                    project = project,
                    selectedCode = selectedCode,
                    onCellTap = { row, col ->
                        if (row in 0 until grid.height && col in 0 until grid.width) {
                            selectedPos = row to col
                            selectedCode = grid[row, col]
                        } else {
                            selectedPos = null
                            selectedCode = null
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            InfoBar(
                selectedCode = selectedCode,
                selectedPos = selectedPos,
                name = selectedCode?.let { itemByCode[it]?.name },
                sameColorCount = selectedCode?.let { counts[it] } ?: 0,
            )

            ColorTablePanel(
                project = project,
                selectedCode = selectedCode,
                expanded = tableExpanded,
                onToggle = { tableExpanded = !tableExpanded },
                onSelect = { selectedCode = it },
            )
        }
    }
}

@Composable
private fun InfoBar(
    selectedCode: String?,
    selectedPos: Pair<Int, Int>?,
    name: String?,
    sameColorCount: Int,
) {
    Divider()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selectedCode == null) {
            Text("点击格子查看色号与坐标", style = MaterialTheme.typography.bodyMedium)
        } else {
            val pos = selectedPos
            if (pos != null) {
                Text("第 ${pos.first + 1} 行 / 第 ${pos.second + 1} 列", style = MaterialTheme.typography.bodyMedium)
            }
            Text(selectedCode, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            if (!name.isNullOrBlank()) {
                Text(name, style = MaterialTheme.typography.bodyMedium)
            }
            Text("同色 $sameColorCount 粒", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ColorTablePanel(
    project: PindouProject,
    selectedCode: String?,
    expanded: Boolean,
    onToggle: () -> Unit,
    onSelect: (String) -> Unit,
) {
    Divider()
    Column(Modifier.fillMaxWidth().padding(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "用色 ${project.paletteUsed.size} 种",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(if (expanded) "收起" else "展开", style = MaterialTheme.typography.bodySmall)
        }

        if (expanded) {
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(project.paletteUsed, key = { it.code }) { item ->
                    val selected = item.code == selectedCode
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onSelect(item.code) },
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(item.rgb.toArgb()), RoundedCornerShape(6.dp))
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) Color.Black else Color(0xFF888888),
                                    shape = RoundedCornerShape(6.dp),
                                ),
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(item.code, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
