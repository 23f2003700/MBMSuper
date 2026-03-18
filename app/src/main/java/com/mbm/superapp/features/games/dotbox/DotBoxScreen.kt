package com.mbm.superapp.features.games.dotbox

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.abs

data class DotBoxLine(val r1: Int, val c1: Int, val r2: Int, val c2: Int, val player: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DotBoxScreen(onBack: () -> Unit) {
    val gridSize = 5 // 5x5 dots = 4x4 boxes
    var numPlayers by remember { mutableIntStateOf(2) }
    var currentPlayer by remember { mutableIntStateOf(0) }
    var lines by remember { mutableStateOf(setOf<String>()) }
    var lineOwners by remember { mutableStateOf(mapOf<String, Int>()) }
    var boxOwners by remember { mutableStateOf(mapOf<String, Int>()) }
    var scores by remember { mutableStateOf(IntArray(4) { 0 }) }
    var gameOver by remember { mutableStateOf(false) }

    val p1Color = MaterialTheme.colorScheme.primary
    val p2Color = MaterialTheme.colorScheme.onBackground
    val p3Color = MaterialTheme.colorScheme.secondary
    val p4Color = MaterialTheme.colorScheme.tertiary
    val playerColors = listOf(p1Color, p2Color, p3Color, p4Color)
    val playerNames = listOf("P1", "P2", "P3", "P4")

    fun lineKey(r1: Int, c1: Int, r2: Int, c2: Int): String {
        return if (r1 < r2 || (r1 == r2 && c1 < c2)) "$r1,$c1-$r2,$c2" else "$r2,$c2-$r1,$c1"
    }

    fun boxKey(r: Int, c: Int) = "box_$r,$c"

    fun checkBoxCompletion(newLine: String): Int {
        var completed = 0
        // Determine which boxes this line could complete
        // Parse the line
        val parts = newLine.split("-")
        val (r1s, c1s) = parts[0].split(",").map { it.toInt() }
        val (r2s, c2s) = parts[1].split(",").map { it.toInt() }

        val isHorizontal = r1s == r2s
        val isVertical = c1s == c2s

        if (isHorizontal) {
            val r = r1s
            val cMin = minOf(c1s, c2s)
            // Box above: (r-1, cMin) if r > 0
            if (r > 0) {
                val top = lineKey(r - 1, cMin, r - 1, cMin + 1)
                val left = lineKey(r - 1, cMin, r, cMin)
                val right = lineKey(r - 1, cMin + 1, r, cMin + 1)
                val bottom = newLine
                if (top in lines && left in lines && right in lines) {
                    val bk = boxKey(r - 1, cMin)
                    if (bk !in boxOwners) {
                        boxOwners = boxOwners + (bk to currentPlayer)
                        completed++
                    }
                }
            }
            // Box below: (r, cMin) if r < gridSize-1
            if (r < gridSize - 1) {
                val top = newLine
                val left = lineKey(r, cMin, r + 1, cMin)
                val right = lineKey(r, cMin + 1, r + 1, cMin + 1)
                val bottom = lineKey(r + 1, cMin, r + 1, cMin + 1)
                if (left in lines && right in lines && bottom in lines) {
                    val bk = boxKey(r, cMin)
                    if (bk !in boxOwners) {
                        boxOwners = boxOwners + (bk to currentPlayer)
                        completed++
                    }
                }
            }
        } else if (isVertical) {
            val c = c1s
            val rMin = minOf(r1s, r2s)
            // Box left: (rMin, c-1) if c > 0
            if (c > 0) {
                val top = lineKey(rMin, c - 1, rMin, c)
                val left = lineKey(rMin, c - 1, rMin + 1, c - 1)
                val right = newLine
                val bottom = lineKey(rMin + 1, c - 1, rMin + 1, c)
                if (top in lines && left in lines && bottom in lines) {
                    val bk = boxKey(rMin, c - 1)
                    if (bk !in boxOwners) {
                        boxOwners = boxOwners + (bk to currentPlayer)
                        completed++
                    }
                }
            }
            // Box right: (rMin, c) if c < gridSize-1
            if (c < gridSize - 1) {
                val top = lineKey(rMin, c, rMin, c + 1)
                val left = newLine
                val right = lineKey(rMin, c + 1, rMin + 1, c + 1)
                val bottom = lineKey(rMin + 1, c, rMin + 1, c + 1)
                if (top in lines && right in lines && bottom in lines) {
                    val bk = boxKey(rMin, c)
                    if (bk !in boxOwners) {
                        boxOwners = boxOwners + (bk to currentPlayer)
                        completed++
                    }
                }
            }
        }
        return completed
    }

    fun placeLine(r1: Int, c1: Int, r2: Int, c2: Int) {
        val key = lineKey(r1, c1, r2, c2)
        if (key in lines || gameOver) return

        lines = lines + key
        lineOwners = lineOwners + (key to currentPlayer)

        val completed = checkBoxCompletion(key)
        if (completed > 0) {
            val newScores = scores.copyOf()
            newScores[currentPlayer] += completed
            scores = newScores
        }

        // Check game over
        val totalBoxes = (gridSize - 1) * (gridSize - 1)
        if (boxOwners.size >= totalBoxes) {
            gameOver = true
            return
        }

        // If no box completed, next player's turn
        if (completed == 0) {
            currentPlayer = (currentPlayer + 1) % numPlayers
        }
    }

    fun resetGame() {
        lines = setOf()
        lineOwners = mapOf()
        boxOwners = mapOf()
        scores = IntArray(4) { 0 }
        currentPlayer = 0
        gameOver = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dots & Boxes") },
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
            // Player count selection
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

            // Scores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                for (i in 0 until numPlayers) {
                    val isActive = currentPlayer == i && !gameOver
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = playerNames[i],
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            ),
                            color = if (isActive) playerColors[i] else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        )
                        Text(
                            text = "${scores[i]}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = playerColors[i],
                        )
                    }
                }
            }

            if (gameOver) {
                Spacer(Modifier.height(8.dp))
                val maxScore = scores.take(numPlayers).max()
                val winnerIdx = scores.take(numPlayers).indexOfFirst { it == maxScore }
                Text(
                    text = "${playerNames[winnerIdx]} Wins!",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = playerColors[winnerIdx],
                )
            }

            Spacer(Modifier.height(16.dp))

            // Game board
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .aspectRatio(1f)
                    .pointerInput(lines, gameOver) {
                        detectTapGestures { offset ->
                            if (gameOver) return@detectTapGestures
                            val cellW = size.width.toFloat() / (gridSize - 1)
                            val cellH = size.height.toFloat() / (gridSize - 1)

                            // Find nearest line
                            var bestDist = Float.MAX_VALUE
                            var bestLine: Pair<Pair<Int, Int>, Pair<Int, Int>>? = null

                            // Horizontal lines
                            for (r in 0 until gridSize) {
                                for (c in 0 until gridSize - 1) {
                                    val x1 = c * cellW
                                    val x2 = (c + 1) * cellW
                                    val y = r * cellH
                                    val midX = (x1 + x2) / 2
                                    val dist = abs(offset.x - midX) + abs(offset.y - y) * 2
                                    if (dist < bestDist && dist < cellW) {
                                        bestDist = dist
                                        bestLine = Pair(Pair(r, c), Pair(r, c + 1))
                                    }
                                }
                            }
                            // Vertical lines
                            for (r in 0 until gridSize - 1) {
                                for (c in 0 until gridSize) {
                                    val x = c * cellW
                                    val y1 = r * cellH
                                    val y2 = (r + 1) * cellH
                                    val midY = (y1 + y2) / 2
                                    val dist = abs(offset.x - x) * 2 + abs(offset.y - midY)
                                    if (dist < bestDist && dist < cellH) {
                                        bestDist = dist
                                        bestLine = Pair(Pair(r, c), Pair(r + 1, c))
                                    }
                                }
                            }

                            bestLine?.let { (p1, p2) ->
                                placeLine(p1.first, p1.second, p2.first, p2.second)
                            }
                        }
                    },
            ) {
                val surfaceColor = MaterialTheme.colorScheme.onBackground
                val dotColor = surfaceColor
                val emptyLineColor = surfaceColor.copy(alpha = 0.08f)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cellW = size.width / (gridSize - 1)
                    val cellH = size.height / (gridSize - 1)

                    // Draw box fills
                    for ((key, player) in boxOwners) {
                        val parts = key.removePrefix("box_").split(",")
                        val r = parts[0].toInt()
                        val c = parts[1].toInt()
                        drawRect(
                            color = playerColors[player].copy(alpha = 0.15f),
                            topLeft = Offset(c * cellW, r * cellH),
                            size = androidx.compose.ui.geometry.Size(cellW, cellH),
                        )
                    }

                    // Draw empty lines (hint lines)
                    // Horizontal
                    for (r in 0 until gridSize) {
                        for (c in 0 until gridSize - 1) {
                            val key = lineKey(r, c, r, c + 1)
                            if (key !in lines) {
                                drawLine(
                                    emptyLineColor,
                                    Offset(c * cellW, r * cellH),
                                    Offset((c + 1) * cellW, r * cellH),
                                    strokeWidth = 2.dp.toPx(),
                                    cap = StrokeCap.Round,
                                )
                            }
                        }
                    }
                    // Vertical
                    for (r in 0 until gridSize - 1) {
                        for (c in 0 until gridSize) {
                            val key = lineKey(r, c, r + 1, c)
                            if (key !in lines) {
                                drawLine(
                                    emptyLineColor,
                                    Offset(c * cellW, r * cellH),
                                    Offset(c * cellW, (r + 1) * cellH),
                                    strokeWidth = 2.dp.toPx(),
                                    cap = StrokeCap.Round,
                                )
                            }
                        }
                    }

                    // Draw placed lines
                    for ((key, player) in lineOwners) {
                        val parts = key.split("-")
                        val (r1, c1) = parts[0].split(",").map { it.toInt() }
                        val (r2, c2) = parts[1].split(",").map { it.toInt() }
                        drawLine(
                            playerColors[player],
                            Offset(c1 * cellW, r1 * cellH),
                            Offset(c2 * cellW, r2 * cellH),
                            strokeWidth = 4.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }

                    // Draw dots
                    for (r in 0 until gridSize) {
                        for (c in 0 until gridSize) {
                            drawCircle(
                                dotColor,
                                radius = 5.dp.toPx(),
                                center = Offset(c * cellW, r * cellH),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

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
