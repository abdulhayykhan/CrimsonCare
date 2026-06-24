package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun MyApplicationTheme(
  themeMode: String = "system",
  // Dynamic color is supported if requested and available
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val darkTheme = when (themeMode) {
    "light" -> false
    "dark" -> true
    else -> isSystemInDarkTheme()
  }

  val lightColorScheme = lightColorScheme(
    primary = CrimsonPrimary,
    secondary = CrimsonSecondary,
    tertiary = ColorMenstruation,
    background = SoftCream,
    surface = Color.White,
    surfaceVariant = SleekSurfaceVariant,
    outline = SleekOutline,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = DarkWine,
    onSurface = DarkWine,
    onSurfaceVariant = SleekTextSecondary
  )

  val darkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB4AB),
    secondary = Color(0xFFFFB3B4),
    tertiary = Color(0xFFE5C1A7),
    background = Color(0xFF140D0E),
    surface = Color(0xFF1E1214),
    surfaceVariant = Color(0xFF231517),
    outline = Color(0xFF4E2C2E),
    onPrimary = Color(0xFF690005),
    onSecondary = Color(0xFF680016),
    onBackground = Color(0xFFECE0E0),
    onSurface = Color(0xFFECE0E0),
    onSurfaceVariant = Color(0xFFB3A5A8)
  )

  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> darkColorScheme
      else -> lightColorScheme
    }

  CompositionLocalProvider(LocalThemeIsDark provides darkTheme) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}
