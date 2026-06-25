package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun MyApplicationTheme(
  themeMode: String = "light",
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  // Completely force Light Mode (ignore themeMode and system settings)
  val darkTheme = false

  val lightColorScheme = lightColorScheme(
    primary = CrimsonPrimary,
    secondary = CrimsonSecondary,
    tertiary = ColorMenstruation,
    background = SoftCream,
    surface = Color.White.copy(alpha = 0.5f), // Translucent white surface for glassmorphism defaults
    surfaceVariant = SleekSurfaceVariant,
    outline = SleekOutline,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = DarkWine,
    onSurface = DarkWine,
    onSurfaceVariant = SleekTextSecondary
  )

  CompositionLocalProvider(LocalThemeIsDark provides false) {
    MaterialTheme(
      colorScheme = lightColorScheme,
      typography = Typography,
      content = content
    )
  }
}
