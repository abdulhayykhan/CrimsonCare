package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalThemeIsDark = staticCompositionLocalOf { false }

// Brand colors
val CrimsonPrimary = Color(0xFFB12E33)
val CrimsonSecondary = Color(0xFF8C1D1D)

val WarmRose: Color
    @Composable
    get() = if (LocalThemeIsDark.current) Color(0xFF3D1B1D) else Color(0xFFFDE7E9)

val SoftCream: Color
    @Composable
    get() = if (LocalThemeIsDark.current) Color(0xFF140D0E) else Color(0xFFFFFBFF)

val DarkWine: Color
    @Composable
    get() = if (LocalThemeIsDark.current) Color(0xFFECE0E0) else Color(0xFF211A1D)

val SlateBackground = Color(0xFF1C1B1F)

// Cycle Phase specific colors
val ColorMenstruation: Color
    @Composable
    get() = if (LocalThemeIsDark.current) Color(0xFFFF5252) else Color(0xFFB12E33)

val ColorFollicular: Color
    @Composable
    get() = if (LocalThemeIsDark.current) Color(0xFFFF8A80) else Color(0xFF8C1D1D)

val ColorOvulation: Color
    @Composable
    get() = if (LocalThemeIsDark.current) Color(0xFFB0BEC5) else Color(0xFF685D61)

val ColorLuteal: Color
    @Composable
    get() = if (LocalThemeIsDark.current) Color(0xFFFFAB40) else Color(0xFFB35A5F)

// Additional Sleek Interface theme colors
val SleekSurfaceVariant: Color
    @Composable
    get() = if (LocalThemeIsDark.current) Color(0xFF201314) else Color(0xFFFDF0F1)

val SleekOutline: Color
    @Composable
    get() = if (LocalThemeIsDark.current) Color(0xFF452426) else Color(0xFFF9D4D7)

val SleekBackgroundVariant: Color
    @Composable
    get() = if (LocalThemeIsDark.current) Color(0xFF1A1112) else Color(0xFFF5F2F4)

val SleekBorderVariant: Color
    @Composable
    get() = if (LocalThemeIsDark.current) Color(0xFF331E20) else Color(0xFFE9E1E3)

val SleekCircleBase: Color
    @Composable
    get() = if (LocalThemeIsDark.current) Color(0xFF2A1719) else Color(0xFFF5EEF0)

val SleekTextPrimary: Color
    @Composable
    get() = if (LocalThemeIsDark.current) Color(0xFFECE0E0) else Color(0xFF211A1D)

val SleekTextSecondary: Color
    @Composable
    get() = if (LocalThemeIsDark.current) Color(0xFFA59496) else Color(0xFF685D61)

