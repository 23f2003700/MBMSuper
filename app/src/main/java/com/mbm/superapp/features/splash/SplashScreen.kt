package com.mbm.superapp.features.splash

import android.graphics.PathMeasure as AndroidPathMeasure
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.mbm.superapp.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    // Animation progress for each letter (0f..1f)
    val m1Progress = remember { Animatable(0f) }
    val bProgress = remember { Animatable(0f) }
    val m2Progress = remember { Animatable(0f) }

    // Glow alpha for each letter
    val m1Glow = remember { Animatable(0f) }
    val bGlow = remember { Animatable(0f) }
    val m2Glow = remember { Animatable(0f) }

    // Logo animation
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.5f) }

    // "Super" text
    val superAlpha = remember { Animatable(0f) }
    val superScale = remember { Animatable(0.8f) }

    // Underline
    val underlineProgress = remember { Animatable(0f) }

    // Overall fade-out
    val screenAlpha = remember { Animatable(1f) }

    // Subtle background grain/noise dots
    val bgPulse = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Logo pop-in first
        launch { logoAlpha.animateTo(1f, tween(500, easing = EaseOutCubic)) }
        launch { logoScale.animateTo(1f, tween(600, easing = EaseOutCubic)) }
        delay(400)

        // Background subtle pulse
        launch {
            bgPulse.animateTo(1f, tween(3500, easing = LinearEasing))
        }

        // Draw M1
        launch { m1Progress.animateTo(1f, tween(650, easing = EaseOutCubic)) }
        delay(350)
        // M1 glow flash
        launch {
            m1Glow.animateTo(0.5f, tween(180))
            m1Glow.animateTo(0.08f, tween(400))
        }
        delay(150)

        // Draw B
        launch { bProgress.animateTo(1f, tween(650, easing = EaseOutCubic)) }
        delay(350)
        // B glow flash
        launch {
            bGlow.animateTo(0.5f, tween(180))
            bGlow.animateTo(0.08f, tween(400))
        }
        delay(150)

        // Draw M2
        launch { m2Progress.animateTo(1f, tween(650, easing = EaseOutCubic)) }
        delay(350)
        // M2 glow flash
        launch {
            m2Glow.animateTo(0.5f, tween(180))
            m2Glow.animateTo(0.08f, tween(400))
        }
        delay(200)

        // Reveal "Super" text
        launch { superAlpha.animateTo(1f, tween(500, easing = EaseOutCubic)) }
        launch { superScale.animateTo(1f, tween(500, easing = EaseOutCubic)) }
        delay(250)

        // Draw underline
        launch { underlineProgress.animateTo(1f, tween(450, easing = EaseOutQuad)) }
        delay(700)

        // Fade out and navigate
        screenAlpha.animateTo(0f, tween(400))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2EE)) // Warm off-white (old TV phosphor)
            .alpha(screenAlpha.value),
        contentAlignment = Alignment.Center,
    ) {
        // Subtle scan-line overlay effect
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lineSpacing = 4f
            val alpha = 0.03f * bgPulse.value
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = Color.Black.copy(alpha = alpha),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f,
                )
                y += lineSpacing
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Logo
            Image(
                painter = painterResource(R.drawable.mbm_logo),
                contentDescription = "MBM Logo",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .alpha(logoAlpha.value)
                    .scale(logoScale.value),
            )

            Spacer(Modifier.height(16.dp))

            // MBM letters canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(160.dp)
            ) {
                val sx = size.width / 500f
                val sy = size.height / 200f
                val strokeW = 13f * sx
                val glowW = 30f * sx

                // Build paths
                val m1 = buildM1Path(sx, sy)
                val b = buildBPath(sx, sy)
                val m2 = buildM2Path(sx, sy)

                val m1Len = measurePath(m1)
                val bLen = measurePath(b)
                val m2Len = measurePath(m2)

                val textCol = Color(0xFF0A0A0A) // Near-black
                val glowCol = Color.White

                // Draw glows (behind text)
                drawAnimatedStroke(m1, m1Len, m1Progress.value, glowCol.copy(alpha = m1Glow.value), glowW)
                drawAnimatedStroke(b, bLen, bProgress.value, glowCol.copy(alpha = bGlow.value), glowW)
                drawAnimatedStroke(m2, m2Len, m2Progress.value, glowCol.copy(alpha = m2Glow.value), glowW)

                // Draw main letter strokes
                drawAnimatedStroke(m1, m1Len, m1Progress.value, textCol, strokeW)
                drawAnimatedStroke(b, bLen, bProgress.value, textCol, strokeW)
                drawAnimatedStroke(m2, m2Len, m2Progress.value, textCol, strokeW)

                // Underline
                if (underlineProgress.value > 0f) {
                    val lineY = 185f * sy
                    val startX = 75f * sx
                    val endX = startX + (350f * sx) * underlineProgress.value
                    // Glow
                    drawLine(
                        color = glowCol.copy(alpha = 0.2f),
                        start = Offset(startX, lineY),
                        end = Offset(endX, lineY),
                        strokeWidth = 8f * sx,
                        cap = StrokeCap.Round,
                    )
                    // Main line
                    drawLine(
                        color = textCol.copy(alpha = 0.6f),
                        start = Offset(startX, lineY),
                        end = Offset(endX, lineY),
                        strokeWidth = 3f * sx,
                        cap = StrokeCap.Round,
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // "Super" text
            Text(
                text = "Super",
                style = TextStyle(
                    fontSize = 38.sp,
                    fontWeight = FontWeight.W200,
                    letterSpacing = 14.sp,
                    color = Color(0xFF0A0A0A).copy(alpha = superAlpha.value),
                ),
                modifier = Modifier
                    .alpha(superAlpha.value)
                    .padding(start = 8.dp),
            )

            Spacer(Modifier.height(48.dp))

            // Subtle tagline
            Text(
                text = "me being me",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W300,
                    letterSpacing = 3.sp,
                    color = Color(0xFF666666).copy(alpha = superAlpha.value * 0.7f),
                ),
            )
        }
    }
}

// --- Path builders (coordinate system: 500 x 200) ---

private fun buildM1Path(sx: Float, sy: Float): Path = Path().apply {
    moveTo(80f * sx, 170f * sy)
    lineTo(80f * sx, 30f * sy)
    lineTo(130f * sx, 125f * sy)
    lineTo(180f * sx, 30f * sy)
    lineTo(180f * sx, 170f * sy)
}

private fun buildBPath(sx: Float, sy: Float): Path = Path().apply {
    moveTo(220f * sx, 170f * sy)
    lineTo(220f * sx, 30f * sy)
    lineTo(275f * sx, 65f * sy)
    lineTo(220f * sx, 100f * sy)
    lineTo(280f * sx, 140f * sy)
    lineTo(220f * sx, 170f * sy)
}

private fun buildM2Path(sx: Float, sy: Float): Path = Path().apply {
    moveTo(320f * sx, 170f * sy)
    lineTo(320f * sx, 30f * sy)
    lineTo(370f * sx, 125f * sy)
    lineTo(420f * sx, 30f * sy)
    lineTo(420f * sx, 170f * sy)
}

private fun measurePath(path: Path): Float {
    val pm = AndroidPathMeasure(path.asAndroidPath(), false)
    return pm.length
}

private fun DrawScope.drawAnimatedStroke(
    path: Path,
    totalLength: Float,
    progress: Float,
    color: Color,
    strokeWidth: Float,
) {
    if (progress <= 0f || totalLength <= 0f) return
    val drawLen = totalLength * progress
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(drawLen, totalLength),
                phase = 0f,
            ),
        ),
    )
}
