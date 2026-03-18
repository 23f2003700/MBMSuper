package com.mbm.superapp.features.games.ludo

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBounce
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

data class LudoPiece(val player: Int, val index: Int, var position: Int = -1) {
    val isHome get() = position == -1
    val isFinished get() = position >= 56
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LudoScreen(onBack: () -> Unit) {
    var numPlayers by remember { mutableIntStateOf(2) }
    var currentPlayer by remember { mutableIntStateOf(0) }
    var diceValue by remember { mutableIntStateOf(0) }
    var hasRolled by remember { mutableStateOf(false) }
    var pieces by remember {
        mutableStateOf(Array(4) { p -> Array(4) { i -> LudoPiece(p, i) } })
    }
    var statusText by remember { mutableStateOf("Player 1: Roll the dice!") }
    var gameOver by remember { mutableStateOf(false) }

    val diceAnim = remember { Animatable(1f) }
    val textMeasurer = rememberTextMeasurer()

    val p1Color = MaterialTheme.colorScheme.primary
    val p2Color = MaterialTheme.colorScheme.onBackground
    val p3Color = MaterialTheme.colorScheme.secondary
    val p4Color = MaterialTheme.colorScheme.tertiary
    val playerColors = listOf(p1Color, p2Color, p3Color, p4Color)
    val playerNames = listOf("Player 1", "Player 2", "Player 3", "Player 4")

    fun rollDice() {
        if (hasRolled || gameOver) return
        diceValue = Random.nextInt(1, 7)
        hasRolled = true

        // Check if player can make any move
        val playerPieces = pieces[currentPlayer]
        val canMove = playerPieces.any { piece ->
            if (piece.isFinished) false
            else if (piece.isHome) diceValue == 6
            else piece.position + diceValue <= 56
        }

        if (!canMove) {
            // Auto-skip turn
            hasRolled = false
            if (diceValue != 6) {
                currentPlayer = (currentPlayer + 1) % numPlayers
            }
            statusText = "${playerNames[currentPlayer]}: Roll the dice!"
        } else {
            statusText = "${playerNames[currentPlayer]}: Move a piece"
        }
    }

    fun movePiece(pieceIdx: Int) {
        if (!hasRolled || gameOver) return
        val piece = pieces[currentPlayer][pieceIdx]

        if (piece.isFinished) return
        if (piece.isHome && diceValue != 6) return
        if (!piece.isHome && piece.position + diceValue > 56) return

        val newPieces = Array(4) { p -> Array(4) { i -> pieces[p][i].copy() } }

        if (piece.isHome) {
            newPieces[currentPlayer][pieceIdx] = piece.copy(position = 0)
        } else {
            newPieces[currentPlayer][pieceIdx] = piece.copy(position = piece.position + diceValue)
        }

        // Check if finished
        if (newPieces[currentPlayer].all { it.isFinished }) {
            statusText = "${playerNames[currentPlayer]} Wins!"
            gameOver = true
        }

        pieces = newPieces
        hasRolled = false

        if (!gameOver) {
            if (diceValue != 6) {
                currentPlayer = (currentPlayer + 1) % numPlayers
            }
            statusText = "${playerNames[currentPlayer]}: Roll the dice!"
        }
    }

    // Dice animation
    LaunchedEffect(diceValue) {
        if (diceValue > 0) {
            diceAnim.snapTo(0.5f)
            diceAnim.animateTo(1f, tween(400, easing = EaseOutBounce))
        }
    }

    fun resetGame() {
        pieces = Array(4) { p -> Array(4) { i -> LudoPiece(p, i) } }
        currentPlayer = 0
        diceValue = 0
        hasRolled = false
        gameOver = false
        statusText = "Player 1: Roll the dice!"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ludo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Player count
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (n in 2..4) {
                    FilterChip(
                        selected = numPlayers == n,
                        onClick = { numPlayers = n; resetGame() },
                        label = { Text("$n Players") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        ),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = playerColors[currentPlayer],
            )

            Spacer(Modifier.height(12.dp))

            // Dice display
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .pointerInput(hasRolled, gameOver) {
                        detectTapGestures { rollDice() }
                    },
                contentAlignment = Alignment.Center,
            ) {
                val diceColor = MaterialTheme.colorScheme.onBackground

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val s = size.width * diceAnim.value
                    val pad = (size.width - s) / 2
                    drawRoundRect(
                        color = diceColor.copy(alpha = 0.1f),
                        topLeft = Offset(pad, pad),
                        size = Size(s, s),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
                        style = Stroke(width = 2.dp.toPx()),
                    )

                    if (diceValue > 0) {
                        val dotR = s * 0.08f
                        val cx = size.width / 2
                        val cy = size.height / 2
                        val off = s * 0.25f

                        val positions = when (diceValue) {
                            1 -> listOf(cx to cy)
                            2 -> listOf(cx - off to cy - off, cx + off to cy + off)
                            3 -> listOf(cx - off to cy - off, cx to cy, cx + off to cy + off)
                            4 -> listOf(cx - off to cy - off, cx + off to cy - off, cx - off to cy + off, cx + off to cy + off)
                            5 -> listOf(cx - off to cy - off, cx + off to cy - off, cx to cy, cx - off to cy + off, cx + off to cy + off)
                            6 -> listOf(cx - off to cy - off, cx + off to cy - off, cx - off to cy, cx + off to cy, cx - off to cy + off, cx + off to cy + off)
                            else -> emptyList()
                        }
                        positions.forEach { (x, y) ->
                            drawCircle(diceColor, dotR, Offset(x, y))
                        }
                    }
                }
            }

            Text(
                text = if (diceValue > 0) "Tap to roll" else "Tap dice to start",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            )

            Spacer(Modifier.height(16.dp))

            // Simplified board - show piece positions
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (p in 0 until numPlayers) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = playerNames[p],
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (currentPlayer == p) FontWeight.Bold else FontWeight.Normal,
                            ),
                            color = playerColors[p],
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (i in 0..3) {
                                val piece = pieces[p][i]
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .pointerInput(piece, hasRolled, currentPlayer, gameOver) {
                                            detectTapGestures {
                                                if (currentPlayer == p) movePiece(i)
                                            }
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val color = playerColors[p]
                                        if (piece.isFinished) {
                                            drawCircle(color, radius = size.width * 0.35f, style = Fill)
                                            val check = textMeasurer.measure("✓", TextStyle(fontSize = 14.sp))
                                            drawText(check, Color.Black, Offset(
                                                (size.width - check.size.width) / 2,
                                                (size.height - check.size.height) / 2,
                                            ))
                                        } else if (piece.isHome) {
                                            drawCircle(color.copy(alpha = 0.3f), radius = size.width * 0.35f, style = Fill)
                                            drawCircle(color, radius = size.width * 0.35f, style = Stroke(width = 2.dp.toPx()))
                                        } else {
                                            drawCircle(color, radius = size.width * 0.35f, style = Fill)
                                            val posText = textMeasurer.measure("${piece.position}", TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold))
                                            drawText(posText, Color.Black, Offset(
                                                (size.width - posText.size.width) / 2,
                                                (size.height - posText.size.height) / 2,
                                            ))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Track visualization
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                val trackLen = 57 // positions 0-56
                val cellW = size.width / trackLen
                val cellH = size.height / numPlayers

                // Draw track
                for (pos in 0 until trackLen) {
                    val x = pos * cellW
                    drawRect(
                        Color.Gray.copy(alpha = if (pos % 5 == 0) 0.15f else 0.05f),
                        Offset(x, 0f),
                        Size(cellW, size.height),
                    )
                }

                // Draw pieces on track
                for (p in 0 until numPlayers) {
                    for (piece in pieces[p]) {
                        if (!piece.isHome && !piece.isFinished) {
                            val x = piece.position * cellW + cellW / 2
                            val y = p * cellH + cellH / 2
                            drawCircle(playerColors[p], radius = cellH * 0.35f, center = Offset(x, y))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { resetGame() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text("New Game")
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}
