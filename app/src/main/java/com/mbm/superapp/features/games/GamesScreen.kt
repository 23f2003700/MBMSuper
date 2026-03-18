package com.mbm.superapp.features.games

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Segment
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun GamesScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text(
            text = "Games",
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "All games work offline",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        )

        Spacer(Modifier.height(24.dp))

        GameCard(
            icon = Icons.Outlined.Tag,
            title = "Tic Tac Toe",
            description = "Classic X & O. 2 players or play vs AI",
            players = "1-2 Players",
            onClick = { navController.navigate("games/tictactoe") },
        )
        Spacer(Modifier.height(12.dp))

        GameCard(
            icon = Icons.Outlined.GridOn,
            title = "Dots & Boxes",
            description = "Connect dots, complete boxes. Same-device multiplayer",
            players = "2-4 Players",
            onClick = { navController.navigate("games/dotbox") },
        )
        Spacer(Modifier.height(12.dp))

        GameCard(
            icon = Icons.Outlined.Segment,
            title = "Snake",
            description = "Classic snake game. Eat, grow, survive",
            players = "1 Player",
            onClick = { navController.navigate("games/snake") },
        )
        Spacer(Modifier.height(12.dp))

        GameCard(
            icon = Icons.Outlined.SmartToy,
            title = "Chess",
            description = "Two-player chess with full piece movement",
            players = "2 Players",
            onClick = { navController.navigate("games/chess") },
        )
        Spacer(Modifier.height(12.dp))

        GameCard(
            icon = Icons.Outlined.Casino,
            title = "Ludo",
            description = "Roll dice, race your pieces home",
            players = "2-4 Players",
            onClick = { navController.navigate("games/ludo") },
        )
        Spacer(Modifier.height(12.dp))

        GameCard(
            icon = Icons.Outlined.Pets,
            title = "Cat Chase",
            description = "Tap the screen and watch the cat chase your finger",
            players = "1 Player",
            onClick = { navController.navigate("games/catchcat") },
        )

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun GameCard(
    icon: ImageVector,
    title: String,
    description: String,
    players: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = players,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }
    }
}
