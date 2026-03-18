package com.mbm.superapp.features.games.tictactoe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

enum class CellState { EMPTY, X, O }
enum class GameMode { TWO_PLAYER, VS_AI }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicTacToeScreen(onBack: () -> Unit) {
    var board by remember { mutableStateOf(Array(3) { Array(3) { CellState.EMPTY } }) }
    var currentPlayer by remember { mutableStateOf(CellState.X) }
    var winner by remember { mutableStateOf<CellState?>(null) }
    var isDraw by remember { mutableStateOf(false) }
    var gameMode by remember { mutableStateOf(GameMode.TWO_PLAYER) }
    var scoreX by remember { mutableIntStateOf(0) }
    var scoreO by remember { mutableIntStateOf(0) }
    var winLine by remember { mutableStateOf<Pair<Pair<Int, Int>, Pair<Int, Int>>?>(null) }
    var aiThinking by remember { mutableStateOf(false) }

    fun checkWinner(): CellState? {
        // Rows
        for (r in 0..2) {
            if (board[r][0] != CellState.EMPTY && board[r][0] == board[r][1] && board[r][1] == board[r][2]) {
                winLine = Pair(Pair(r, 0), Pair(r, 2))
                return board[r][0]
            }
        }
        // Columns
        for (c in 0..2) {
            if (board[0][c] != CellState.EMPTY && board[0][c] == board[1][c] && board[1][c] == board[2][c]) {
                winLine = Pair(Pair(0, c), Pair(2, c))
                return board[0][c]
            }
        }
        // Diagonals
        if (board[0][0] != CellState.EMPTY && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            winLine = Pair(Pair(0, 0), Pair(2, 2))
            return board[0][0]
        }
        if (board[0][2] != CellState.EMPTY && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            winLine = Pair(Pair(0, 2), Pair(2, 0))
            return board[0][2]
        }
        return null
    }

    fun minimax(b: Array<Array<CellState>>, isMax: Boolean, depth: Int): Int {
        // Check terminal states
        for (r in 0..2) {
            if (b[r][0] != CellState.EMPTY && b[r][0] == b[r][1] && b[r][1] == b[r][2])
                return if (b[r][0] == CellState.O) 10 - depth else depth - 10
        }
        for (c in 0..2) {
            if (b[0][c] != CellState.EMPTY && b[0][c] == b[1][c] && b[1][c] == b[2][c])
                return if (b[0][c] == CellState.O) 10 - depth else depth - 10
        }
        if (b[0][0] != CellState.EMPTY && b[0][0] == b[1][1] && b[1][1] == b[2][2])
            return if (b[0][0] == CellState.O) 10 - depth else depth - 10
        if (b[0][2] != CellState.EMPTY && b[0][2] == b[1][1] && b[1][1] == b[2][0])
            return if (b[0][2] == CellState.O) 10 - depth else depth - 10

        val empty = (0..2).flatMap { r -> (0..2).filter { c -> b[r][c] == CellState.EMPTY }.map { c -> r to c } }
        if (empty.isEmpty()) return 0

        return if (isMax) {
            var best = Int.MIN_VALUE
            for ((r, c) in empty) {
                b[r][c] = CellState.O
                best = maxOf(best, minimax(b, false, depth + 1))
                b[r][c] = CellState.EMPTY
            }
            best
        } else {
            var best = Int.MAX_VALUE
            for ((r, c) in empty) {
                b[r][c] = CellState.X
                best = minOf(best, minimax(b, true, depth + 1))
                b[r][c] = CellState.EMPTY
            }
            best
        }
    }

    fun aiMove() {
        val b = Array(3) { r -> Array(3) { c -> board[r][c] } }
        var bestScore = Int.MIN_VALUE
        var bestMove = Pair(0, 0)
        for (r in 0..2) {
            for (c in 0..2) {
                if (b[r][c] == CellState.EMPTY) {
                    b[r][c] = CellState.O
                    val score = minimax(b, false, 0)
                    b[r][c] = CellState.EMPTY
                    if (score > bestScore) {
                        bestScore = score
                        bestMove = Pair(r, c)
                    }
                }
            }
        }
        val newBoard = Array(3) { r -> Array(3) { c -> board[r][c] } }
        newBoard[bestMove.first][bestMove.second] = CellState.O
        board = newBoard
        winner = checkWinner()
        if (winner == null) {
            isDraw = board.all { row -> row.all { it != CellState.EMPTY } }
            if (!isDraw) currentPlayer = CellState.X
        } else if (winner == CellState.O) {
            scoreO++
        }
    }

