package com.pindou.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pindou.app.PindouController
import com.pindou.app.data.ExportManager

@Composable
fun HomeScreen(controller: PindouController) {
    val context = LocalContext.current
    val exporter = remember { ExportManager(context) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { controller.pickImage(it) } }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) cameraUri?.let { controller.pickImage(it) } }

    val openLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { controller.openPindou(it) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("拼豆助手", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "导入图片，生成可打印、可交互、可分享的拼豆图纸。",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(32.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { galleryLauncher.launch("image/*") },
        ) { Text("从相册选择") }

        Spacer(Modifier.height(12.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val file = exporter.newCameraFile()
                val uri = exporter.uriFor(file)
                cameraUri = uri
                cameraLauncher.launch(uri)
            },
        ) { Text("拍照") }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { openLauncher.launch(arrayOf("application/x-pindou", "*/*")) },
        ) { Text("打开 .pindou 图纸") }

        Spacer(Modifier.height(32.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("内置色板", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                controller.paletteMetas.forEach { meta ->
                    Text("• ${meta.displayName}（${meta.colorCount} 色）")
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "提示：分享的 .pindou 文件可在其它设备用本应用打开，直接查看交互图纸。",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
