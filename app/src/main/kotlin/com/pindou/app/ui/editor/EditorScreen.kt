package com.pindou.app.ui.editor

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.pindou.app.PindouController
import com.pindou.app.R
import com.pindou.app.state.EditTool
import com.pindou.core.imaging.ImageType
import com.pindou.core.model.PindouProject
import kotlinx.coroutines.launch

private val GRID_SIZES = listOf(29, 52, 58, 78, 104, 116, 128)
private val COLOR_COUNTS = listOf(4, 6, 8, 12, 16, 24, 32, 48, 64, 96, 128)
private const val AUTO_COLOR_COUNT = 16

/**
 * The pattern screen: a view mode (parameter panel + tap-to-highlight Form 2
 * grid, reference compare) and an edit mode (tool bar + direct grid drawing)
 * over the same [PatternGrid]. Editing mutations go through [PindouController]
 * and are reflected immediately.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(controller: PindouController) {
    val project = controller.project
    val grid = project?.grid
    val editMode = controller.editMode

    var selectedCode by remember(project) { mutableStateOf<String?>(null) }
    var selectedPos by remember(project) { mutableStateOf<Pair<Int, Int>?>(null) }
    var tableExpanded by remember { mutableStateOf(true) }
    var paramsExpanded by remember { mutableStateOf(false) }
    var showPattern by remember { mutableStateOf(true) }
    var peeking by remember { mutableStateOf(false) }
    val referenceImage = remember(project) { project?.referenceGrid?.toImage() }

    var pendingPalette by remember { mutableStateOf<String?>(null) }
    var pendingSize by remember { mutableStateOf<Int?>(null) }
    var pendingRegen by remember { mutableStateOf<(() -> Unit)?>(null) }
    var paletteOpen by remember { mutableStateOf(false) }
    var toolbarOffset by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var toolbarSize by remember { mutableStateOf(IntSize.Zero) }
    var confirmLeave by remember { mutableStateOf(false) }
    var pendingEmptyAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Keep the floating toolbar inside the canvas: the CenterEnd anchor plus the
    // drag offset is clamped so the whole dock stays within the viewport.
    val clampToolbarOffset: (Offset) -> Offset = { next ->
        val maxX = (canvasSize.width - toolbarSize.width).coerceAtLeast(0).toFloat()
        val maxY = (canvasSize.height - toolbarSize.height).coerceAtLeast(0).toFloat()
        val baseX = (canvasSize.width - toolbarSize.width).toFloat()
        val baseY = (canvasSize.height - toolbarSize.height) / 2f
        Offset(
            (baseX + next.x).coerceIn(0f, maxX) - baseX,
            (baseY + next.y).coerceIn(0f, maxY) - baseY,
        )
    }

    val referenceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri -> uri?.let { controller.pickReference(it) } }

    fun runOrConfirm(action: () -> Unit) {
        if (controller.edited) pendingRegen = action else action()
    }

    fun leaveToHome() {
        if (controller.needsSave) confirmLeave = true else controller.goHome()
    }

    fun guardEmpty(then: () -> Unit) {
        if (controller.needsSave && controller.projectIsEmpty) pendingEmptyAction = then else then()
    }

    BackHandler {
        if (editMode && !controller.isFreeCreation) controller.exitEdit() else leaveToHome()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val label = if (editMode) "编辑图纸" else "生成图纸"
                    val dims = grid?.let { "${it.width}×${it.height}" }
                    Text(if (dims != null) "$label $dims" else label)
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (editMode && !controller.isFreeCreation) {
                                controller.exitEdit()
                            } else {
                                leaveToHome()
                            }
                        },
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        enabled = project != null,
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
                    if (editMode) {
                        TextButton(
                            enabled = project != null && !controller.busy,
                            onClick = { guardEmpty { controller.saveProject() } },
                        ) { Text("保存") }
                        TextButton(onClick = {
                            guardEmpty {
                                controller.saveProject()
                                controller.exitEdit()
                            }
                        }) { Text("完成") }
                    } else {
                        TextButton(enabled = project != null, onClick = { controller.enterEdit() }) {
                            Text("编辑")
                        }
                        TextButton(
                            enabled = project != null && !controller.busy,
                            onClick = {
                                guardEmpty {
                                    controller.saveProject()
                                    controller.goExport()
                                }
                            },
                        ) { Text("导出") }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (paramsExpanded && project != null) {
                ParamsPanel(
                    controller = controller,
                    onImageType = { type -> runOrConfirm { controller.updateImageType(type) } },
                    onPalette = { name -> pendingPalette = name },
                    onSize = { size ->
                        if (controller.hasSource && !controller.edited) {
                            controller.resizeGridRegenerate(size)
                        } else {
                            pendingSize = size
                        }
                    },
                    onColorCount = { auto, count -> runOrConfirm { controller.updateColorCountSelection(auto, count) } },
                )
            }

            if (editMode && project != null) {
                EditActionBar(
                    controller = controller,
                    referenceAvailable = referenceImage != null,
                    onOpenPalette = { paletteOpen = true },
                    onLoadReference = { referenceLauncher.launch("image/*") },
                    onToggleReference = { controller.setShowReference(!controller.showReference) },
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clipToBounds()
                    .background(Color(0xFFEEEEEE))
                    .onSizeChanged { canvasSize = it },
            ) {
                if (project != null) {
                    val g = project.grid
                    PatternGrid(
                        project = project,
                        selectedCode = if (editMode) null else selectedCode,
                        selectedPos = if (editMode) null else selectedPos,
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
                        showPattern = if (peeking) false else if (editMode) true else showPattern,
                        showReference = if (peeking) true else controller.showReference,
                        editMode = editMode,
                        moveMode = controller.editTool == EditTool.MOVE,
                        dragDraw = controller.editTool == EditTool.PAINT || controller.editTool == EditTool.ERASE,
                        onEdit = { row, col -> controller.applyEdit(row, col, peeking) },
                        onEditStart = { controller.beginStroke() },
                        onEditEnd = { controller.endStroke() },
                        highlightCell = controller.editCursor,
                        onEditCursor = { row, col, active ->
                            if (active) controller.updateEditCursor(row, col) else controller.clearEditCursor()
                        },
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

                if (editMode && project != null) {
                    FloatingToolbar(
                        selected = controller.editTool,
                        onSelect = controller::selectEditTool,
                        canUndo = controller.canUndo,
                        canRedo = controller.canRedo,
                        onUndo = { controller.undoEdit() },
                        onRedo = { controller.redoEdit() },
                        onMove = { delta -> toolbarOffset = clampToolbarOffset(toolbarOffset + delta) },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .offset {
                                val clamped = clampToolbarOffset(toolbarOffset)
                                IntOffset(clamped.x.toInt(), clamped.y.toInt())
                            }
                            .onSizeChanged { toolbarSize = it }
                            .alpha(if (toolbarSize == IntSize.Zero) 0f else 1f),
                    )
                }
            }

            if (project != null) {
                val itemByCode = remember(project) { project.paletteSource.items.associateBy { it.code } }
                val counts = remember(project) { project.grid.cells.filterNotNull().groupingBy { it }.eachCount() }
                InfoBar(
                    editMode = editMode,
                    toolLabel = controller.editTool.label,
                    brushCode = controller.brushCode,
                    selectedCode = selectedCode,
                    selectedPos = selectedPos,
                    name = selectedCode?.let { itemByCode[it]?.name },
                    sameColorCount = selectedCode?.let { counts[it] } ?: 0,
                    referenceAvailable = referenceImage != null,
                    showReference = controller.showReference,
                    onToggleReference = {
                        if (!editMode && controller.showReference && !showPattern) showPattern = true
                        controller.setShowReference(!controller.showReference)
                    },
                    showPattern = showPattern,
                    onTogglePattern = {
                        if (showPattern && !controller.showReference) controller.setShowReference(true)
                        showPattern = !showPattern
                    },
                    onPeek = { peeking = it },
                )
                ColorTablePanel(
                    project = project,
                    selectedCode = if (editMode) controller.brushCode else selectedCode,
                    expanded = tableExpanded,
                    onToggle = { tableExpanded = !tableExpanded },
                    onSelect = { if (editMode) controller.selectBrush(it) else selectedCode = it },
                )
            }
        }
    }

    pendingPalette?.let { name ->
        AlertDialog(
            onDismissRequest = { pendingPalette = null },
            title = { Text("更换色板") },
            text = { Text("将把当前全部珠子按最近颜色重映射到新色板，是否继续？") },
            confirmButton = {
                TextButton(onClick = { controller.remapPalette(name); pendingPalette = null }) { Text("继续") }
            },
            dismissButton = { TextButton(onClick = { pendingPalette = null }) { Text("取消") } },
        )
    }

    pendingSize?.let { size ->
        val regenerate = controller.hasSource
        AlertDialog(
            onDismissRequest = { pendingSize = null },
            title = { Text(if (regenerate) "重新生成" else "更改尺寸") },
            text = {
                Text(
                    if (regenerate) {
                        "重新生成（新尺寸）会丢弃已手动编辑的内容，是否继续？"
                    } else {
                        "更改尺寸将清空当前图纸（无源图项目），是否继续？"
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (regenerate) controller.resizeGridRegenerate(size) else controller.clearGrid(size)
                    pendingSize = null
                }) { Text("继续") }
            },
            dismissButton = { TextButton(onClick = { pendingSize = null }) { Text("取消") } },
        )
    }

    pendingRegen?.let { action ->
        AlertDialog(
            onDismissRequest = { pendingRegen = null },
            title = { Text("重新生成") },
            text = { Text("重新生成会丢弃已手动编辑的内容，是否继续？") },
            confirmButton = {
                TextButton(onClick = { action(); pendingRegen = null }) { Text("继续") }
            },
            dismissButton = { TextButton(onClick = { pendingRegen = null }) { Text("取消") } },
        )
    }

    if (paletteOpen) {
        PalettePickerDialog(controller) { paletteOpen = false }
    }

    if (confirmLeave) {
        AlertDialog(
            onDismissRequest = { confirmLeave = false },
            title = { Text("未保存") },
            text = { Text("当前图纸尚未保存，退出后更改将丢失，是否保存？") },
            confirmButton = {
                TextButton(onClick = {
                    confirmLeave = false
                    guardEmpty { controller.saveProject { controller.goHome() } }
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = {
                    confirmLeave = false
                    controller.goHome()
                }) { Text("不保存") }
            },
        )
    }

    pendingEmptyAction?.let { action ->
        AlertDialog(
            onDismissRequest = { pendingEmptyAction = null },
            title = { Text("图纸为空") },
            text = { Text("当前图纸没有任何珠子，为空。是否仍要保存/导出？") },
            confirmButton = {
                TextButton(onClick = {
                    pendingEmptyAction = null
                    action()
                }) { Text("继续") }
            },
            dismissButton = { TextButton(onClick = { pendingEmptyAction = null }) { Text("取消") } },
        )
    }
}

/**
 * A full-palette color picker grouped by bead series (the leading letters of a
 * color code, e.g. `A`, `C`, `M`). Tapping a swatch sets the brush color.
 */
