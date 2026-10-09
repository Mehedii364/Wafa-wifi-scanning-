package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppThemeMode {
    SYSTEM,
    OBSIDIAN,
    AMOLED,
    LIGHT
}

private val ObsidianColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Color(0xFF002028),
    primaryContainer = Color(0xFF004D61),
    onPrimaryContainer = Color(0xFFBCE9FF),
    secondary = VioletAccent,
    onSecondary = Color(0xFF32005C),
    secondaryContainer = Color(0xFF531E85),
    onSecondaryContainer = Color(0xFFEEDBFF),
    tertiary = EmeraldGreen,
    background = ObsidianBackground,
    onBackground = Color(0xFFE2E8F0),
    surface = ObsidianSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = ObsidianBorder,
    error = CrimsonError
)

private val AmoledColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF003847),
    onPrimaryContainer = Color(0xFFBCE9FF),
    secondary = VioletAccent,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF380860),
    onSecondaryContainer = Color(0xFFEEDBFF),
    tertiary = EmeraldGreen,
    background = AmoledBackground,
    onBackground = Color(0xFFEDEDED),
    surface = AmoledSurface,
    onSurface = Color(0xFFF5F5F5),
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = Color(0xFFA3A3A3),
    outline = AmoledBorder,
    error = CrimsonError
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF7C3AED),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF6D28D9),
    tertiary = EmeraldGreen,
    background = LightBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF1E293B),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF64748B),
    outline = LightBorder,
    error = CrimsonError
)

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.OBSIDIAN,
    content: @Composable () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val colorScheme = when (themeMode) {
        AppThemeMode.OBSIDIAN -> ObsidianColorScheme
        AppThemeMode.AMOLED -> AmoledColorScheme
        AppThemeMode.LIGHT -> LightColorScheme
        AppThemeMode.SYSTEM -> if (dark) ObsidianColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
