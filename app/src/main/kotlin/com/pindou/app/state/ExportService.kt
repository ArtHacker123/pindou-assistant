package com.pindou.app.state

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pindou.app.ExportSize
import com.pindou.app.data.ExportManager
import com.pindou.app.render.Form1BitmapRenderer
import com.pindou.core.model.PindouProject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Renders the print PNG, writes the `.pindou` payload, and shares/saves either.
 * Owns the export options ([exportSize]) and reports busy / status / errors
 * through [NavigationState].
 */
class ExportService(
    private val context: Context,
    private val scope: CoroutineScope,
    private val nav: NavigationState,
) {

    private val TAG = "ExportService"
    private val exportManager = ExportManager(context)

    var exportSize by mutableStateOf(ExportSize.MAX)

    fun renderForm1(project: PindouProject?, cellSize: Int? = null): Bitmap? {
        val p = project ?: return null
        return Form1BitmapRenderer.render(p, cellSize ?: resolvedExportCellSize(p))
    }

    /** Resolves the chosen export size to px/cell and the resulting PNG dimensions. */
    fun resolvedExportPlan(p: PindouProject): Form1BitmapRenderer.ExportPlan =
        when (exportSize) {
            ExportSize.MAX -> Form1BitmapRenderer.fitMax(p)
            ExportSize.A4 -> Form1BitmapRenderer.fitToA4(p)
        }

    fun resolvedExportCellSize(p: PindouProject): Int = resolvedExportPlan(p).cellOutPx

    fun exportPng(project: PindouProject?, share: Boolean) {
        val p = project ?: return
        if (!share && !storagePermissionOk()) {
            nav.showError("需要「存储」权限才能保存到相册，请在系统设置中开启")
            return
        }
        nav.markBusy(true)
        nav.clearError()
        scope.launch {
            try {
                if (share) {
                    val file = withContext(Dispatchers.Default) {
                        val bitmap = Form1BitmapRenderer.render(p, resolvedExportCellSize(p))
                        exportManager.savePng(bitmap, baseName(p))
                    }
                    exportManager.share(file, ExportManager.MIME_PNG, "分享拼豆图纸")
                } else {
                    withContext(Dispatchers.Default) {
                        val bitmap = Form1BitmapRenderer.render(p, resolvedExportCellSize(p))
                        exportManager.savePngToGallery(bitmap, baseName(p))
                    }
                    nav.setStatus("已保存到系统相册（Pictures/PindouAssistant）")
                }
            } catch (e: Exception) {
                Log.w(TAG, "exportPng failed", e)
                nav.showError(e.message ?: "导出失败")
            } finally {
                nav.markBusy(false)
            }
        }
    }

    fun exportPindou(
        project: PindouProject?,
        share: Boolean,
        onSaved: (() -> Unit)? = null,
    ) {
        val p = project ?: return
        if (!share && !storagePermissionOk()) {
            nav.showError("需要「存储」权限才能保存到文档目录，请在系统设置中开启")
            return
        }
        // Always embed the reference image so shared files stay comparable on other devices.
        val toSave = p
        nav.markBusy(true)
        nav.clearError()
        scope.launch {
            try {
                if (share) {
                    val file = withContext(Dispatchers.IO) {
                        exportManager.saveProject(toSave, pindouBaseName(p))
                    }
                    exportManager.share(
                        file,
                        ExportManager.MIME_BINARY,
                        "分享交互图纸",
                        ExportManager.MIME_ANY,
                    )
                } else {
                    val uri = withContext(Dispatchers.IO) {
                        exportManager.saveProjectToDocuments(toSave, pindouBaseName(p))
                    }
                    nav.setStatus("已保存到 文档/PindouAssistant/${uri.lastPathSegment ?: ""}")
                    onSaved?.invoke()
                }
            } catch (e: Exception) {
                Log.w(TAG, "exportPindou failed", e)
                nav.showError(e.message ?: "导出失败")
            } finally {
                nav.markBusy(false)
            }
        }
    }

    /** Shares an already-saved `.pindou` from Documents (copies it into our share dir first). */
    fun shareStoredPindou(uri: Uri, displayName: String) {
        nav.markBusy(true)
        nav.clearError()
        scope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    exportManager.copyToShare(uri, displayName)
                }
                exportManager.share(
                    file,
                    ExportManager.MIME_BINARY,
                    "分享交互图纸",
                    ExportManager.MIME_ANY,
                )
            } catch (e: Exception) {
                Log.w(TAG, "shareStoredPindou failed", e)
                nav.showError(e.message ?: "分享失败")
            } finally {
                nav.markBusy(false)
            }
        }
    }

    private fun storagePermissionOk(): Boolean {
        if (Build.VERSION.SDK_INT >= 29) return true
        return context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun baseName(p: PindouProject): String {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "pindou_${p.grid.width}x${p.grid.height}_$stamp"
    }

    /** Stable name derived from the project's creation time so re-saving overwrites the same file. */
    private fun pindouBaseName(p: PindouProject): String {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(p.meta.createdAt))
        return "pindou_${p.grid.width}x${p.grid.height}_$stamp"
    }
}
