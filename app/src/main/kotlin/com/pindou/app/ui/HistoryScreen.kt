package com.pindou.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pindou.app.PindouController
import com.pindou.app.data.PatternEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

/** A standalone page listing every saved pattern as a thumbnail grid. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(controller: PindouController) {
    LaunchedEffect(Unit) { controller.refreshHistory() }
    var pendingDelete by remember { mutableStateOf<PatternEntry?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的图纸") },
                navigationIcon = {
                    IconButton(onClick = { controller.goHome() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        val entries = controller.historyItems
        when {
            controller.historyLoading && entries.isEmpty() ->
                CenteredHint(padding, "正在加载…")
            entries.isEmpty() ->
                CenteredHint(padding, "还没有保存的图纸。\n保存或导出后会自动出现在这里。")
            else ->
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(entries, key = { it.uri.toString() }) { entry ->
                        HistoryCard(
                            entry = entry,
                            onClick = { controller.openPindou(entry.uri) },
                            onDelete = { pendingDelete = entry },
                        )
                    }
                }
        }
    }

    pendingDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除图纸") },
            text = { Text("确定删除「${entry.title}」？此操作会移除已保存的 .pindou 文件。") },
            confirmButton = {
                TextButton(onClick = {
                    controller.deleteHistory(entry)
                    pendingDelete = null
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun CenteredHint(padding: PaddingValues, text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun HistoryCard(
    entry: PatternEntry,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            GridThumbnail(
                project = entry.project,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color(0xFFF5F5F5))
                    .clickable(onClick = onClick),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        entry.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        formatDate(entry.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "删除")
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String =
    if (timestamp > 0L) DATE_FORMAT.format(Date(timestamp)) else ""