    // AI move trigger
    LaunchedEffect(currentPlayer, aiThinking) {
        if (gameMode == GameMode.VS_AI && currentPlayer == CellState.O && winner == null && !isDraw) {
            aiThinking = true
            delay(400) // Simulate thinking
            aiMove()
            aiThinking = false
        }
    }

    fun onCellClick(row: Int, col: Int) {
        if (board[row][col] != CellState.EMPTY || winner != null || isDraw || aiThinking) return
        if (gameMode == GameMode.VS_AI && currentPlayer != CellState.X) return

        val newBoard = Array(3) { r -> Array(3) { c -> board[r][c] } }
        newBoard[row][col] = currentPlayer
        board = newBoard

        winner = checkWinner()
        if (winner != null) {
            if (winner == CellState.X) scoreX++ else scoreO++
        } else {
            isDraw = board.all { r -> r.all { it != CellState.EMPTY } }
            if (!isDraw) currentPlayer = if (currentPlayer == CellState.X) CellState.O else CellState.X
        }
    }

    fun resetBoard() {
        board = Array(3) { Array(3) { CellState.EMPTY } }
        currentPlayer = CellState.X
        winner = null
        isDraw = false
        winLine = null
        aiThinking = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tic Tac Toe") },
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Game mode chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = gameMode == GameMode.TWO_PLAYER,
                    onClick = { gameMode = GameMode.TWO_PLAYER; resetBoard() },
                    label = { Text("2 Player") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    ),
                )
                FilterChip(
                    selected = gameMode == GameMode.VS_AI,
                    onClick = { gameMode = GameMode.VS_AI; resetBoard() },
                    label = { Text("vs AI") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    ),
                )
            }

            Spacer(Modifier.height(16.dp))

            // Scores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("X", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                    Text("$scoreX", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("O", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                    Text("$scoreO", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Status
            val statusText = when {
                winner != null -> "${winner!!.name} Wins!"
                isDraw -> "Draw!"
                aiThinking -> "AI thinking..."
                else -> "${currentPlayer.name}'s Turn"
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(20.dp))

            // Board
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f),
            ) {
                val gridColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                val xColor = MaterialTheme.colorScheme.onBackground
                val oColor = MaterialTheme.colorScheme.primary
                val winColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cellW = size.width / 3f
                    val cellH = size.height / 3f
                    val lineW = 2.dp.toPx()

                    // Grid lines
                    for (i in 1..2) {
                        drawLine(gridColor, Offset(cellW * i, 0f), Offset(cellW * i, size.height), lineW)
                        drawLine(gridColor, Offset(0f, cellH * i), Offset(size.width, cellH * i), lineW)
                    }

                    // Win line
                    winLine?.let { (start, end) ->
                        val sx = start.second * cellW + cellW / 2
                        val sy = start.first * cellH + cellH / 2
                        val ex = end.second * cellW + cellW / 2
                        val ey = end.first * cellH + cellH / 2
                        drawLine(winColor, Offset(sx, sy), Offset(ex, ey), 6.dp.toPx(), StrokeCap.Round)
                    }
                }

                // Clickable grid overlay
                Column(modifier = Modifier.fillMaxSize()) {
                    for (row in 0..2) {
                        Row(modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()) {
                            for (col in 0..2) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clickable { onCellClick(row, col) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    val cell = board[row][col]
                                    if (cell == CellState.X) {
                                        XMark(color = xColor)
                                    } else if (cell == CellState.O) {
                                        OMark(color = oColor)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { resetBoard() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text("New Game")
            }
        }
    }
}

@Composable
private fun XMark(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(48.dp)) {
        val pad = size.width * 0.2f
        val strokeW = 4.dp.toPx()
        drawLine(color, Offset(pad, pad), Offset(size.width - pad, size.height - pad), strokeW, StrokeCap.Round)
        drawLine(color, Offset(size.width - pad, pad), Offset(pad, size.height - pad), strokeW, StrokeCap.Round)
    }
}

@Composable
private fun OMark(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(48.dp)) {
        val pad = size.width * 0.2f
        val strokeW = 4.dp.toPx()
        drawCircle(color, radius = (size.width - pad * 2) / 2, style = Stroke(width = strokeW))
    }
}
