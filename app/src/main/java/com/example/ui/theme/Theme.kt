package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = EmeraldGreen,
    secondary = DeepEmerald,
    tertiary = LightEmerald,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = TextOnSurface,
    onSurface = TextOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextMuted
  )

private val LightColorScheme =
  lightColorScheme(
    primary = DeepEmerald,
    secondary = EmeraldGreen,
    tertiary = LightEmerald,
    background = LightBackground,
    surface = LightSurface,
    onBackground = TextOnSurfaceLight,
    onSurface = TextOnSurfaceLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextMutedLight
  )

@Composable
fun MyApplicationTheme(
  appTheme: String = "system", // "dark", "light", "system"
  content: @Composable () -> Unit,
) {
  val isDark = when (appTheme) {
    "dark" -> true
    "light" -> false
    else -> isSystemInDarkTheme()
  }

  val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

