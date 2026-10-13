package com.pindou.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pindou.app.ExportSize
import com.pindou.app.PindouController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val HOME_PALETTE = mapOf(
    'R' to Color(0xFFE5533D),
    'W' to Color(0xFFFFD9A0),
    'D' to Color(0xFF6D4C41),
)

private val HOME_SPRITE = listOf(
    "...R...",
    "..RRR..",
    ".RRRRR.",
    "RRRRRRR",
    "WWWWWWW",
    "WWWDWWW",
    "WWWDWWW",
)

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
        else controller.showError("需要「存储」权限才能保存到相册")
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            val bitmap = preview
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "图纸预览",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F5F5)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("正在渲染预览…", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("输出分辨率", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(6.dp))
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
            Spacer(Modifier.height(6.dp))
            Text(
                when (controller.exportSize) {
                    ExportSize.MAX ->
                        "内存上限内最大：${project.grid.width}×${project.grid.height}，PNG≈${plan.canvasWidth}×${plan.canvasHeight}px，适合大屏放大查看。"
                    ExportSize.A4 ->
                        "A4@300DPI：${project.grid.width}×${project.grid.height}，PNG≈${plan.canvasWidth}×${plan.canvasHeight}px，整页无缩放打印。"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (project.referenceGrid != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "分享的图纸会内嵌参考图，可在其它设备对比原图。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = !controller.busy,
                    onClick = { saveWithLegacyPermission { controller.exportPng(share = false) } },
                ) { Text("保存 PNG") }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = !controller.busy,
                    onClick = { controller.exportPindou(share = true) },
                ) { Text("分享图纸") }
            }

            Spacer(Modifier.height(8.dp))
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { controller.goHome() },
            ) {
                PixelIcon(
                    sprite = HOME_SPRITE,
                    palette = HOME_PALETTE,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text("返回首页")
            }
        }
    }
}
