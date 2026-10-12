package com.pindou.app.ui

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.pindou.app.PindouController
import com.pindou.app.Screen
import com.pindou.app.ui.editor.EditorScreen

/** Root composable: owns the controller and routes between screens. */
@Composable
fun PindouApp(pendingUri: MutableState<Uri?>) {
    val context = LocalContext.current
    val controller = remember { PindouController(context.applicationContext) }

    LaunchedEffect(pendingUri.value) {
        val uri = pendingUri.value
        if (uri != null) {
            controller.openPindou(uri)
            pendingUri.value = null
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        when (controller.screen) {
            Screen.Home -> HomeScreen(controller)
            Screen.History -> HistoryScreen(controller)
            Screen.Crop -> CropScreen(controller)
            Screen.Editor -> EditorScreen(controller)
            Screen.Export -> ExportScreen(controller)
        }
    }

    controller.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { controller.clearError() },
            confirmButton = {
                TextButton(onClick = { controller.clearError() }) { Text("确定") }
            },
            title = { Text("提示") },
            text = { Text(message) },
        )
    }

    controller.statusMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { controller.clearStatus() },
            confirmButton = {
                TextButton(onClick = { controller.clearStatus() }) { Text("确定") }
            },
            title = { Text("完成") },
            text = { Text(message) },
        )
    }
}
