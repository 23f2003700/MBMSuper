package com.mbm.superapp.features.games.chess

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class PieceColor { WHITE, BLACK }
enum class PieceType { KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN }

data class ChessPiece(val type: PieceType, val color: PieceColor) {
    val symbol: String get() = when (type) {
        PieceType.KING -> if (color == PieceColor.WHITE) "\u2654" else "\u265A"
        PieceType.QUEEN -> if (color == PieceColor.WHITE) "\u2655" else "\u265B"
        PieceType.ROOK -> if (color == PieceColor.WHITE) "\u2656" else "\u265C"
        PieceType.BISHOP -> if (color == PieceColor.WHITE) "\u2657" else "\u265D"
        PieceType.KNIGHT -> if (color == PieceColor.WHITE) "\u2658" else "\u265E"
        PieceType.PAWN -> if (color == PieceColor.WHITE) "\u2659" else "\u265F"
    }
}

typealias Board = Array<Array<ChessPiece?>>

fun initialBoard(): Board {
    val board: Board = Array(8) { arrayOfNulls(8) }
    val backRow = arrayOf(PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
        PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK)
    for (c in 0..7) {
        board[0][c] = ChessPiece(backRow[c], PieceColor.BLACK)
        board[1][c] = ChessPiece(PieceType.PAWN, PieceColor.BLACK)
        board[6][c] = ChessPiece(PieceType.PAWN, PieceColor.WHITE)
        board[7][c] = ChessPiece(backRow[c], PieceColor.WHITE)
    }
    return board
}

