package com.pindou.app.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import com.pindou.app.PindouController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val CELL_SIZES = listOf(48, 64, 80, 96)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(controller: PindouController) {
    val project = controller.project ?: return
    val previewCell = 18

    val preview by produceState<Bitmap?>(initialValue = null, project, previewCell) {
        value = withContext(Dispatchers.Default) { controller.renderForm1(previewCell) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导出图纸") },
                navigationIcon = {
                    IconButton(onClick = { controller.goEditor() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text("打印预览（含坐标与色号，右侧为色表）", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            val bitmap = preview
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "图纸预览",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5)),
                )
            } else {
                Text("正在渲染预览…", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(16.dp))
            Text("每格像素", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CELL_SIZES.forEach { size ->
                    FilterChip(
                        selected = controller.exportCellSize == size,
                        onClick = { controller.exportCellSize = size },
                        label = { Text("$size px") },
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("可打印 PNG", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = !controller.busy,
                    onClick = { controller.exportPng(share = false) },
                ) { Text("保存 PNG") }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = !controller.busy,
                    onClick = { controller.exportPng(share = true) },
                ) { Text("分享 PNG") }
            }

            Spacer(Modifier.height(20.dp))
            Text("交互图纸 .pindou", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                "自包含文件，可在其它设备用本应用打开并交互查看。",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = !controller.busy,
                    onClick = { controller.exportPindou(share = false) },
                ) { Text("保存 .pindou") }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = !controller.busy,
                    onClick = { controller.exportPindou(share = true) },
                ) { Text("分享 .pindou") }
            }

            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { controller.goEditor() },
            ) { Text("返回编辑") }
        }
    }
}