@Composable
private fun PalettePickerDialog(controller: PindouController, onDismiss: () -> Unit) {
    val palette = controller.palette() ?: return
    val items = palette.items
    val series = remember(palette) {
        items.map { it.code.takeWhile { ch -> ch.isLetter() } }.distinct()
    }
    val current = controller.brushCode
    val usedCodes = remember(controller.project) {
        controller.project?.paletteUsed?.map { it.code }?.toSet().orEmpty()
    }
    val colorBySeries = remember(items) {
        items.groupBy { it.code.takeWhile { ch -> ch.isLetter() } }
            .mapValues { (_, list) ->
                val r = list.sumOf { it.r } / list.size
                val g = list.sumOf { it.g } / list.size
                val b = list.sumOf { it.b } / list.size
                Color(r, g, b)
            }
    }
    var selectedSeries by remember(series) {
        val cur = current?.takeWhile { it.isLetter() }
        mutableStateOf(if (cur != null && cur in series) cur else series.firstOrNull().orEmpty())
    }
    val visible = remember(items, selectedSeries) { items.filter { it.code.startsWith(selectedSeries) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择颜色") },
        text = {
            Column(Modifier.fillMaxWidth()) {
                val scope = rememberCoroutineScope()
                val listState = rememberLazyListState()
                val canScrollLeft by remember {
                    derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
                }
                val canScrollRight by remember {
                    derivedStateOf { (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) < series.lastIndex }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { scope.launch { listState.animateScrollBy(-560f) } },
                        enabled = canScrollLeft,
                    ) {
                        Icon(
                            Icons.Filled.KeyboardArrowLeft,
                            contentDescription = "上一组色系",
                            modifier = Modifier.size(32.dp),
                        )
                    }
                    LazyRow(
                        state = listState,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        items(series) { s ->
                            val on = s == selectedSeries
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (on) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                                    .clickable { selectedSeries = s }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(colorBySeries[s] ?: Color(0xFFBBBBBB), RoundedCornerShape(4.dp)),
                                )
                                Text(s, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    IconButton(
                        onClick = { scope.launch { listState.animateScrollBy(560f) } },
                        enabled = canScrollRight,
                    ) {
                        Icon(
                            Icons.Filled.KeyboardArrowRight,
                            contentDescription = "下一组色系",
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    gridItems(visible, key = { it.code }) { item ->
                        val selected = item.code == current
                        val used = item.code in usedCodes
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                controller.selectBrush(item.code)
                                onDismiss()
                            },
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .background(Color(item.rgb.toArgb()), RoundedCornerShape(6.dp))
                                    .border(
                                        width = if (selected) 3.dp else if (used) 2.dp else 1.dp,
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.primary
                                        } else if (used) {
                                            Color(0xFF666666)
                                        } else {
                                            Color(0xFFBBBBBB)
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                    ),
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(item.code, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } },
    )
}

/** Maps an editing tool to the icon shown in the tool bar. */
@Composable
private fun EditTool.icon(): ImageVector = when (this) {
    EditTool.PAINT -> Icons.Filled.Brush
    EditTool.ERASE -> ImageVector.vectorResource(R.drawable.ic_eraser)
    EditTool.EYEDROPPER -> Icons.Filled.Colorize
    EditTool.BUCKET -> Icons.Filled.FormatColorFill
    EditTool.MOVE -> Icons.Filled.PanTool
}

/**
 * The bottom action bar (in edit mode): the reference-image entry (import, plus a
 * show toggle) and the current-color swatch that opens the full series picker.
 * All tools and history actions live in the draggable [FloatingToolbar].
 */
@Composable
private fun EditActionBar(
    controller: PindouController,
    referenceAvailable: Boolean,
    onOpenPalette: () -> Unit,
    onLoadReference: () -> Unit,
    onToggleReference: () -> Unit,
) {
    Divider()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (!controller.hasSource) {
            TextButton(onClick = onLoadReference) { Text("导入参考图") }
        }
        if (referenceAvailable) {
            val on = controller.showReference
            TextButton(
                onClick = onToggleReference,
                modifier = Modifier.background(
                    if (on) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                    RoundedCornerShape(20.dp),
                ),
            ) {
                Text(if (on) "显示参考图 ✓" else "显示参考图")
            }
        }
        Spacer(Modifier.weight(1f))
        val brush = controller.brushCode
        val rgb = brush?.let { controller.palette()?.item(it)?.rgb }
        if (brush != null) {
            Text(brush, style = MaterialTheme.typography.labelSmall)
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Color(rgb?.toArgb() ?: 0xFFEEEEEE.toInt()), RoundedCornerShape(6.dp))
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                .clickable { onOpenPalette() },
            contentAlignment = Alignment.Center,
        ) {
            if (brush == null) {
                Icon(Icons.Filled.ArrowDropDown, contentDescription = "选择颜色", modifier = Modifier.size(20.dp))
            }
        }
    }
    Divider()
}

/**
 * The floating tool dock. It sits on top of the canvas and can be dragged to
 * any position: a transparent gesture catcher fills the dock and consumes
 * touches (so the grid below never starts a stroke), while the tool buttons on
 * top still receive taps. A grip bar at the top makes the drag target obvious.
 */
@Composable
private fun FloatingToolbar(
    selected: EditTool,
    onSelect: (EditTool) -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onMove: (Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val drawTools = listOf(EditTool.PAINT, EditTool.ERASE, EditTool.EYEDROPPER, EditTool.BUCKET)
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .shadow(2.dp, shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Color(0xCCCCCCCC), shape),
    ) {
        Box(
            Modifier
                .matchParentSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            if (!change.pressed) break
                            if (change.position != change.previousPosition) {
                                onMove(change.position - change.previousPosition)
                                change.consume()
                            }
                        }
                    }
                },
        )
        Column(
            modifier = Modifier.padding(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Drag grip: a non-clickable area so touches fall through to the
            // catcher below and drag the dock without hitting a tool button.
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 24.dp, height = 3.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50)),
                )
            }
            ToolButton(EditTool.MOVE, selected == EditTool.MOVE) { onSelect(EditTool.MOVE) }
            HistoryButton(Icons.Filled.Undo, "撤销", canUndo, onUndo)
            HistoryButton(Icons.Filled.Redo, "重做", canRedo, onRedo)
            Divider(Modifier.width(24.dp).padding(vertical = 2.dp))
            drawTools.forEach { tool ->
                ToolButton(tool, selected == tool) { onSelect(tool) }
            }
        }
    }
}

