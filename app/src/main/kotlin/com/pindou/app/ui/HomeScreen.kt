package com.pindou.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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

private val FREE_SIZES = listOf(29, 52, 58, 78, 104, 116, 128)
private const val FREE_DEFAULT_SIZE = 52
private const val RECENT_LIMIT = 6

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
            .padding(24.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Text("拼豆助手", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text("导入图片生成图纸，或直接开始创作。", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = { galleryLauncher.launch("image/*") },
            ) { Text("从相册生成") }
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    val file = exporter.newCameraFile()
                    val uri = exporter.uriFor(file)
                    cameraUri = uri
                    cameraLauncher.launch(uri)
                },
            ) { Text("拍照生成") }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { showFreeDialog = true },
        ) { Text("自由创作（空白图纸）") }

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
                Text(
                    "还没有保存的图纸。保存后会自动出现在这里。",
                    style = MaterialTheme.typography.bodySmall,
                )
            else ->
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(recent, key = { it.uri.toString() }) { entry ->
                        RecentCard(entry = entry, onClick = { controller.openPindou(entry.uri) })
                    }
                }
        }
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
private fun RecentCard(entry: PatternEntry, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(96.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GridThumbnail(
            project = entry.project,
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(8.dp))
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
