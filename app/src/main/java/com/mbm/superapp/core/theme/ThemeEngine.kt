package com.mbm.superapp.core.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")

data class ThemeState(
    val primaryColor: Color = MBMColors.Black,
    val accentColor: Color = MBMColors.MediumGray,
    val backgroundColor: Color = Color(0xFFFAFAFA),
    val surfaceColor: Color = MBMColors.White,
    val textColor: Color = Color(0xFF212121),
    val isDarkMode: Boolean = false,
)

class ThemeEngine(private val context: Context) : ViewModel() {

    private object Keys {
        val PRIMARY = intPreferencesKey("primary_color")
        val ACCENT = intPreferencesKey("accent_color")
        val BACKGROUND = intPreferencesKey("background_color")
        val SURFACE = intPreferencesKey("surface_color")
        val TEXT = intPreferencesKey("text_color")
        val IS_DARK = booleanPreferencesKey("is_dark_mode")
    }

    val themeState: StateFlow<ThemeState> = context.themeDataStore.data
        .map { prefs ->
            ThemeState(
                primaryColor = Color(prefs[Keys.PRIMARY] ?: MBMColors.Black.toArgb()),
                accentColor = Color(prefs[Keys.ACCENT] ?: MBMColors.MediumGray.toArgb()),
                backgroundColor = Color(prefs[Keys.BACKGROUND] ?: 0xFFFAFAFA.toInt()),
                surfaceColor = Color(prefs[Keys.SURFACE] ?: MBMColors.White.toArgb()),
                textColor = Color(prefs[Keys.TEXT] ?: 0xFF212121.toInt()),
                isDarkMode = prefs[Keys.IS_DARK] ?: false,
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeState())

    fun updatePrimaryColor(color: Color) {
        viewModelScope.launch {
            context.themeDataStore.edit { it[Keys.PRIMARY] = color.toArgb() }
        }
    }

    fun updateAccentColor(color: Color) {
        viewModelScope.launch {
            context.themeDataStore.edit { it[Keys.ACCENT] = color.toArgb() }
        }
    }

    fun updateBackgroundColor(color: Color) {
        viewModelScope.launch {
            context.themeDataStore.edit { it[Keys.BACKGROUND] = color.toArgb() }
        }
    }

    fun updateSurfaceColor(color: Color) {
        viewModelScope.launch {
            context.themeDataStore.edit { it[Keys.SURFACE] = color.toArgb() }
        }
    }

    fun updateTextColor(color: Color) {
        viewModelScope.launch {
            context.themeDataStore.edit { it[Keys.TEXT] = color.toArgb() }
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            context.themeDataStore.edit { prefs ->
                val current = prefs[Keys.IS_DARK] ?: true
                prefs[Keys.IS_DARK] = !current
                if (!current) {
                    // Switching to dark
                    prefs[Keys.PRIMARY] = MBMColors.White.toArgb()
                    prefs[Keys.ACCENT] = MBMColors.LightGray.toArgb()
                    prefs[Keys.BACKGROUND] = MBMColors.Black.toArgb()
                    prefs[Keys.SURFACE] = MBMColors.DarkGray.toArgb()
                    prefs[Keys.TEXT] = MBMColors.White.toArgb()
                } else {
                    // Switching to light
                    prefs[Keys.PRIMARY] = MBMColors.Black.toArgb()
                    prefs[Keys.ACCENT] = MBMColors.MediumGray.toArgb()
                    prefs[Keys.BACKGROUND] = MBMColors.OffWhite.toArgb()
                    prefs[Keys.SURFACE] = MBMColors.White.toArgb()
                    prefs[Keys.TEXT] = MBMColors.Black.toArgb()
                }
            }
        }
    }

    fun applyPreset(preset: ThemePreset) {
        viewModelScope.launch {
            context.themeDataStore.edit { prefs ->
                prefs[Keys.PRIMARY] = preset.primary.toArgb()
                prefs[Keys.ACCENT] = preset.accent.toArgb()
                prefs[Keys.BACKGROUND] = preset.background.toArgb()
                prefs[Keys.SURFACE] = preset.surface.toArgb()
                prefs[Keys.TEXT] = preset.text.toArgb()
                prefs[Keys.IS_DARK] = preset.isDark
            }
        }
    }

    fun resetToDefault() {
        viewModelScope.launch {
            context.themeDataStore.edit { it.clear() }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ThemeEngine(context) as T
        }
    }
}

data class ThemePreset(
    val name: String,
    val primary: Color,
    val accent: Color,
    val background: Color,
    val surface: Color,
    val text: Color,
    val isDark: Boolean,
)

object ThemePresets {
    val ClassicBW = ThemePreset(
        name = "Classic B&W",
        primary = MBMColors.White,
        accent = MBMColors.LightGray,
        background = MBMColors.Black,
        surface = MBMColors.DarkGray,
        text = MBMColors.White,
        isDark = true,
    )
    val Midnight = ThemePreset(
        name = "Midnight",
        primary = Color(0xFF90CAF9),
        accent = Color(0xFF42A5F5),
        background = Color(0xFF0D1117),
        surface = Color(0xFF161B22),
        text = Color(0xFFE6EDF3),
        isDark = true,
    )
    val Amber = ThemePreset(
        name = "Amber Terminal",
        primary = Color(0xFFFFB300),
        accent = Color(0xFFFF8F00),
        background = Color(0xFF1A1200),
        surface = Color(0xFF2D2000),
        text = Color(0xFFFFD54F),
        isDark = true,
    )
    val GreenPhosphor = ThemePreset(
        name = "Green Phosphor",
        primary = Color(0xFF00E676),
        accent = Color(0xFF69F0AE),
        background = Color(0xFF001A0A),
        surface = Color(0xFF002D12),
        text = Color(0xFF00E676),
        isDark = true,
    )
    val PaperWhite = ThemePreset(
        name = "Paper White",
        primary = MBMColors.Black,
        accent = MBMColors.MediumGray,
        background = Color(0xFFFAFAFA),
        surface = MBMColors.White,
        text = Color(0xFF212121),
        isDark = false,
    )
    val Rosewood = ThemePreset(
        name = "Rosewood",
        primary = Color(0xFFE91E63),
        accent = Color(0xFFF48FB1),
        background = Color(0xFF1A0A10),
        surface = Color(0xFF2D1520),
        text = Color(0xFFFCE4EC),
        isDark = true,
    )
    val all = listOf(ClassicBW, Midnight, Amber, GreenPhosphor, PaperWhite, Rosewood)
}