@Composable
private fun ToolButton(tool: EditTool, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                RoundedCornerShape(50),
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            tool.icon(),
            contentDescription = tool.label,
            modifier = Modifier.size(20.dp),
            tint = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
            else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun HistoryButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(50))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.38f),
        )
    }
}

@Composable
private fun ParamsPanel(
    controller: PindouController,
    onImageType: (ImageType) -> Unit,
    onPalette: (String) -> Unit,
    onSize: (Int) -> Unit,
    onColorCount: (Boolean, Int) -> Unit,
) {
    val hasSource = controller.workingImage != null
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
                enabled = hasSource,
                modifier = Modifier.weight(1f),
                onSelect = { onImageType(ImageType.values()[it]) },
            )
            FieldDropdown(
                label = "拼豆色板",
                selected = controller.paletteMetas
                    .firstOrNull { it.name == controller.paletteName }
                    ?.displayName ?: "请选择",
                options = controller.paletteMetas.map { "${it.displayName}（${it.colorCount} 色）" },
                enabled = true,
                modifier = Modifier.weight(1f),
                onSelect = { onPalette(controller.paletteMetas[it].name) },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FieldDropdown(
                label = "图纸尺寸",
                selected = "${controller.gridWidth}×${controller.gridHeight}",
                options = GRID_SIZES.map { "${it}×${it}" },
                enabled = true,
                modifier = Modifier.weight(1f),
                onSelect = { onSize(GRID_SIZES[it]) },
            )
            FieldDropdown(
                label = "用色数量",
                selected = if (controller.colorCountAuto) "自动" else controller.colorCountTarget.toString(),
                options = listOf("自动") + COLOR_COUNTS.map { it.toString() },
                enabled = hasSource,
                modifier = Modifier.weight(1f),
                onSelect = {
                    val auto = it == 0
                    val count = if (auto) AUTO_COLOR_COUNT else COLOR_COUNTS[it - 1]
                    onColorCount(auto, count)
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
    editMode: Boolean,
    toolLabel: String,
    brushCode: String?,
    selectedCode: String?,
    selectedPos: Pair<Int, Int>?,
    name: String?,
    sameColorCount: Int,
    referenceAvailable: Boolean,
    showReference: Boolean,
    onToggleReference: () -> Unit,
    showPattern: Boolean,
    onTogglePattern: () -> Unit,
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
            if (editMode) {
                Text("工具：$toolLabel", style = MaterialTheme.typography.bodyMedium)
                Text(
                    if (brushCode != null) "当前珠子 $brushCode" else "请在下方选择珠子颜色",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
            } else if (selectedCode == null) {
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

        if (referenceAvailable || !editMode) {
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!editMode) {
                    CompareChip(label = "图纸", on = showPattern, onClick = onTogglePattern)
                    CompareChip(label = "参考图", on = showReference, onClick = onToggleReference)
                }
                if (referenceAvailable) {
                    PeekChip(onPeek = onPeek)
                }
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