fun getValidMoves(board: Board, row: Int, col: Int): List<Pair<Int, Int>> {
    val piece = board[row][col] ?: return emptyList()
    val moves = mutableListOf<Pair<Int, Int>>()

    fun inBounds(r: Int, c: Int) = r in 0..7 && c in 0..7
    fun isEmpty(r: Int, c: Int) = inBounds(r, c) && board[r][c] == null
    fun isEnemy(r: Int, c: Int) = inBounds(r, c) && board[r][c] != null && board[r][c]!!.color != piece.color
    fun canMove(r: Int, c: Int) = isEmpty(r, c) || isEnemy(r, c)

    fun addSliding(dr: Int, dc: Int) {
        var r = row + dr; var c = col + dc
        while (inBounds(r, c)) {
            if (board[r][c] == null) { moves.add(r to c) }
            else { if (board[r][c]!!.color != piece.color) moves.add(r to c); break }
            r += dr; c += dc
        }
    }

    when (piece.type) {
        PieceType.PAWN -> {
            val dir = if (piece.color == PieceColor.WHITE) -1 else 1
            val startRow = if (piece.color == PieceColor.WHITE) 6 else 1
            if (isEmpty(row + dir, col)) {
                moves.add((row + dir) to col)
                if (row == startRow && isEmpty(row + 2 * dir, col)) moves.add((row + 2 * dir) to col)
            }
            if (isEnemy(row + dir, col - 1)) moves.add((row + dir) to (col - 1))
            if (isEnemy(row + dir, col + 1)) moves.add((row + dir) to (col + 1))
        }
        PieceType.KNIGHT -> {
            val offsets = listOf(-2 to -1, -2 to 1, -1 to -2, -1 to 2, 1 to -2, 1 to 2, 2 to -1, 2 to 1)
            for ((dr, dc) in offsets) if (canMove(row + dr, col + dc)) moves.add((row + dr) to (col + dc))
        }
        PieceType.BISHOP -> { for ((dr, dc) in listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)) addSliding(dr, dc) }
        PieceType.ROOK -> { for ((dr, dc) in listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)) addSliding(dr, dc) }
        PieceType.QUEEN -> {
            for ((dr, dc) in listOf(-1 to -1, -1 to 0, -1 to 1, 0 to -1, 0 to 1, 1 to -1, 1 to 0, 1 to 1)) addSliding(dr, dc)
        }
        PieceType.KING -> {
            for (dr in -1..1) for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                if (canMove(row + dr, col + dc)) moves.add((row + dr) to (col + dc))
            }
        }
    }
    return moves
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessScreen(onBack: () -> Unit) {
    var board by remember { mutableStateOf(initialBoard()) }
    var currentTurn by remember { mutableStateOf(PieceColor.WHITE) }
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var validMoves by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }
    var capturedWhite by remember { mutableStateOf(listOf<ChessPiece>()) }
    var capturedBlack by remember { mutableStateOf(listOf<ChessPiece>()) }
    var statusText by remember { mutableStateOf("White's Turn") }

    val textMeasurer = rememberTextMeasurer()

    fun movePiece(fromR: Int, fromC: Int, toR: Int, toC: Int) {
        val newBoard = Array(8) { r -> Array(8) { c -> board[r][c] } }
        val captured = newBoard[toR][toC]
        newBoard[toR][toC] = newBoard[fromR][fromC]
        newBoard[fromR][fromC] = null

        // Pawn promotion
        val piece = newBoard[toR][toC]
        if (piece?.type == PieceType.PAWN && (toR == 0 || toR == 7)) {
            newBoard[toR][toC] = ChessPiece(PieceType.QUEEN, piece.color)
        }

        if (captured != null) {
            if (captured.color == PieceColor.WHITE) capturedWhite = capturedWhite + captured
            else capturedBlack = capturedBlack + captured
        }

        board = newBoard
        currentTurn = if (currentTurn == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        statusText = if (currentTurn == PieceColor.WHITE) "White's Turn" else "Black's Turn"

        // Check if king captured (basic game-over)
        if (captured?.type == PieceType.KING) {
            statusText = if (captured.color == PieceColor.WHITE) "Black Wins!" else "White Wins!"
        }
    }

    fun onCellTap(row: Int, col: Int) {
        if (selectedCell != null && (row to col) in validMoves) {
            movePiece(selectedCell!!.first, selectedCell!!.second, row, col)
            selectedCell = null
            validMoves = emptyList()
            return
        }

        val piece = board[row][col]
        if (piece != null && piece.color == currentTurn) {
            selectedCell = row to col
            validMoves = getValidMoves(board, row, col)
        } else {
            selectedCell = null
            validMoves = emptyList()
        }
    }

    fun resetGame() {
        board = initialBoard()
        currentTurn = PieceColor.WHITE
        selectedCell = null
        validMoves = emptyList()
        capturedWhite = emptyList()
        capturedBlack = emptyList()
        statusText = "White's Turn"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chess") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Captured black pieces
            Text(
                text = capturedBlack.joinToString("") { it.symbol },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(12.dp))

            // Board
            val lightSquare = MaterialTheme.colorScheme.surface
            val darkSquare = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            val selectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            val moveHintColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            val pieceTextColor = MaterialTheme.colorScheme.onSurface

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .pointerInput(selectedCell, validMoves, currentTurn) {
                        detectTapGestures { offset ->
                            val cellSize = size.width / 8f
                            val col = (offset.x / cellSize).toInt().coerceIn(0, 7)
                            val row = (offset.y / cellSize).toInt().coerceIn(0, 7)
                            onCellTap(row, col)
                        }
                    },
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cellSize = size.width / 8f

                    for (r in 0..7) {
                        for (c in 0..7) {
                            val isLight = (r + c) % 2 == 0
                            val rect = Offset(c * cellSize, r * cellSize)

                            // Square color
                            drawRect(
                                color = if (isLight) lightSquare else darkSquare,
                                topLeft = rect,
                                size = Size(cellSize, cellSize),
                            )

                            // Selected highlight
                            if (selectedCell == r to c) {
                                drawRect(selectedColor, rect, Size(cellSize, cellSize))
                            }

                            // Valid move hints
                            if ((r to c) in validMoves) {
                                if (board[r][c] != null) {
                                    drawRect(moveHintColor, rect, Size(cellSize, cellSize))
                                } else {
                                    drawCircle(
                                        moveHintColor,
                                        radius = cellSize * 0.15f,
                                        center = Offset(c * cellSize + cellSize / 2, r * cellSize + cellSize / 2),
                                    )
                                }
                            }

                            // Piece
                            board[r][c]?.let { piece ->
                                val layout = textMeasurer.measure(
                                    text = piece.symbol,
                                    style = TextStyle(fontSize = (cellSize * 0.55f).toSp(), fontWeight = FontWeight.Normal),
                                )
                                drawText(
                                    layout,
                                    color = pieceTextColor,
                                    topLeft = Offset(
                                        c * cellSize + (cellSize - layout.size.width) / 2,
                                        r * cellSize + (cellSize - layout.size.height) / 2,
                                    ),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Captured white pieces
            Text(
                text = capturedWhite.joinToString("") { it.symbol },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { resetGame() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text("New Game")
            }
        }
    }
}

private fun Float.toSp() = (this / 3f).sp // rough px-to-sp approximation for Canvas
