package com.pindou.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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

/** Minimum crop-window edge, in screen px. */
private const val MIN_WINDOW_PX = 32f
/** Scale value tolerances: within this distance of fit the view is "full". */
private const val FULL_VIEW_EPS = 0.001f

private enum class Corner { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

private enum class GestureMode { PAN, MOVE_CROP, PINCH }

/**
 * Crop screen. A fixed on-screen crop [window] stays put while the image
 * (scale/offset) transforms underneath it. The image scale never drops below
 * the full-fit level, and its translation is always edge-constrained so the
 * full-fit state is naturally "locked" (smaller axes are auto-centered).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CropScreen(controller: PindouController) {
    val bitmap = controller.originalBitmap ?: return
    val iw = bitmap.width.toFloat()
    val ih = bitmap.height.toFloat()

    var container by remember { mutableStateOf(IntSize.Zero) }
    var scale by remember { mutableFloatStateOf(0f) }
    var fitScale by remember { mutableFloatStateOf(1f) }
    var fitOffset by remember { mutableStateOf(Offset.Zero) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var window by remember { mutableStateOf(RectF(0f, 0f, iw, ih)) }

    fun resetView() {
        if (container.width <= 0 || container.height <= 0) return
        val fit = min(container.width / iw, container.height / ih) * 0.92f
        fitScale = fit.coerceAtLeast(0.01f)
        fitOffset = Offset(
            (container.width - iw * fitScale) / 2f,
            (container.height - ih * fitScale) / 2f,
        )
        scale = fitScale
        offset = fitOffset
        window = RectF(fitOffset.x, fitOffset.y, fitOffset.x + iw * fitScale, fitOffset.y + ih * fitScale)
    }

    LaunchedEffect(container) { resetView() }

    BackHandler { controller.backFromCrop() }

    /** Constrains the image translation so it can never expose screen edges. */
    fun clampImage() {
        if (container.width <= 0 || container.height <= 0) return
        val imgW = iw * scale
        val imgH = ih * scale
        val x = if (imgW >= container.width) {
            offset.x.coerceIn(container.width - imgW, 0f)
        } else {
            (container.width - imgW) / 2f
        }
        val y = if (imgH >= container.height) {
            offset.y.coerceIn(container.height - imgH, 0f)
        } else {
            (container.height - imgH) / 2f
        }
        offset = Offset(x, y)
    }

    fun clampedWindow() {
        if (container.width <= 0 || container.height <= 0) return
        val img = RectF(offset.x, offset.y, offset.x + iw * scale, offset.y + ih * scale)
        if (img.width() <= 0f || img.height() <= 0f) return
        // The crop window may only occupy the visible picture area:
        // the intersection of the on-screen image rect and the viewport.
        val region = RectF(
            maxOf(img.left, 0f),
            maxOf(img.top, 0f),
            minOf(img.right, container.width.toFloat()),
            minOf(img.bottom, container.height.toFloat()),
        )
        if (region.width() <= 0f || region.height() <= 0f) return
        val minW = minOf(MIN_WINDOW_PX, region.width())
        val minH = minOf(MIN_WINDOW_PX, region.height())
        val w = maxOf(window.right - window.left, minW).coerceAtMost(region.width())
        val h = maxOf(window.bottom - window.top, minH).coerceAtMost(region.height())
        val l = window.left.coerceIn(region.left, maxOf(region.left, region.right - w))
        val t = window.top.coerceIn(region.top, maxOf(region.top, region.bottom - h))
        window = RectF(l, t, l + w, t + h)
    }

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
                                val down = awaitFirstDown()
                                val insideWindow = window.contains(down.position.x, down.position.y)
                                var mode = if (insideWindow) GestureMode.MOVE_CROP else GestureMode.PAN
                                var lastCentroid: Offset? = null
                                var lastDist: Float? = null

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val pressed = event.changes.filter { it.pressed }
                                    if (pressed.isEmpty()) break

