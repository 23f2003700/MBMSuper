package com.mbm.superapp.core.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

@Composable
fun MBMSuperTheme(
    themeState: ThemeState,
    content: @Composable () -> Unit
) {
    val anim = 400
    val primary by animateColorAsState(themeState.primaryColor, tween(anim), label = "p")
    val accent by animateColorAsState(themeState.accentColor, tween(anim), label = "a")
    val background by animateColorAsState(themeState.backgroundColor, tween(anim), label = "bg")
    val surface by animateColorAsState(themeState.surfaceColor, tween(anim), label = "sf")
    val text by animateColorAsState(themeState.textColor, tween(anim), label = "tx")

    val colorScheme = if (themeState.isDarkMode) {
        darkColorScheme(
            primary = primary,
            secondary = accent,
            tertiary = accent,
            background = background,
            surface = surface,
            surfaceVariant = surface,
            surfaceContainerHighest = surface,
            surfaceContainerHigh = surface,
            surfaceContainer = surface,
            surfaceContainerLow = background,
            surfaceContainerLowest = background,
            onPrimary = background,
            onSecondary = background,
            onTertiary = background,
            onBackground = text,
            onSurface = text,
            onSurfaceVariant = text.copy(alpha = 0.7f),
            outline = text.copy(alpha = 0.15f),
            outlineVariant = text.copy(alpha = 0.08f),
            inverseSurface = text,
            inverseOnSurface = background,
            inversePrimary = primary.copy(alpha = 0.7f),
            error = MBMColors.Error,
            onError = Color.White,
        )
    } else {
        lightColorScheme(
            primary = primary,
            secondary = accent,
            tertiary = accent,
            background = background,
            surface = surface,
            surfaceVariant = surface,
            surfaceContainerHighest = surface,
            surfaceContainerHigh = surface,
            surfaceContainer = surface,
            surfaceContainerLow = background,
            surfaceContainerLowest = background,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = text,
            onSurface = text,
            onSurfaceVariant = text.copy(alpha = 0.7f),
            outline = text.copy(alpha = 0.15f),
            outlineVariant = text.copy(alpha = 0.08f),
            inverseSurface = text,
            inverseOnSurface = background,
            inversePrimary = primary.copy(alpha = 0.7f),
            error = MBMColors.Error,
            onError = Color.White,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MBMTypography,
        content = content,
    )
}
