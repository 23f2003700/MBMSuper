package com.mbm.superapp.features.games.catchcat

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private enum class CatState { STILL, ALERT, YAWN, SLEEP, ITCH, SCRATCH, RUN }
private enum class Dir { N, NE, E, SE, S, SW, W, NW }

private val idleSequence = listOf(
    CatState.STILL, CatState.STILL, CatState.YAWN, CatState.STILL,
    CatState.ITCH, CatState.SCRATCH, CatState.STILL, CatState.SLEEP,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatChaseScreen(onBack: () -> Unit) {
    var catX by remember { mutableFloatStateOf(400f) }
    var catY by remember { mutableFloatStateOf(600f) }
    var targetX by remember { mutableFloatStateOf(400f) }
    var targetY by remember { mutableFloatStateOf(600f) }
    var following by remember { mutableStateOf(false) }
    var catState by remember { mutableStateOf(CatState.STILL) }
    var dir by remember { mutableStateOf(Dir.S) }
    var frame by remember { mutableIntStateOf(0) }
    var idleCycle by remember { mutableIntStateOf(0) }
    var idleTicks by remember { mutableIntStateOf(0) }
    var catchCount by remember { mutableIntStateOf(0) }

    val catSize = 64f
    val speed = 12f

    // Animation timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(120)
            frame = (frame + 1) % 2

            if (following && catState != CatState.RUN) {
                catState = CatState.RUN
            }

            if (catState == CatState.RUN && following) {
                val dx = targetX - catX
                val dy = targetY - catY
                val dist = hypot(dx.toDouble(), dy.toDouble()).toFloat()

                if (dist < catSize / 2) {
                    following = false
                    catchCount++
                    catState = CatState.ALERT
                    idleTicks = 0
                    idleCycle = 0
                } else {
                    val angle = atan2(dy.toDouble(), dx.toDouble())
                    catX += (cos(angle) * speed).toFloat()
                    catY += (sin(angle) * speed).toFloat()
                    dir = angleToDir(angle)
                }
            } else if (!following) {
                idleTicks++
                if (idleTicks >= 8) {
                    idleTicks = 0
                    idleCycle = (idleCycle + 1) % idleSequence.size
                    catState = idleSequence[idleCycle]
                }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "cat")
    val breathe by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Reverse),
        label = "breathe",
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cat Chase") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        catX = 400f; catY = 600f; targetX = 400f; targetY = 600f
                        following = false; catState = CatState.STILL; catchCount = 0
                    }) {
                        Icon(Icons.Outlined.Refresh, "Reset")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Score
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Pets, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Tap anywhere and the cat will chase!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Text("Catches: $catchCount", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                }
            }

            // Game arena
            val primaryColor = MaterialTheme.colorScheme.primary
            val surfaceColor = MaterialTheme.colorScheme.surface
            val onSurfaceColor = MaterialTheme.colorScheme.onSurface

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(surfaceColor, RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            targetX = offset.x
                            targetY = offset.y
                            following = true
                            catState = CatState.ALERT
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            targetX = change.position.x
                            targetY = change.position.y
                            following = true
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw target marker
                    if (following) {
                        drawCircle(
                            primaryColor.copy(alpha = 0.15f),
                            radius = 30f,
                            center = Offset(targetX, targetY),
                        )
                        drawCircle(
                            primaryColor.copy(alpha = 0.4f),
                            radius = 6f,
                            center = Offset(targetX, targetY),
                        )
                    }

                    // Draw cat
                    drawCat(
                        cx = catX,
                        cy = catY,
                        size = catSize,
                        state = catState,
                        dir = dir,
                        frame = frame,
                        breathe = breathe,
                        bodyColor = primaryColor,
                        outlineColor = onSurfaceColor,
                    )

                    // Draw paw prints trail (subtle)
                    if (catState == CatState.SLEEP) {
                        val zzz = "z Z z"
                        // sleeping Zzz drawn as small circles
                        for (i in 0..2) {
                            val zx = catX + catSize / 2 + i * 8f
                            val zy = catY - catSize / 2 - i * 12f - breathe * 6f
                            drawCircle(
                                onSurfaceColor.copy(alpha = 0.2f - i * 0.05f),
                                radius = 3f + i * 1.5f,
                                center = Offset(zx, zy),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun angleToDir(angle: Double): Dir {
    val deg = (angle * 180 / PI + 360) % 360
    return when {
        deg < 22.5 || deg >= 337.5 -> Dir.E
        deg < 67.5 -> Dir.SE
        deg < 112.5 -> Dir.S
        deg < 157.5 -> Dir.SW
        deg < 202.5 -> Dir.W
        deg < 247.5 -> Dir.NW
        deg < 292.5 -> Dir.N
        else -> Dir.NE
    }
}

private fun DrawScope.drawCat(
    cx: Float, cy: Float, size: Float,
    state: CatState, dir: Dir, frame: Int, breathe: Float,
    bodyColor: Color, outlineColor: Color,
) {
    val half = size / 2
    val bodyW = half * 0.8f
    val bodyH = half * 0.6f

    // Body direction offset
    val facing = when (dir) {
        Dir.E, Dir.NE, Dir.SE -> 1f
        Dir.W, Dir.NW, Dir.SW -> -1f
        else -> 0f
    }

    val bodyBounce = when (state) {
        CatState.RUN -> if (frame == 0) -3f else 3f
        CatState.ITCH -> if (frame == 0) -2f else 2f
        CatState.SCRATCH -> if (frame == 0) -2f else 2f
        CatState.SLEEP -> breathe * 2f
        CatState.YAWN -> breathe * 3f
        else -> 0f
    }

    // Shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.1f),
        topLeft = Offset(cx - bodyW, cy + bodyH * 0.6f),
        size = Size(bodyW * 2f, bodyH * 0.5f),
    )

    // Body
    drawOval(
        color = bodyColor,
        topLeft = Offset(cx - bodyW + facing * 4f, cy - bodyH + bodyBounce),
        size = Size(bodyW * 2f, bodyH * 2f),
    )
    drawOval(
        color = outlineColor.copy(alpha = 0.3f),
        topLeft = Offset(cx - bodyW + facing * 4f, cy - bodyH + bodyBounce),
        size = Size(bodyW * 2f, bodyH * 2f),
        style = Stroke(width = 1.5f),
    )

    // Head
    val headR = half * 0.55f
    val headX = cx + facing * bodyW * 0.4f
    val headY = cy - bodyH * 0.7f + bodyBounce
    drawCircle(
        color = bodyColor,
        radius = headR,
        center = Offset(headX, headY),
    )
    drawCircle(
        color = outlineColor.copy(alpha = 0.3f),
        radius = headR,
        center = Offset(headX, headY),
        style = Stroke(width = 1.5f),
    )

    // Ears (triangles)
    val earSize = headR * 0.55f
    for (side in listOf(-1f, 1f)) {
        val earPath = Path().apply {
            moveTo(headX + side * headR * 0.5f, headY - headR * 0.3f)
            lineTo(headX + side * headR * 0.9f, headY - headR - earSize)
            lineTo(headX + side * headR * 0.15f, headY - headR * 0.7f)
            close()
        }
        drawPath(earPath, bodyColor, style = Fill)
        drawPath(earPath, outlineColor.copy(alpha = 0.3f), style = Stroke(width = 1.5f))
        // Inner ear
        val innerPath = Path().apply {
            moveTo(headX + side * headR * 0.5f, headY - headR * 0.4f)
            lineTo(headX + side * headR * 0.78f, headY - headR - earSize * 0.6f)
            lineTo(headX + side * headR * 0.25f, headY - headR * 0.65f)
            close()
        }
        drawPath(innerPath, Color(0xFFFFB6C1).copy(alpha = 0.5f), style = Fill)
    }

    // Eyes
    val eyeOpen = when (state) {
        CatState.SLEEP -> 0f
        CatState.YAWN -> 0.5f
        CatState.ALERT -> 1.2f
        else -> 1f
    }
    for (side in listOf(-1f, 1f)) {
        val ex = headX + side * headR * 0.32f
        val ey = headY - headR * 0.05f
        if (eyeOpen > 0.1f) {
            // Eye white
            drawOval(
                Color.White,
                topLeft = Offset(ex - 4f, ey - 3f * eyeOpen),
                size = Size(8f, 6f * eyeOpen),
            )
            // Pupil
            drawCircle(outlineColor, 2f * eyeOpen.coerceAtMost(1f), Offset(ex + facing * 1.5f, ey))
        } else {
            // Closed eyes (line)
            drawLine(outlineColor.copy(alpha = 0.5f), Offset(ex - 4f, ey), Offset(ex + 4f, ey), 1.5f)
        }
    }

    // Nose
    val noseY = headY + headR * 0.2f
    drawCircle(Color(0xFFFF6B8A), 2.5f, Offset(headX, noseY))

    // Mouth
    if (state == CatState.YAWN) {
        drawOval(
            Color(0xFFFF6B8A),
            topLeft = Offset(headX - 5f, noseY + 1f),
            size = Size(10f, 8f * breathe.coerceAtLeast(0.3f)),
        )
    } else {
        // Small "w" mouth
        drawLine(outlineColor.copy(alpha = 0.4f), Offset(headX - 4f, noseY + 3f), Offset(headX, noseY + 5f), 1f)
        drawLine(outlineColor.copy(alpha = 0.4f), Offset(headX, noseY + 5f), Offset(headX + 4f, noseY + 3f), 1f)
    }

    // Whiskers
    for (side in listOf(-1f, 1f)) {
        for (angle in listOf(-15f, 0f, 15f)) {
            val wx = headX + side * headR * 0.7f
            val wy = noseY + angle * 0.1f
            val rad = (angle * PI / 180).toFloat()
            drawLine(
                outlineColor.copy(alpha = 0.2f),
                Offset(wx, wy),
                Offset(wx + side * 14f * cos(rad), wy + 14f * sin(rad)),
                0.8f,
            )
        }
    }

    // Tail
    val tailStartX = cx - facing * bodyW * 0.6f
    val tailStartY = cy + bodyH * 0.2f + bodyBounce
    val tailWag = if (state == CatState.RUN) sin(frame * PI).toFloat() * 10f else sin(breathe * PI).toFloat() * 5f
    val tailPath = Path().apply {
        moveTo(tailStartX, tailStartY)
        cubicTo(
            tailStartX - facing * 20f, tailStartY - 15f + tailWag,
            tailStartX - facing * 30f, tailStartY - 30f + tailWag,
            tailStartX - facing * 25f, tailStartY - 40f + tailWag,
        )
    }
    drawPath(tailPath, bodyColor, style = Stroke(width = 4f))
    drawPath(tailPath, outlineColor.copy(alpha = 0.3f), style = Stroke(width = 1.5f))

    // Legs (simplified)
    val legY = cy + bodyH * 0.5f + bodyBounce
    val legSpacing = bodyW * 0.5f
    for (i in listOf(-1f, -0.3f, 0.3f, 1f)) {
        val legX = cx + i * legSpacing + facing * 5f
        val legOffset = if (state == CatState.RUN) {
            if ((i > 0) == (frame == 0)) -4f else 4f
        } else 0f
        drawLine(
            outlineColor.copy(alpha = 0.3f),
            Offset(legX, legY - 4f),
            Offset(legX + legOffset, legY + 8f),
            3f,
        )
        // Paw
        drawCircle(bodyColor, 4f, Offset(legX + legOffset, legY + 10f))
    }
}
