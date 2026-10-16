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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.pindou.app.PindouController
import com.pindou.app.R
import com.pindou.app.ml.CutoutPreset
import com.pindou.app.state.EditTool
import com.pindou.app.ui.NameDialog
import com.pindou.core.imaging.ImageType
import com.pindou.core.model.PindouProject
import kotlinx.coroutines.launch

private val GRID_SIZES = listOf(15, 29, 52, 58, 78, 104, 116, 128)
private val COLOR_COUNTS = listOf(4, 6, 8, 12, 16, 24, 32, 48, 64, 96, 128)
private const val AUTO_COLOR_COUNT = 16

/** Fixed height of the color-swatch row in [ColorTablePanel] so the bottom panel never resizes. */
private val SWATCH_ROW_HEIGHT = 60.dp

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
    var paramsExpanded by remember { mutableStateOf(false) }
    var showPattern by remember { mutableStateOf(true) }
    var peeking by remember { mutableStateOf(false) }
    val referenceImage = remember(project) { project?.referenceGrid?.toImage() }
    // A spread of current-palette colors for the bead-themed generating animation.
    val generatingColors = remember(controller.paletteName) {
        val items = controller.palette()?.items.orEmpty()
        if (items.isEmpty()) {
            emptyList()
        } else {
            val step = maxOf(1, items.size / 8)
            items.filterIndexed { i, _ -> i % step == 0 }.take(8).map { Color(it.rgb.toArgb()) }
        }
    }

    var pendingPalette by remember { mutableStateOf<String?>(null) }
    var pendingSize by remember { mutableStateOf<Int?>(null) }
    var pendingRegen by remember { mutableStateOf<(() -> Unit)?>(null) }
    var paletteOpen by remember { mutableStateOf(false) }
    var toolbarOffset by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var toolbarSize by remember { mutableStateOf(IntSize.Zero) }
    var peekOffset by remember { mutableStateOf(Offset.Zero) }
    var peekSize by remember { mutableStateOf(IntSize.Zero) }
    var confirmLeave by remember { mutableStateOf(false) }
    var pendingEmptyAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var nameContinuation by remember { mutableStateOf<((String) -> Unit)?>(null) }

    // Clamp a drag offset so a [size]-sized element anchored at [baseX]/[baseY]
    // stays fully inside the canvas viewport.
    fun clampToCanvas(next: Offset, size: IntSize, baseX: Float, baseY: Float): Offset {
        val maxX = (canvasSize.width - size.width).coerceAtLeast(0).toFloat()
        val maxY = (canvasSize.height - size.height).coerceAtLeast(0).toFloat()
        return Offset(
            (baseX + next.x).coerceIn(0f, maxX) - baseX,
            (baseY + next.y).coerceIn(0f, maxY) - baseY,
        )
    }

    // Keep the floating toolbar inside the canvas: the CenterEnd anchor plus the
    // drag offset is clamped so the whole dock stays within the viewport.
    val clampToolbarOffset: (Offset) -> Offset = { next ->
        clampToCanvas(
            next,
            toolbarSize,
            (canvasSize.width - toolbarSize.width).toFloat(),
            (canvasSize.height - toolbarSize.height) / 2f,
        )
    }

    // The peek button is anchored BottomStart.
    val clampPeekOffset: (Offset) -> Offset = { next ->
        clampToCanvas(next, peekSize, 0f, (canvasSize.height - peekSize.height).toFloat())
    }

    val referenceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri -> uri?.let { controller.pickReference(it) } }

    fun runOrConfirm(action: () -> Unit) {
        if (controller.edited) pendingRegen = action else action()
    }

    // Where a confirmed "leave" goes: back to the pattern view when leaving edit mode
    // of a normal project, otherwise home (free creation or leaving the view itself).
    var leaveDest by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun leaveWithConfirm(dest: () -> Unit) {
        if (controller.needsSave) {
            leaveDest = dest
            confirmLeave = true
        } else {
            dest()
        }
    }

    fun backFromEditor() {
        if (editMode && !controller.isFreeCreation) {
            leaveWithConfirm { controller.exitEdit() }
        } else {
            leaveWithConfirm { controller.goHome() }
        }
    }

    fun guardEmpty(then: () -> Unit) {
        if (controller.needsSave && controller.projectIsEmpty) pendingEmptyAction = then else then()
    }

    // Saves under the chosen name: prompt only the first time (when the project has no name yet).
    fun saveWithName(then: () -> Unit) {
        if (!controller.needsSave) {
            then()
            return
        }
        if (controller.hasName) {
            controller.saveProject(controller.suggestName()) { then() }
        } else {
            nameContinuation = { name -> controller.saveProject(name) { then() } }
        }
    }

    BackHandler { backFromEditor() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (editMode) "编辑" else "图纸")
                },
                navigationIcon = {
                    IconButton(onClick = { backFromEditor() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        enabled = project != null && (!editMode || controller.isFreeCreation),
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
                            onClick = { guardEmpty { saveWithName { } } },
                        ) { Text("保存") }
                        TextButton(onClick = {
                            guardEmpty {
                                saveWithName { controller.exitEdit() }
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
                                    saveWithName { controller.goExport() }
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
            if (controller.imported && project != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FileDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "来自外部的图纸文件",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            Text(
                                "尚未保存到本机，可存为我的图纸以便随时打开",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        FilledTonalButton(
                            enabled = !controller.busy,
                            onClick = {
                                if (controller.hasName) {
                                    controller.importToDocuments(controller.suggestName())
                                } else {
                                    nameContinuation = { name -> controller.importToDocuments(name) }
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        ) { Text("保存") }
                    }
                }
            }

            if (paramsExpanded && project != null) {
                ParamsPanel(
                    controller = controller,
                    onImageType = { type -> runOrConfirm { controller.updateImageType(type) } },
                    onPalette = { name -> pendingPalette = name },
                    onColorStrategy = { id -> runOrConfirm { controller.updateColorStrategy(id) } },
                    onSize = { size ->
                        if (controller.hasSource && !controller.edited) {
                            controller.resizeGridRegenerate(size)
                        } else {
                            pendingSize = size
                        }
                    },
                    onColorCount = { auto, count -> runOrConfirm { controller.updateColorCountSelection(auto, count) } },
                    onCutout = { enabled -> runOrConfirm { controller.updateCutout(enabled) } },
                    onCutoutPreset = { preset -> runOrConfirm { controller.updateCutoutPreset(preset) } },
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
                        dragDraw = controller.editTool == EditTool.PAINT ||
                            (controller.editTool == EditTool.ERASE && !controller.eraseConnected),
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
                        if (controller.busy) {
                            GeneratingIndicator(colors = generatingColors)
                        } else {
                            Text(
                                "请选择参数生成图纸",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                if (controller.busy && project != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x66000000)),
                        contentAlignment = Alignment.Center,
                    ) {
                        GeneratingIndicator(colors = generatingColors, textColor = Color.White)
                    }
                }

                if (editMode && project != null) {
                    FloatingToolbar(
                        selected = controller.editTool,
                        eraseConnected = controller.eraseConnected,
                        onSelect = controller::selectEditTool,
                        onToggleEraseConnected = {
                            controller.selectEditTool(EditTool.ERASE)
                            controller.toggleEraseConnected()
                        },
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

                if (referenceImage != null && project != null) {
                    FloatingPeekButton(
                        onPeek = { peeking = it },
                        onMove = { delta -> peekOffset = clampPeekOffset(peekOffset + delta) },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset {
                                val clamped = clampPeekOffset(peekOffset)
                                IntOffset(clamped.x.toInt(), clamped.y.toInt())
                            }
                            .onSizeChanged { peekSize = it }
                            .alpha(if (peekSize == IntSize.Zero) 0f else 1f),
                    )
                }
            }

            if (project != null) {
                val itemByCode = remember(project) { project.paletteSource.items.associateBy { it.code } }
                val counts = remember(project) { project.grid.cells.filterNotNull().groupingBy { it }.eachCount() }
                InfoBar(
                    editMode = editMode,
                    toolLabel = controller.editToolLabel,
                    brushCode = controller.brushCode,
                    selectedCode = selectedCode,
                    selectedPos = selectedPos,
                    name = selectedCode?.let { itemByCode[it]?.name },
                    sameColorCount = selectedCode?.let { counts[it] } ?: 0,
                )
                ColorTablePanel(
                    project = project,
                    selectedCode = if (editMode) controller.brushCode else selectedCode,
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
            onDismissRequest = {
                confirmLeave = false
                leaveDest = null
            },
            title = { Text("未保存") },
            text = { Text("当前图纸尚未保存，不保存将放弃本次更改，是否保存？") },
            confirmButton = {
                TextButton(onClick = {
                    confirmLeave = false
                    val dest = leaveDest
                    leaveDest = null
                    guardEmpty { saveWithName { dest?.invoke() } }
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = {
                    confirmLeave = false
                    val dest = leaveDest
                    leaveDest = null
                    controller.discardEdits()
                    dest?.invoke()
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

    nameContinuation?.let { action ->
        NameDialog(
            initial = controller.suggestName(),
            onConfirm = { name ->
                nameContinuation = null
                action(name)
            },
            onDismiss = { nameContinuation = null },
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
    EditTool.REPLACE -> Icons.Filled.FindReplace
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
    eraseConnected: Boolean,
    onSelect: (EditTool) -> Unit,
    onToggleEraseConnected: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onMove: (Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val drawTools = listOf(EditTool.PAINT, EditTool.ERASE, EditTool.EYEDROPPER, EditTool.BUCKET, EditTool.REPLACE)
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
                if (tool == EditTool.ERASE) {
                    ToolButton(
                        tool = tool,
                        selected = selected == tool,
                        accent = eraseConnected,
                        onDoubleTap = onToggleEraseConnected,
                        onClick = { onSelect(tool) },
                    )
                } else {
                    ToolButton(tool, selected == tool) { onSelect(tool) }
                }
            }
        }
    }
}

@Composable
private fun ToolButton(
    tool: EditTool,
    selected: Boolean,
    accent: Boolean = false,
    onDoubleTap: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    val doubleTap = onDoubleTap
    val background = when {
        accent -> MaterialTheme.colorScheme.primary
        selected -> MaterialTheme.colorScheme.secondaryContainer
        else -> Color.Transparent
    }
    val tint = when {
        accent -> MaterialTheme.colorScheme.onPrimary
        selected -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(50))
            .background(background, RoundedCornerShape(50))
            .then(
                if (doubleTap == null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onClick() },
                            onDoubleTap = { doubleTap() },
                        )
                    }
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            tool.icon(),
            contentDescription = if (accent) "${tool.label}·区域擦除" else tool.label,
            modifier = Modifier.size(20.dp),
            tint = tint,
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
    onColorStrategy: (String) -> Unit,
    onSize: (Int) -> Unit,
    onColorCount: (Boolean, Int) -> Unit,
    onCutout: (Boolean) -> Unit,
    onCutoutPreset: (CutoutPreset) -> Unit,
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
                enabled = hasSource && !controller.busy,
                modifier = Modifier.weight(1f),
                onSelect = { onImageType(ImageType.values()[it]) },
            )
            FieldDropdown(
                label = "拼豆色板",
                selected = controller.paletteMetas
                    .firstOrNull { it.name == controller.paletteName }
                    ?.displayName ?: "请选择",
                options = controller.paletteMetas.map { "${it.displayName}（${it.colorCount} 色）" },
                enabled = !controller.busy,
                modifier = Modifier.weight(1f),
                onSelect = { onPalette(controller.paletteMetas[it].name) },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FieldDropdown(
                label = "图纸尺寸",
                selected = "${controller.gridWidth}×${controller.gridHeight}",
                options = GRID_SIZES.map { "${it}×${it}" },
                enabled = !controller.busy,
                modifier = Modifier.weight(1f),
                onSelect = { onSize(GRID_SIZES[it]) },
            )
            FieldDropdown(
                label = "用色数量",
                selected = if (controller.colorCountAuto) "自动" else controller.colorCountTarget.toString(),
                options = listOf("自动") + COLOR_COUNTS.map { it.toString() },
                enabled = hasSource && !controller.busy,
                modifier = Modifier.weight(1f),
                onSelect = {
                    val auto = it == 0
                    val count = if (auto) AUTO_COLOR_COUNT else COLOR_COUNTS[it - 1]
                    onColorCount(auto, count)
                },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FieldDropdown(
                label = "取色方式",
                selected = controller.colorStrategies
                    .firstOrNull { it.id == controller.colorStrategy }
                    ?.displayName ?: "参考图直取",
                options = controller.colorStrategies.map { it.displayName },
                enabled = hasSource && !controller.busy,
                modifier = Modifier.weight(1f),
                onSelect = { onColorStrategy(controller.colorStrategies[it].id) },
            )
        }
        if (hasSource) {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("自动抠出主体", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        if (!controller.cutoutAvailable) {
                            controller.cutoutMessage ?: "当前设备暂不支持该功能"
                        } else {
                            "只保留画面主体，背景不参与拼豆；离线在本机处理"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = controller.cutoutEnabled,
                    enabled = controller.cutoutAvailable && !controller.busy,
                    onCheckedChange = onCutout,
                )
            }
            if (controller.cutoutEnabled && controller.cutoutAvailable) {
                Spacer(Modifier.height(4.dp))
                FieldDropdown(
                    label = "抠图微调",
                    selected = controller.cutoutPreset.label,
                    options = controller.cutoutPresets.map { it.label },
                    enabled = !controller.busy,
                    onSelect = { onCutoutPreset(controller.cutoutPresets[it]) },
                )
            }
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
                    if (brushCode != null) "当前色号 $brushCode" else "请在下方选择珠子颜色",
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
    }
}

@Composable
private fun FloatingPeekButton(
    onPeek: (Boolean) -> Unit,
    onMove: (Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(54.dp)
            .shadow(2.dp, CircleShape)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Color(0xCCCCCCCC), CircleShape)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    onPeek(true)
                    var dragging = false
                    var total = Offset.Zero
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        if (!change.pressed) break
                        val delta = change.positionChange()
                        change.consume()
                        total += delta
                        if (!dragging && total.getDistance() > viewConfiguration.touchSlop) dragging = true
                        if (dragging) onMove(delta)
                    }
                    onPeek(false)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "按住\n看参考图",
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ColorTablePanel(
    project: PindouProject,
    selectedCode: String?,
    onSelect: (String) -> Unit,
) {
    Divider()
    Column(Modifier.fillMaxWidth().padding(12.dp)) {
        Text(
            "${project.grid.width}×${project.grid.height} · ${project.paletteSource.displayName} · 用色 ${project.paletteUsed.size} 种",
            style = MaterialTheme.typography.titleSmall,
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SWATCH_ROW_HEIGHT),
            contentAlignment = Alignment.Center,
        ) {
            if (project.paletteUsed.isEmpty()) {
                Text(
                    "在图纸上绘制后，用到的颜色会显示在这里",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
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
}
