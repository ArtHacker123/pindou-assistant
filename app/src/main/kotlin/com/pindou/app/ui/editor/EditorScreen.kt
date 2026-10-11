package com.pindou.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pindou.app.PindouController
import com.pindou.core.model.PindouProject
import com.pindou.core.processor.ImageType

private val GRID_SIZES = listOf(29, 52, 58, 78, 104, 116, 128)
private val COLOR_COUNTS = listOf(4, 6, 8, 12, 16, 24, 32, 48, 64, 96, 128)
private const val AUTO_COLOR_COUNT = 16

/**
 * The merged parameter + pattern screen: a collapsible parameter panel on top of
 * the interactive Form 2 grid. Any parameter change regenerates the pattern in
 * place via [PindouController].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(controller: PindouController) {
    val project = controller.project
    val grid = project?.grid

    var selectedCode by remember(project) { mutableStateOf<String?>(null) }
    var selectedPos by remember(project) { mutableStateOf<Pair<Int, Int>?>(null) }
    var tableExpanded by remember { mutableStateOf(true) }
    var paramsExpanded by remember { mutableStateOf(false) }
    var showPattern by remember { mutableStateOf(true) }
    var showReference by remember { mutableStateOf(false) }
    var peeking by remember { mutableStateOf(false) }
    val referenceImage = remember(project) { project?.referenceGrid?.toImage() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(grid?.let { "生成图纸 ${it.width}×${it.height}" } ?: "生成图纸") },
                navigationIcon = {
                    IconButton(onClick = { controller.goHome() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        enabled = controller.workingImage != null,
                        onClick = { paramsExpanded = !paramsExpanded },
                        modifier = Modifier.background(
                            if (paramsExpanded) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                            RoundedCornerShape(20.dp),
                        ),
                    ) {
                        Text("设置")
                        Icon(
                            imageVector = if (paramsExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                            contentDescription = if (paramsExpanded) "收起设置" else "展开设置",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    TextButton(
                        enabled = project != null && !controller.busy,
                        onClick = { controller.goExport() },
                    ) { Text("导出") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (paramsExpanded && controller.workingImage != null) {
                ParamsPanel(
                    controller = controller,
                    editable = controller.workingImage != null,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clipToBounds()
                    .background(Color(0xFFEEEEEE)),
            ) {
                if (project != null) {
                    val g = project.grid
                    PatternGrid(
                        project = project,
                        selectedCode = selectedCode,
                        selectedPos = selectedPos,
                        onCellTap = { row, col ->
                            if (row in 0 until g.height && col in 0 until g.width) {
                                val code = g[row, col]
                                if (code != null) {
                                    if (selectedCode != null && code == selectedCode) {
                                        selectedPos = null
                                        selectedCode = null
                                    } else {
                                        selectedPos = row to col
                                        selectedCode = code
                                    }
                                }
                            } else {
                                selectedPos = null
                                selectedCode = null
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        reference = referenceImage,
                        showPattern = if (peeking) false else showPattern,
                        showReference = if (peeking) true else showReference,
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (controller.busy) "生成中…" else "请选择参数生成图纸",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                if (controller.busy) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x00000000)),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (project != null) {
                val itemByCode = remember(project) { project.paletteSource.items.associateBy { it.code } }
                val counts = remember(project) { project.grid.cells.filterNotNull().groupingBy { it }.eachCount() }
                InfoBar(
                    selectedCode = selectedCode,
                    selectedPos = selectedPos,
                    name = selectedCode?.let { itemByCode[it]?.name },
                    sameColorCount = selectedCode?.let { counts[it] } ?: 0,
                    referenceAvailable = referenceImage != null,
                    showPattern = showPattern,
                    showReference = showReference,
                    onTogglePattern = {
                        if (showPattern && !showReference) showReference = true
                        showPattern = !showPattern
                    },
                    onToggleReference = {
                        if (showReference && !showPattern) showPattern = true
                        showReference = !showReference
                    },
                    onPeek = { peeking = it },
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
}

@Composable
private fun ParamsPanel(
    controller: PindouController,
    editable: Boolean,
) {
    Divider()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FieldDropdown(
                label = "图像类型",
                selected = controller.imageType.displayName,
                options = ImageType.values().map { it.displayName },
                enabled = editable,
                modifier = Modifier.weight(1f),
                onSelect = { controller.updateImageType(ImageType.values()[it]) },
            )
            FieldDropdown(
                label = "拼豆色板",
                selected = controller.paletteMetas
                    .firstOrNull { it.name == controller.paletteName }
                    ?.displayName ?: "请选择",
                options = controller.paletteMetas.map { "${it.displayName}（${it.colorCount} 色）" },
                enabled = editable,
                modifier = Modifier.weight(1f),
                onSelect = { controller.updatePalette(controller.paletteMetas[it].name) },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FieldDropdown(
                label = "图纸尺寸",
                selected = "${controller.gridWidth}×${controller.gridHeight}",
                options = GRID_SIZES.map { "${it}×${it}" },
                enabled = editable,
                modifier = Modifier.weight(1f),
                onSelect = { controller.updateGridSize(GRID_SIZES[it]) },
            )
            FieldDropdown(
                label = "用色数量",
                selected = if (controller.colorCountAuto) "自动" else controller.colorCountTarget.toString(),
                options = listOf("自动") + COLOR_COUNTS.map { it.toString() },
                enabled = editable,
                modifier = Modifier.weight(1f),
                onSelect = {
                    val auto = it == 0
                    val count = if (auto) AUTO_COLOR_COUNT else COLOR_COUNTS[it - 1]
                    controller.updateColorCountSelection(auto, count)
                },
            )
        }
    }
}

@Composable
private fun FieldDropdown(
    label: String,
    selected: String,
    options: List<String>,
    enabled: Boolean,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    Column(modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(2.dp))
        OutlinedButton(
            onClick = { open = true },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                selected,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(2.dp))
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "下拉", modifier = Modifier.size(20.dp))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(index)
                        open = false
                    },
                )
            }
        }
    }
}

@Composable
private fun InfoBar(
    selectedCode: String?,
    selectedPos: Pair<Int, Int>?,
    name: String?,
    sameColorCount: Int,
    referenceAvailable: Boolean,
    showPattern: Boolean,
    showReference: Boolean,
    onTogglePattern: () -> Unit,
    onToggleReference: () -> Unit,
    onPeek: (Boolean) -> Unit,
) {
    Divider()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selectedCode == null) {
                Text("点击格子查看色号与坐标，点高亮格可取消选择", style = MaterialTheme.typography.bodyMedium)
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

        if (referenceAvailable) {
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CompareChip(label = "图纸", on = showPattern, onClick = onTogglePattern)
                CompareChip(label = "缩略图", on = showReference, onClick = onToggleReference)
                PeekChip(onPeek = onPeek)
            }
        }
    }
}

@Composable
private fun CompareChip(label: String, on: Boolean, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.background(
            if (on) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
            RoundedCornerShape(20.dp),
        ),
    ) {
        Text(if (on) "$label ✓" else label)
    }
}

/** A press-and-hold control that temporarily shows the reference only (A/B peek). */
@Composable
private fun PeekChip(onPeek: (Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    onPeek(true)
                    waitForUpOrCancellation()
                    onPeek(false)
                }
            }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("按住看原图", style = MaterialTheme.typography.labelLarge)
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