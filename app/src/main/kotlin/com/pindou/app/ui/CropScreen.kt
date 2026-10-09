package com.pindou.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.pindou.app.PindouController
import com.pindou.app.data.toPixelImage
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.min
import kotlin.math.roundToInt

private const val MIN_CROP_PX = 8f

private enum class Corner { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

private enum class GestureMode { PAN, MOVE_CROP, PINCH }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CropScreen(controller: PindouController) {
    val bitmap = controller.originalBitmap ?: return
    val iw = bitmap.width.toFloat()
    val ih = bitmap.height.toFloat()

    var container by remember { mutableStateOf(IntSize.Zero) }
    var scale by remember { mutableFloatStateOf(0f) }
    var fitScale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var crop by remember { mutableStateOf(RectF(0f, 0f, iw, ih)) }

    fun resetView() {
        if (container.width <= 0 || container.height <= 0) return
        val fit = min(container.width / iw, container.height / ih) * 0.92f
        fitScale = fit.coerceAtLeast(0.01f)
        scale = fitScale
        offset = Offset(
            (container.width - iw * fitScale) / 2f,
            (container.height - ih * fitScale) / 2f,
        )
        crop = RectF(0f, 0f, iw, ih)
    }

    LaunchedEffect(container) { resetView() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("裁剪与缩放") },
                navigationIcon = {
                    IconButton(onClick = { controller.backFromCrop() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF222222)),
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { container = it }
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                val firstDown = awaitFirstDown(requireUnconsumed = false)
                                val startedInsideCrop = scale > 0f && run {
                                    val img = Offset(
                                        (firstDown.position.x - offset.x) / scale,
                                        (firstDown.position.y - offset.y) / scale,
                                    )
                                    img.x in crop.left..crop.right && img.y in crop.top..crop.bottom
                                }
                                var mode = if (startedInsideCrop) GestureMode.MOVE_CROP else GestureMode.PAN
                                var lastCentroid: Offset? = null

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val pressed = event.changes.filter { it.pressed }
                                    if (pressed.isEmpty()) break

                                    if (pressed.size >= 2) {
                                        // Two fingers: pinch-zoom anchored at the finger centroid.
                                        mode = GestureMode.PINCH
                                        val centroid =
                                            pressed.fold(Offset.Zero) { acc, c -> acc + c.position } /
                                            pressed.size.toFloat()
                                        val zoom = event.calculateZoom()
                                        val newScale = (scale * zoom).coerceIn(fitScale * 0.5f, fitScale * 12f)
                                        val k = newScale / scale
                                        val anchor = lastCentroid ?: centroid
                                        offset = Offset(
                                            centroid.x - (anchor.x - offset.x) * k,
                                            centroid.y - (anchor.y - offset.y) * k,
                                        )
                                        scale = newScale
                                        lastCentroid = centroid
                                        pressed.forEach { it.consume() }
                                    } else {
                                        lastCentroid = null
                                        val change = pressed.first()
                                        val delta = change.position - change.previousPosition
                                        when (mode) {
                                            GestureMode.MOVE_CROP -> {
                                                val dx = delta.x / scale
                                                val dy = delta.y / scale
                                                val nl = (crop.left + dx).coerceIn(0f, iw - crop.width())
                                                val nt = (crop.top + dy).coerceIn(0f, ih - crop.height())
                                                crop = RectF(nl, nt, nl + crop.width(), nt + crop.height())
                                            }
                                            GestureMode.PAN -> offset += delta
                                            GestureMode.PINCH -> Unit
                                        }
                                        change.consume()
                                    }
                                }
                            }
                        },
                ) {
                    if (scale <= 0f) return@Canvas
                    drawImage(
                        image = bitmap.asImageBitmap(),
                        dstOffset = IntOffset(offset.x.roundToInt(), offset.y.roundToInt()),
                        dstSize = IntSize((iw * scale).roundToInt(), (ih * scale).roundToInt()),
                    )

                    val sx0 = offset.x + crop.left * scale
                    val sy0 = offset.y + crop.top * scale
                    val sx1 = offset.x + crop.right * scale
                    val sy1 = offset.y + crop.bottom * scale
                    val dim = Color(0x99000000)
                    drawRect(dim, Offset.Zero, Size(size.width, sy0.coerceAtLeast(0f)))
                    drawRect(dim, Offset(0f, sy1), Size(size.width, (size.height - sy1).coerceAtLeast(0f)))
                    drawRect(dim, Offset(0f, sy0), Size(sx0.coerceAtLeast(0f), (sy1 - sy0).coerceAtLeast(0f)))
                    drawRect(dim, Offset(sx1, sy0), Size((size.width - sx1).coerceAtLeast(0f), (sy1 - sy0).coerceAtLeast(0f)))
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(sx0, sy0),
                        size = Size((sx1 - sx0).coerceAtLeast(0f), (sy1 - sy0).coerceAtLeast(0f)),
                        style = Stroke(width = 2f),
                    )
                }

                fun sx(ix: Float) = offset.x + ix * scale
                fun sy(iy: Float) = offset.y + iy * scale

                CornerHandle(Corner.TOP_LEFT, sx(crop.left), sy(crop.top)) { dx, dy ->
                    val nl = (crop.left + dx).coerceIn(0f, crop.right - MIN_CROP_PX)
                    val nt = (crop.top + dy).coerceIn(0f, crop.bottom - MIN_CROP_PX)
                    crop = RectF(nl, nt, crop.right, crop.bottom)
                }
                CornerHandle(Corner.TOP_RIGHT, sx(crop.right), sy(crop.top)) { dx, dy ->
                    val nr = (crop.right + dx).coerceIn(crop.left + MIN_CROP_PX, iw)
                    val nt = (crop.top + dy).coerceIn(0f, crop.bottom - MIN_CROP_PX)
                    crop = RectF(crop.left, nt, nr, crop.bottom)
                }
                CornerHandle(Corner.BOTTOM_LEFT, sx(crop.left), sy(crop.bottom)) { dx, dy ->
                    val nl = (crop.left + dx).coerceIn(0f, crop.right - MIN_CROP_PX)
                    val nb = (crop.bottom + dy).coerceIn(crop.top + MIN_CROP_PX, ih)
                    crop = RectF(nl, crop.top, crop.right, nb)
                }
                CornerHandle(Corner.BOTTOM_RIGHT, sx(crop.right), sy(crop.bottom)) { dx, dy ->
                    val nr = (crop.right + dx).coerceIn(crop.left + MIN_CROP_PX, iw)
                    val nb = (crop.bottom + dy).coerceIn(crop.top + MIN_CROP_PX, ih)
                    crop = RectF(crop.left, crop.top, nr, nb)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { resetView() },
                ) { Text("重置") }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { crop = RectF(0f, 0f, iw, ih) },
                ) { Text("全图") }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val rect = Rect(
                            crop.left.toInt().coerceIn(0, bitmap.width - 1),
                            crop.top.toInt().coerceIn(0, bitmap.height - 1),
                            crop.right.toInt().coerceIn(1, bitmap.width),
                            crop.bottom.toInt().coerceIn(1, bitmap.height),
                        )
                        val w = (rect.right - rect.left).coerceAtLeast(1)
                        val h = (rect.bottom - rect.top).coerceAtLeast(1)
                        val cropped = Bitmap.createBitmap(bitmap, rect.left, rect.top, w, h)
                        controller.onCropConfirmed(cropped.toPixelImage())
                    },
                ) { Text("确认") }
            }
        }
    }
}

