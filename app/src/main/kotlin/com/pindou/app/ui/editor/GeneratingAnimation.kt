package com.pindou.app.ui.editor

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Side length of the little peg board, in cells. */
private const val BOARD = 5

/** Fraction of the cycle spent staggering the wave across the board. */
private const val WAVE_MAX_DELAY = 0.62f

/** How long (in cycle fraction) a single bead takes to pop in. */
private const val BEAD_APPEAR = 0.16f

/** Fraction of the cycle after which the whole board fades out before looping. */
private const val FADE_START = 0.85f

/** Used before a palette is available, or as a variety fallback. */
private val FALLBACK_COLORS = listOf(
    Color(0xFFE53935),
    Color(0xFFFB8C00),
    Color(0xFFFDD835),
    Color(0xFF43A047),
    Color(0xFF1E88E5),
    Color(0xFF8E24AA),
)

/**
 * A perler-bead themed "generating" indicator: beads pop into a small peg board
 * in a diagonal wave — one color per diagonal, cycling through the palette —
 * then the board fades and repeats. Reads as a pattern being assembled bead by
 * bead, which fits the app far better than a generic spinner. Drawn with a
 * transparent background so it floats over whatever is behind it.
 *
 * @param colors bead colors to cycle through; falls back to a small built-in set
 *   when empty.
 * @param textColor label color; defaults to the ambient content color.
 */
@Composable
fun GeneratingIndicator(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
) {
    val palette = colors.ifEmpty { FALLBACK_COLORS }
    val transition = rememberInfiniteTransition(label = "generating")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "progress",
    )

    Column(
        modifier = modifier.padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Canvas(modifier = Modifier.size(132.dp)) {
            val n = BOARD
            val cell = size.minDimension / n
            val beadRadius = cell * 0.34f
            val pegRadius = cell * 0.12f

            val fade = if (progress > FADE_START) {
                ((1f - progress) / (1f - FADE_START)).coerceIn(0f, 1f)
            } else {
                1f
            }

            for (r in 0 until n) {
                for (c in 0 until n) {
                    val cx = (c + 0.5f) * cell
                    val cy = (r + 0.5f) * cell
                    val center = Offset(cx, cy)

                    // Empty peg, always visible.
                    drawCircle(Color(0xFFD0D0D0), radius = pegRadius, center = center)

                    val delay = ((r + c).toFloat() / (2f * (n - 1))) * WAVE_MAX_DELAY
                    val appear = ((progress - delay) / BEAD_APPEAR).coerceIn(0f, 1f)
                    if (appear <= 0f) continue

                    val alpha = fade * appear
                    val bead = palette[(r + c) % palette.size]
                    val radius = beadRadius * (0.55f + 0.45f * appear)
                    drawCircle(bead.copy(alpha = alpha), radius = radius, center = center)
                    // Soft highlight gives the bead a domed, physical look.
                    drawCircle(
                        color = Color.White.copy(alpha = 0.45f * alpha),
                        radius = radius * 0.3f,
                        center = Offset(cx - radius * 0.32f, cy - radius * 0.32f),
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("生成中…", style = MaterialTheme.typography.bodyMedium, color = textColor)
    }
}
