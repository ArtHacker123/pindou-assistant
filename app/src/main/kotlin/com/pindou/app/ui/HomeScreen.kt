package com.pindou.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pindou.app.PindouController
import com.pindou.app.data.ExportManager
import com.pindou.app.data.PatternEntry
import com.pindou.app.data.PindouTemplate
import com.pindou.app.data.TEMPLATES

private val FREE_SIZES = listOf(29, 52, 58, 78, 104, 116, 128)
private const val FREE_DEFAULT_SIZE = 52
private const val RECENT_LIMIT = 6

private val CARD_BLUE = Color(0xFFEAF1FF)
private val CARD_ORANGE = Color(0xFFFFF1E0)
private val CARD_GREEN = Color(0xFFEAF7EC)

private val LOGO_PALETTE = mapOf(
    'R' to Color(0xFFE53935),
    'O' to Color(0xFFFB8C00),
    'Y' to Color(0xFFFDD835),
    'G' to Color(0xFF43A047),
    'B' to Color(0xFF1E88E5),
    'P' to Color(0xFF8E24AA),
)
private val LOGO_SPRITE = listOf(
    "...............",
    "...RRR...RRR...",
    "..RRRRR.RRRRR..",
    ".OOOOOOOOOOOOO.",
    ".OOOOOOOOOOOOO.",
    "YYYYYYYYYYYYYYY",
    "YYYYYYYYYYYYYYY",
    "YYYYYYYYYYYYYYY",
    ".GGGGGGGGGGGGG.",
    ".GGGGGGGGGGGGG.",
    "..BBBBBBBBBBB..",
    "...BBBBBBBBB...",
    "....PPPPPPP....",
    ".....PPPPP.....",
    "......PPP......",
)

private val GALLERY_PALETTE = mapOf(
    'F' to Color(0xFF37474F),
    'B' to Color(0xFF90CAF9),
    'Y' to Color(0xFFFFD43B),
    'M' to Color(0xFF2E7D32),
    'G' to Color(0xFFA5D6A7),
)
private val GALLERY_SPRITE = listOf(
    "FFFFFFFFFF",
    "FBBBBBBYYF",
    "FBBBBBBYYF",
    "FBBBBBBBBF",
    "FBBBMMBBBF",
    "FBMMMMMBBF",
    "FBMMMMMMBF",
    "FGGGGGGGGF",
    "FGGGGGGGGF",
    "FFFFFFFFFF",
)

private val CAMERA_PALETTE = mapOf(
    'D' to Color(0xFF4C6EF5),
    'F' to Color(0xFFFFD43B),
    'L' to Color(0xFF1F2933),
    'C' to Color(0xFF4DD0E1),
    'H' to Color(0xFFB2EBF2),
)
private val CAMERA_SPRITE = listOf(
    "...DDDD...",
    "..DDDDDD..",
    "DDDDDDDFFD",
    "DDDDLLDDFF",
    "DDDLCCLDDD",
    "DDLHCCCLDD",
    "DDLCCCCLDD",
    "DDDLCCLDDD",
    "DDDDLLDDDD",
    "DDDDDDDDDD",
)

private val CREATE_PALETTE = mapOf(
    'D' to Color(0xFFF2C48B),
    'R' to Color(0xFFE53935),
    'B' to Color(0xFF1E88E5),
    'Y' to Color(0xFFFDD835),
    'G' to Color(0xFF43A047),
)
private val CREATE_SPRITE = listOf(
    "...DDDD...",
    ".DDDDDDDD.",
    ".DDDDDDDD.",
    "DDRDDDDBDD",
    "DDRDDDDBDD",
    "DDDYDDGDDD",
    "DDDYDDGDDD",
    ".DDD..DDD.",
    ".DDDDDDDD.",
    "..DDDDDD..",
)

private val TIP_PALETTE = mapOf('Y' to Color(0xFFFFD54F), 'B' to Color(0xFF9E9E9E))
private val TIP_SPRITE = listOf(
    "..YYYY..",
    ".YYYYYY.",
    "YYYYYYYY",
    "YYYYYYYY",
    "YYYYYYYY",
    ".YYYYYY.",
    "..YYYY..",
    "..BBBB..",
)

private val EMPTY_PALETTE = mapOf('P' to Color(0xFFB0BEC5))
private val EMPTY_SPRITE = listOf(
    ".PP.PP.",
    "PPPPPPP",
    "PPPPPPP",
    "PPPPPPP",
    ".PPPPP.",
    "..PPP..",
    "...P...",
)

private val TIPS = listOf(
    "熨烫时垫一张烘焙纸，豆子就不会粘熨斗。",
    "先按颜色分类摆好豆子，成品更整齐也更快。",
    "图纸尺寸越大细节越多，但也更耗时。",
    "用镊子摆小尺寸的豆子，比用手更精准。",
    "拼好后压平冷却再取下，成品更平整。",
    "深浅色的豆子分开收纳，找色会快很多。",
    "先从大面积的颜色开始摆，效率更高。",
)