                                    if (pressed.size >= 2) {
                                        mode = GestureMode.PINCH
                                        val p0 = pressed[0].position
                                        val p1 = pressed[1].position
                                        val dist = (p1 - p0).getDistance()
                                        val centroid = (p0 + p1) / 2f
                                        val prev = lastDist
                                        if (prev != null && prev > 0f && dist > 0f) {
                                            val newScale = (scale * (dist / prev)).coerceIn(fitScale, fitScale * 12f)
                                            if (newScale <= fitScale + FULL_VIEW_EPS) {
                                                // Reached the floor: snap back to the canonical full view.
                                                scale = fitScale
                                                offset = fitOffset
                                            } else {
                                                val k = newScale / scale
                                                val anchor = lastCentroid ?: centroid
                                                offset = Offset(
                                                    centroid.x - (anchor.x - offset.x) * k,
                                                    centroid.y - (anchor.y - offset.y) * k,
                                                )
                                                scale = newScale
                                            }
                                            clampImage()
                                            clampedWindow()
                                        }
                                        lastDist = dist
                                        lastCentroid = centroid
                                        pressed.forEach { it.consume() }
                                    } else {
                                        lastCentroid = null
                                        lastDist = null
                                        val change = pressed.first()
                                        val delta = change.position - change.previousPosition
                                        when (mode) {
                                            GestureMode.PINCH -> Unit
                                            GestureMode.MOVE_CROP -> {
                                                window = RectF(
                                                    window.left + delta.x,
                                                    window.top + delta.y,
                                                    window.right + delta.x,
                                                    window.bottom + delta.y,
                                                )
                                                clampedWindow()
                                            }
                                            GestureMode.PAN -> {
                                                offset += delta
                                                clampImage()
                                                clampedWindow()
                                            }
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

                    val win = window
                    val dim = Color(0x99000000)
                    drawRect(dim, Offset.Zero, Size(size.width, win.top.coerceAtLeast(0f)))
                    drawRect(dim, Offset(0f, win.bottom), Size(size.width, (size.height - win.bottom).coerceAtLeast(0f)))
                    drawRect(dim, Offset(0f, win.top), Size(win.left.coerceAtLeast(0f), (win.bottom - win.top).coerceAtLeast(0f)))
                    drawRect(dim, Offset(win.right, win.top), Size((size.width - win.right).coerceAtLeast(0f), (win.bottom - win.top).coerceAtLeast(0f)))
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(win.left, win.top),
                        size = Size((win.right - win.left).coerceAtLeast(0f), (win.bottom - win.top).coerceAtLeast(0f)),
                        style = Stroke(width = 2f),
                    )
                }

                CornerHandle(Corner.TOP_LEFT, window.left, window.top) { dx, dy ->
                    window = RectF(
                        (window.left + dx).coerceAtMost(window.right - 1f),
                        (window.top + dy).coerceAtMost(window.bottom - 1f),
                        window.right,
                        window.bottom,
                    )
                    clampedWindow()
                }
                CornerHandle(Corner.TOP_RIGHT, window.right, window.top) { dx, dy ->
                    window = RectF(
                        window.left,
                        (window.top + dy).coerceAtMost(window.bottom - 1f),
                        (window.right + dx).coerceAtLeast(window.left + 1f),
                        window.bottom,
                    )
                    clampedWindow()
                }
                CornerHandle(Corner.BOTTOM_LEFT, window.left, window.bottom) { dx, dy ->
                    window = RectF(
                        (window.left + dx).coerceAtMost(window.right - 1f),
                        window.top,
                        window.right,
                        (window.bottom + dy).coerceAtLeast(window.top + 1f),
                    )
                    clampedWindow()
                }
                CornerHandle(Corner.BOTTOM_RIGHT, window.right, window.bottom) { dx, dy ->
                    window = RectF(
                        window.left,
                        window.top,
                        (window.right + dx).coerceAtLeast(window.left + 1f),
                        (window.bottom + dy).coerceAtLeast(window.top + 1f),
                    )
                    clampedWindow()
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
                Button(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    onClick = {
                        val l = ((window.left - offset.x) / scale).coerceIn(0f, iw)
                        val t = ((window.top - offset.y) / scale).coerceIn(0f, ih)
                        val r = ((window.right - offset.x) / scale).coerceIn(0f, iw)
                        val b = ((window.bottom - offset.y) / scale).coerceIn(0f, ih)
                        val rect = Rect(
                            l.toInt().coerceIn(0, bitmap.width - 1),
                            t.toInt().coerceIn(0, bitmap.height - 1),
                            r.toInt().coerceIn(1, bitmap.width),
                            b.toInt().coerceIn(1, bitmap.height),
                        )
                        val left = minOf(rect.left, rect.right - 1)
                        val top = minOf(rect.top, rect.bottom - 1)
                        val w = (rect.right - left).coerceAtLeast(1)
                        val h = (rect.bottom - top).coerceAtLeast(1)
                        val cropped = Bitmap.createBitmap(bitmap, left, top, w, h)
                        controller.onCropConfirmed(cropped.toPixelImage())
                    },
                ) { Text("确认") }
            }
        }
    }
}

/**
 * A white L-shaped crop corner bracket with a center-aligned touch target.
 * It consumes the pointer-down so the parent image transform never also reacts.
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
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id && it.pressed } ?: break
                        val delta = change.position - change.previousPosition
                        if (delta != Offset.Zero) {
                            onDrag(delta.x, delta.y)
                        }
                        change.consume()
                    }
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