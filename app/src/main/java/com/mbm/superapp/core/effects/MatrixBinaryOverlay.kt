package com.mbm.superapp.core.effects

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.hypot
import kotlin.random.Random

/*
 * Full-screen Matrix rain with tear-and-heal.
 *
 * Grid-based: columns are spaced evenly across the entire width (like a monospace grid).
 * Each column has 2 overlapping streams so the column is never empty — continuous flow.
 * Every cell on screen always has a 0 or 1 character.
 *
 * On touch: dark tear opens, matrix chars inside become vivid bright green.
 * Release: tear heals shut.
 *
 * No pointerInput — all touches pass through.
 */

// Grid cell size — controls the Matrix "spacing" feel
private const val CELL_W = 26f   // horizontal spacing between columns
private const val CELL_H = 28f   // vertical spacing between rows

// A single falling stream within a column
private class Stream(
    var yOffset: Float,
    val speed: Float,
    val len: Int,
    val chars: CharArray,
) {
    companion object {
        fun make(screenH: Float): Stream {
            val n = Random.nextInt(12, 30)
            return Stream(
                yOffset = -Random.nextFloat() * screenH * 1.5f,
                speed = Random.nextFloat() * 3f + 1.8f,
                len = n,
                chars = CharArray(n) { if (Random.nextBoolean()) '0' else '1' },
            )
        }
    }
}

// Each grid column has 2 streams offset so there's always something falling
private class MatrixCol(
    val colIndex: Int,
    val streams: List<Stream>,
)

private data class TrailPt(val x: Float, val y: Float, var life: Float = 1f)

@Composable
fun MatrixRainOverlay(
    isTouching: Boolean = false,
    touchX: Float = 0f,
    touchY: Float = 0f,
) {
    var tick by remember { mutableLongStateOf(0L) }
    val textMeasurer = rememberTextMeasurer()

    // We compute columns dynamically based on screen width (remembered once)
    val columns = remember { mutableListOf<MatrixCol>() }
    val allStreams = remember { mutableListOf<Stream>() }
    var initialized by remember { mutableLongStateOf(0L) }

    val trail = remember { mutableListOf<TrailPt>() }

    val tearRadius by animateFloatAsState(
        targetValue = if (isTouching) 220f else 0f,
        animationSpec = tween(if (isTouching) 100 else 500),
        label = "tear",
    )

    // Tick — advance all streams + trails
    LaunchedEffect(Unit) {
        while (true) {
            delay(38)
            tick++
            for (s in allStreams) {
                s.yOffset += s.speed
                if (Random.nextInt(4) == 0) {
                    s.chars[Random.nextInt(s.chars.size)] = if (Random.nextBoolean()) '0' else '1'
                }
            }
            val iter = trail.iterator()
            while (iter.hasNext()) {
                val p = iter.next()
                p.life -= 0.03f
                if (p.life <= 0f) iter.remove()
            }
        }
    }

    LaunchedEffect(isTouching, touchX, touchY) {
        if (isTouching) {
            trail.add(TrailPt(touchX, touchY))
            while (trail.size > 40) trail.removeFirst()
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cw = size.width
        val ch = size.height

        @Suppress("UNUSED_EXPRESSION")
        tick

        // Initialize columns on first draw (need actual pixel size)
        if (initialized == 0L) {
            initialized = 1L
            columns.clear()
            allStreams.clear()
            val numCols = (cw / CELL_W).toInt() + 1
            for (c in 0 until numCols) {
                val s1 = Stream.make(ch)
                val s2 = Stream.make(ch).also { it.yOffset -= ch * 0.6f } // offset second stream
                allStreams.add(s1)
                allStreams.add(s2)
                columns.add(MatrixCol(c, listOf(s1, s2)))
            }
        }

        val hasTear = tearRadius > 2f || trail.isNotEmpty()

        // --- Only draw chars inside tear zones, no backdrop ---
        if (!hasTear) return@Canvas

        for (mc in columns) {
            val x = mc.colIndex * CELL_W

            for (stream in mc.streams) {
                val totalH = stream.len * CELL_H
                val wrapH = ch + totalH + 200f
                val baseY = ((stream.yOffset % wrapH) + wrapH) % wrapH - totalH

                for (i in 0 until stream.len) {
                    val cy = baseY + i * CELL_H
                    if (cy < -CELL_H || cy > ch + CELL_H) continue

                    val isHead = i >= stream.len - 2

                    // Only visible inside tear radius
                    var tearAlpha = 0f
                    if (tearRadius > 2f) {
                        val d = hypot(x - touchX, cy - touchY)
                        if (d < tearRadius * 0.88f) {
                            val e = 1f - (d / (tearRadius * 0.88f))
                            tearAlpha = (e * 1.8f).coerceAtMost(1f)
                        }
                    }
                    for (pt in trail) {
                        val r = tearRadius * pt.life * 0.65f
                        if (r < 2f) continue
                        val d = hypot(x - pt.x, cy - pt.y)
                        if (d < r * 0.88f) {
                            val e = 1f - (d / (r * 0.88f))
                            val b = (e * 1.5f * pt.life).coerceAtMost(1f)
                            if (b > tearAlpha) tearAlpha = b
                        }
                    }

                    if (tearAlpha < 0.05f) continue

                    // Dark green on white — no black backdrop needed
                    val color = if (isHead) Color(0xFF006400) else Color(0xFF008F11)

                    val result = textMeasurer.measure(
                        stream.chars[i].toString(),
                        TextStyle(
                            fontSize = 14.sp,
                            fontWeight = if (isHead) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp,
                        ),
                    )
                    drawText(result, color.copy(alpha = tearAlpha), Offset(x, cy))
                }
            }
        }
    }
}
