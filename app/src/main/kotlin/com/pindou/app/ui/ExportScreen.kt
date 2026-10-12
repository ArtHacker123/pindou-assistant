package com.pindou.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import com.pindou.app.ExportSize
import com.pindou.app.PindouController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(controller: PindouController) {
    val project = controller.project ?: return
    val previewCell = 18

    val appContext = LocalContext.current.applicationContext
    var pendingSave by remember { mutableStateOf<(() -> Unit)?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) pendingSave?.invoke()
        else controller.showError("需要「存储」权限才能保存到相册/文档")
        pendingSave = null
    }
    fun saveWithLegacyPermission(action: () -> Unit) {
        val ok = Build.VERSION.SDK_INT >= 29 ||
            appContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
        if (ok) action()
        else {
            pendingSave = action
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    val preview by produceState<Bitmap?>(initialValue = null, project, controller.exportSize, previewCell) {
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
            Text("输出分辨率", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            val plan = controller.resolvedExportPlan(project)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExportSize.values().forEach { size ->
                    FilterChip(
                        selected = controller.exportSize == size,
                        onClick = { controller.exportSize = size },
                        label = { Text(size.label) },
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                when (controller.exportSize) {
                    ExportSize.A4 ->
                        "A4@300DPI（10mm 边距）：${project.grid.width}×${project.grid.height} → ${plan.cellOutPx} px/格，" +
                            "PNG≈${plan.canvasWidth}×${plan.canvasHeight}px，整页无缩放打印。"
                    ExportSize.MAX ->
                        "单图内存上限内最大：${project.grid.width}×${project.grid.height} → ${plan.cellOutPx} px/格，" +
                            "PNG≈${plan.canvasWidth}×${plan.canvasHeight}px，适合大屏/大幅放大查看。"
                    else ->
                        "${controller.exportSize.label} 高清：${project.grid.width}×${project.grid.height} → " +
                            "${plan.cellOutPx} px/格，PNG≈${plan.canvasWidth}×${plan.canvasHeight}px，适合屏幕放大对色。"
                },
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(Modifier.height(24.dp))
            Text("可打印 PNG", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = !controller.busy,
                    onClick = { saveWithLegacyPermission { controller.exportPng(share = false) } },
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
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = !controller.busy,
                    onClick = { controller.exportPindou(share = true) },
                ) { Text("分享 .pindou") }
            }
            if (project.referenceGrid != null) {
                Spacer(Modifier.height(8.dp))
                FilterChip(
                    selected = controller.includeReference,
                    onClick = { controller.includeReference = !controller.includeReference },
                    label = { Text("包含参考图（对比原图）") },
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    if (controller.includeReference) "文件内嵌原图参考，可在其它设备对比查看（文件更大）。"
                    else "不内嵌参考图，文件更小，但其它设备打开后无法对比原图。",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { controller.goEditor() },
            ) { Text("返回编辑") }
        }
    }
}
