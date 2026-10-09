package com.pindou.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.pindou.app.PindouController
import com.pindou.core.generate.PatternGenerator
import com.pindou.core.processor.ImageType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParameterScreen(controller: PindouController) {
    var paletteMenuOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("生成参数") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text("图像类型", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ImageType.values().forEach { type ->
                    FilterChip(
                        selected = controller.imageType == type,
                        onClick = { controller.imageType = type },
                        label = { Text(type.displayName) },
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("拼豆色板", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Column {
                OutlinedButton(onClick = { paletteMenuOpen = true }) {
                    val current = controller.paletteMetas.firstOrNull { it.name == controller.paletteName }
                    Text(current?.let { "${it.displayName}（${it.colorCount} 色）" } ?: "选择色板")
                }
                DropdownMenu(expanded = paletteMenuOpen, onDismissRequest = { paletteMenuOpen = false }) {
                    controller.paletteMetas.forEach { meta ->
                        DropdownMenuItem(
                            text = { Text("${meta.displayName}（${meta.colorCount} 色）") },
                            onClick = {
                                controller.paletteName = meta.name
                                paletteMenuOpen = false
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("用色数量：${controller.colorCountTarget} 种", style = MaterialTheme.typography.titleSmall)
            Slider(
                value = controller.colorCountTarget.toFloat(),
                onValueChange = { controller.colorCountTarget = it.toInt().coerceIn(4, 128) },
                valueRange = 4f..128f,
            )

            Spacer(Modifier.height(12.dp))
            Text("网格尺寸（最大 ${PatternGenerator.MAX_GRID}×${PatternGenerator.MAX_GRID}）", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = controller.gridWidth.toString(),
                    onValueChange = { controller.gridWidth = it.filter { c -> c.isDigit() }.toIntOrNull() ?: 1 },
                    label = { Text("宽") },
                    singleLine = true,
                    modifier = Modifier.width(120.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                Spacer(Modifier.width(16.dp))
                OutlinedTextField(
                    value = controller.gridHeight.toString(),
                    onValueChange = { controller.gridHeight = it.filter { c -> c.isDigit() }.toIntOrNull() ?: 1 },
                    label = { Text("高") },
                    singleLine = true,
                    modifier = Modifier.width(120.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }

            Spacer(Modifier.height(32.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !controller.busy && controller.paletteName != null,
                onClick = {
                    controller.gridWidth = controller.gridWidth.coerceIn(1, PatternGenerator.MAX_GRID)
                    controller.gridHeight = controller.gridHeight.coerceIn(1, PatternGenerator.MAX_GRID)
                    controller.generate()
                },
            ) {
                Text(if (controller.busy) "生成中…" else "生成图纸")
            }
            Text(
                text = "生成后可在交互视图中点选格子高亮同色。",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
        }
    }
}