@Composable
fun HomeScreen(controller: PindouController) {
    val context = LocalContext.current
    val exporter = remember { ExportManager(context) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var showFreeDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { controller.pickImage(it) } }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) cameraUri?.let { controller.pickImage(it) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PixelIcon(LOGO_SPRITE, LOGO_PALETTE, Modifier.size(44.dp))
            Text("拼豆助手", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(4.dp))
        Text("导入图片生成图纸，或直接开始创作。", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionCard(
                title = "从相册生成",
                subtitle = "选择图片",
                sprite = GALLERY_SPRITE,
                palette = GALLERY_PALETTE,
                containerColor = CARD_BLUE,
                modifier = Modifier.weight(1f).height(140.dp),
                onClick = { galleryLauncher.launch("image/*") },
            )
            ActionCard(
                title = "拍照生成",
                subtitle = "拍一张照片",
                sprite = CAMERA_SPRITE,
                palette = CAMERA_PALETTE,
                containerColor = CARD_ORANGE,
                modifier = Modifier.weight(1f).height(140.dp),
                onClick = {
                    val file = exporter.newCameraFile()
                    val uri = exporter.uriFor(file)
                    cameraUri = uri
                    cameraLauncher.launch(uri)
                },
            )
        }

        Spacer(Modifier.height(12.dp))
        WideActionCard(
            title = "自由创作",
            subtitle = "空白图纸，自己拼",
            sprite = CREATE_SPRITE,
            palette = CREATE_PALETTE,
            containerColor = CARD_GREEN,
            onClick = { showFreeDialog = true },
        )

        Spacer(Modifier.height(32.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("快速模板", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            Text("点一下即可套用", style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(TEMPLATES, key = { it.id }) { template ->
                TemplateCard(template, onClick = { controller.startTemplateCreation(template) })
            }
        }

        Spacer(Modifier.height(32.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("最近图纸", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { controller.goHistory() }) { Text("查看全部") }
        }
        Spacer(Modifier.height(8.dp))

        val recent = controller.historyItems.take(RECENT_LIMIT)
        when {
            controller.historyLoading && recent.isEmpty() ->
                Text("正在加载…", style = MaterialTheme.typography.bodySmall)
            recent.isEmpty() ->
                EmptyRecentHint()
            else -> RecentGrid(recent) { controller.openPindou(it.uri) }
        }

        Spacer(Modifier.height(28.dp))
        TipCard()
        Spacer(Modifier.height(8.dp))
    }

    if (showFreeDialog) {
        FreeCreationDialog(
            controller = controller,
            onDismiss = { showFreeDialog = false },
            onConfirm = { paletteName, size ->
                controller.startFreeCreation(paletteName, size)
                showFreeDialog = false
            },
        )
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    sprite: List<String>,
    palette: Map<Char, Color>,
    containerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        PixelIcon(sprite, palette, Modifier.size(52.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun WideActionCard(
    title: String,
    subtitle: String,
    sprite: List<String>,
    palette: Map<Char, Color>,
    containerColor: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PixelIcon(sprite, palette, Modifier.size(48.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TemplateCard(template: PindouTemplate, onClick: () -> Unit) {
    val colors = remember(template) { template.colors.mapValues { Color(it.value.toArgb()) } }
    Column(
        modifier = Modifier
            .width(76.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center,
        ) {
            PixelIcon(template.sprite, colors, Modifier.size(40.dp))
        }
        Text(
            template.name,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RecentGrid(entries: List<PatternEntry>, onOpen: (PatternEntry) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        entries.chunked(3).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { entry ->
                    RecentCard(entry, Modifier.weight(1f)) { onOpen(entry) }
                }
                repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun EmptyRecentHint() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PixelIcon(EMPTY_SPRITE, EMPTY_PALETTE, Modifier.size(48.dp))
        Text(
            "还没有保存的图纸，点上方开始第一张吧",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TipCard() {
    var index by remember { mutableStateOf((0 until TIPS.size).random()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFFFF8E1))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PixelIcon(TIP_SPRITE, TIP_PALETTE, Modifier.size(36.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text("拼豆小贴士", style = MaterialTheme.typography.titleSmall)
            Text(
                TIPS[index],
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TextButton(onClick = { index = (index + 1) % TIPS.size }) { Text("换一条") }
    }
}

@Composable
private fun RecentCard(entry: PatternEntry, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GridThumbnail(
            project = entry.project,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFEEEEEE)),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            entry.title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FreeCreationDialog(
    controller: PindouController,
    onDismiss: () -> Unit,
    onConfirm: (paletteName: String, size: Int) -> Unit,
) {
    val metas = controller.paletteMetas
    var paletteName by remember { mutableStateOf(metas.firstOrNull()?.name.orEmpty()) }
    var size by remember { mutableStateOf(FREE_DEFAULT_SIZE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("自由创作") },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Text("拼豆色板", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(4.dp))
                DropdownPicker(
                    selected = metas.firstOrNull { it.name == paletteName }?.displayName ?: "请选择",
                    options = metas.map { "${it.displayName}（${it.colorCount} 色）" },
                    onSelect = { paletteName = metas[it].name },
                )
                Spacer(Modifier.height(12.dp))
                Text("图纸尺寸", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(4.dp))
                DropdownPicker(
                    selected = "${size}×${size}",
                    options = FREE_SIZES.map { "${it}×${it}" },
                    onSelect = { size = FREE_SIZES[it] },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(paletteName, size) },
                enabled = paletteName.isNotEmpty(),
            ) { Text("开始创作") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun DropdownPicker(
    selected: String,
    options: List<String>,
    onSelect: (Int) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Column {
        OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth()) {
            Text(
                selected,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelect(index); open = false },
                )
            }
        }
    }
}
