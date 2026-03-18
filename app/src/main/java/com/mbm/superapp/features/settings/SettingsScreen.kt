package com.mbm.superapp.features.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbm.superapp.core.theme.ThemeEngine
import com.mbm.superapp.core.theme.ThemePresets

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(themeEngine: ThemeEngine, onBack: () -> Unit) {
    val themeState by themeEngine.themeState.collectAsState()

    val customColors = listOf(
        "White" to Color(0xFFFFFFFF),
        "Black" to Color(0xFF000000),
        "Red" to Color(0xFFE53935),
        "Pink" to Color(0xFFEC407A),
        "Purple" to Color(0xFF7E57C2),
        "Blue" to Color(0xFF42A5F5),
        "Cyan" to Color(0xFF26C6DA),
        "Teal" to Color(0xFF26A69A),
        "Green" to Color(0xFF66BB6A),
        "Lime" to Color(0xFFD4E157),
        "Yellow" to Color(0xFFFFEE58),
        "Orange" to Color(0xFFFFA726),
        "Amber" to Color(0xFFFFCA28),
        "Gray" to Color(0xFF78909C),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { themeEngine.resetToDefault() }) {
                        Icon(Icons.Outlined.Refresh, "Reset", tint = MaterialTheme.colorScheme.onSurface)
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
                .padding(20.dp),
        ) {
            // Dark mode toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        if (themeState.isDarkMode) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                        contentDescription = "Theme Mode",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (themeState.isDarkMode) "Dark Mode" else "Light Mode",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Toggle between dark and light themes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
                    }
                    Switch(
                        checked = themeState.isDarkMode,
                        onCheckedChange = { themeEngine.toggleDarkMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        ),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Theme presets
            Text(
                text = "Theme Presets",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (preset in ThemePresets.all) {
                    Card(
                        onClick = { themeEngine.applyPreset(preset) },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(preset.primary)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Custom color pickers
            Text(
                text = "Customize Primary Color",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                for ((name, color) in customColors) {
                    val isSelected = themeState.primaryColor == color
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = CircleShape,
                            )
                            .clickable { themeEngine.updatePrimaryColor(color) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Outlined.Check,
                                contentDescription = "Selected",
                                modifier = Modifier.size(18.dp),
                                tint = if (color == Color.White || color == Color(0xFFFFEE58) || color == Color(0xFFD4E157))
                                    Color.Black else Color.White,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Accent color
            Text(
                text = "Customize Accent Color",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                for ((name, color) in customColors) {
                    val isSelected = themeState.accentColor == color
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = CircleShape,
                            )
                            .clickable { themeEngine.updateAccentColor(color) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Outlined.Check,
                                contentDescription = "Selected",
                                modifier = Modifier.size(18.dp),
                                tint = if (color == Color.White || color == Color(0xFFFFEE58) || color == Color(0xFFD4E157))
                                    Color.Black else Color.White,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Background color
            Text(
                text = "Background Color",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                for ((name, color) in customColors) {
                    val isSelected = themeState.backgroundColor == color
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = CircleShape,
                            )
                            .clickable { themeEngine.updateBackgroundColor(color) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Outlined.Check,
                                contentDescription = "Selected",
                                modifier = Modifier.size(18.dp),
                                tint = if (color == Color.White || color == Color(0xFFFFEE58) || color == Color(0xFFD4E157))
                                    Color.Black else Color.White,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Reset
            TextButton(
                onClick = { themeEngine.resetToDefault() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Reset to Default (Classic B&W)",
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}
