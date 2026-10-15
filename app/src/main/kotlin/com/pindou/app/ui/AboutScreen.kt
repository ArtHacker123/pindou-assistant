package com.pindou.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pindou.app.PindouController

/** A standalone page with the app identity and its privacy note. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(controller: PindouController) {
    BackHandler { controller.goHome() }

    val context = LocalContext.current
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("关于与隐私") },
                navigationIcon = {
                    IconButton(onClick = { controller.goHome() }) {
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("拼豆助手", style = MaterialTheme.typography.headlineSmall)
            versionName?.let {
                Text(
                    "版本 $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("隐私说明", style = MaterialTheme.typography.titleMedium)

            PrivacySection(
                title = "图片仅在本机处理",
                body = "你选择的图片只用于在本机生成拼豆图纸，不会上传到任何服务器。",
            )
            PrivacySection(
                title = "只读取你选择的那张图片",
                body = "相册与相机通过系统的照片选择器 / 系统相机完成，应用只会收到你主动选择或拍摄的那一张图片，不会读取整个相册。",
            )
            PrivacySection(
                title = "联网按需发生",
                body = "只有当你在将来主动使用在线（如分享、社区）功能时才会联网，应用不会在后台联网或静默上传。",
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PrivacySection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
