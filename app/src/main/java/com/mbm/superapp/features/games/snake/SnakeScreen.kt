package com.mbm.superapp.features.games.snake

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

enum class Direction { UP, DOWN, LEFT, RIGHT }
data class SnakePoint(val x: Int, val y: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnakeScreen(onBack: () -> Unit) {
    val gridW = 20
    val gridH = 20
    var snake by remember { mutableStateOf(listOf(SnakePoint(10, 10), SnakePoint(9, 10), SnakePoint(8, 10))) }
    var food by remember { mutableStateOf(SnakePoint(15, 15)) }
    var direction by remember { mutableStateOf(Direction.RIGHT) }
    var score by remember { mutableIntStateOf(0) }
    var highScore by remember { mutableIntStateOf(0) }
    var isRunning by remember { mutableStateOf(true) }
    var gameOver by remember { mutableStateOf(false) }
    var speed by remember { mutableIntStateOf(150) }

    fun spawnFood(): SnakePoint {
        var p: SnakePoint
        do {
            p = SnakePoint(Random.nextInt(gridW), Random.nextInt(gridH))
        } while (p in snake)
        return p
    }

    fun resetGame() {
        snake = listOf(SnakePoint(10, 10), SnakePoint(9, 10), SnakePoint(8, 10))
        direction = Direction.RIGHT
        food = spawnFood()
        score = 0
        gameOver = false
        isRunning = true
        speed = 150
    }

    // Game loop
    LaunchedEffect(isRunning, gameOver) {
        while (isRunning && !gameOver) {
            delay(speed.toLong())

            val head = snake.first()
            val newHead = when (direction) {
                Direction.UP -> SnakePoint(head.x, head.y - 1)
                Direction.DOWN -> SnakePoint(head.x, head.y + 1)
                Direction.LEFT -> SnakePoint(head.x - 1, head.y)
                Direction.RIGHT -> SnakePoint(head.x + 1, head.y)
            }

            // Wall collision
            if (newHead.x < 0 || newHead.x >= gridW || newHead.y < 0 || newHead.y >= gridH) {
                gameOver = true
                if (score > highScore) highScore = score
                continue
            }

            // Self collision
            if (newHead in snake) {
                gameOver = true
                if (score > highScore) highScore = score
                continue
            }

            val ate = newHead == food
            val newSnake = buildList {
                add(newHead)
                addAll(snake)
                if (!ate) removeLast()
            }
            snake = newSnake
            if (ate) {
                score++
                food = spawnFood()
                if (speed > 60) speed -= 3
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Snake") },
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
            // Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Score: $score",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Best: $highScore",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
            }

            Spacer(Modifier.height(12.dp))

            // Game board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(8.dp),
                    )
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            val (dx, dy) = dragAmount
                            if (abs(dx) > abs(dy)) {
                                if (dx > 0 && direction != Direction.LEFT) direction = Direction.RIGHT
                                else if (dx < 0 && direction != Direction.RIGHT) direction = Direction.LEFT
                            } else {
                                if (dy > 0 && direction != Direction.UP) direction = Direction.DOWN
                                else if (dy < 0 && direction != Direction.DOWN) direction = Direction.UP
                            }
                        }
                    },
            ) {
                val snakeColor = MaterialTheme.colorScheme.onSurface
                val headColor = MaterialTheme.colorScheme.primary
                val foodColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                val gridLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)

                Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                    val cellW = size.width / gridW
                    val cellH = size.height / gridH

                    // Grid
                    for (i in 0..gridW) {
                        drawLine(gridLineColor, Offset(i * cellW, 0f), Offset(i * cellW, size.height), 0.5f)
                    }
                    for (i in 0..gridH) {
                        drawLine(gridLineColor, Offset(0f, i * cellH), Offset(size.width, i * cellH), 0.5f)
                    }

                    // Food
                    drawRect(
                        foodColor,
                        Offset(food.x * cellW + 1, food.y * cellH + 1),
                        Size(cellW - 2, cellH - 2),
                    )

                    // Snake
                    snake.forEachIndexed { index, point ->
                        val color = if (index == 0) headColor else snakeColor.copy(alpha = 0.8f - index * 0.02f)
                        drawRect(
                            color.copy(alpha = color.alpha.coerceAtLeast(0.3f)),
                            Offset(point.x * cellW + 0.5f, point.y * cellH + 0.5f),
                            Size(cellW - 1, cellH - 1),
                        )
                    }
                }

                // Game over overlay
                if (gameOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Game Over",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                "Score: $score",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // D-pad controls
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { if (direction != Direction.DOWN) direction = Direction.UP },
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape),
                ) {
                    Icon(Icons.Filled.KeyboardArrowUp, "Up", tint = MaterialTheme.colorScheme.onSurface)
                }
                Row {
                    IconButton(
                        onClick = { if (direction != Direction.RIGHT) direction = Direction.LEFT },
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape),
                    ) {
                        Icon(Icons.Filled.KeyboardArrowLeft, "Left", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.size(48.dp))
                    IconButton(
                        onClick = { if (direction != Direction.LEFT) direction = Direction.RIGHT },
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape),
                    ) {
                        Icon(Icons.Filled.KeyboardArrowRight, "Right", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
                IconButton(
                    onClick = { if (direction != Direction.UP) direction = Direction.DOWN },
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape),
                ) {
                    Icon(Icons.Filled.KeyboardArrowDown, "Down", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(Modifier.height(12.dp))

            if (gameOver) {
                Button(
                    onClick = { resetGame() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Text("Play Again")
                }
            }
        }
    }
}