/**
 * A white L-shaped crop corner bracket. Its vertex sits exactly on the image
 * corner at (x, y); the canvas is sized as a touch target centered on it.
 */
@Composable
private fun CornerHandle(
    corner: Corner,
    x: Float,
    y: Float,
    onDrag: (dx: Float, dy: Float) -> Unit,
) {
    val density = LocalDensity.current
    val touchSize: Dp = 44.dp
    val touchPx = with(density) { touchSize.toPx() }
    val lenPx = with(density) { 18.dp.toPx() }
    val thickPx = with(density) { 4.dp.toPx() }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    if (corner == Corner.TOP_LEFT || corner == Corner.BOTTOM_LEFT) x.roundToInt() else (x - touchPx).roundToInt(),
                    if (corner == Corner.TOP_LEFT || corner == Corner.TOP_RIGHT) y.roundToInt() else (y - touchPx).roundToInt(),
                )
            }
            .size(touchSize)
            .pointerInput(Unit) {
                detectDragGestures { change, drag ->
                    change.consume()
                    onDrag(drag.x, drag.y)
                }
            },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val vertex = when (corner) {
                Corner.TOP_LEFT -> Offset(0f, 0f)
                Corner.TOP_RIGHT -> Offset(touchPx, 0f)
                Corner.BOTTOM_LEFT -> Offset(0f, touchPx)
                Corner.BOTTOM_RIGHT -> Offset(touchPx, touchPx)
            }
            val right = if (corner == Corner.TOP_RIGHT || corner == Corner.BOTTOM_RIGHT) -lenPx else lenPx
            val down = if (corner == Corner.TOP_LEFT || corner == Corner.TOP_RIGHT) lenPx else -lenPx
            drawLine(Color.White, vertex, vertex + Offset(right, 0f), strokeWidth = thickPx, cap = StrokeCap.Round)
            drawLine(Color.White, vertex, vertex + Offset(0f, down), strokeWidth = thickPx, cap = StrokeCap.Round)
        }
    }
}