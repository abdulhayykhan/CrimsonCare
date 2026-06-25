package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalThemeIsDark = staticCompositionLocalOf { false }

// Brand colors
val CrimsonPrimary = Color(0xFFB12E33) // Deep Crimson
val CrimsonSecondary = Color(0xFF8C1D1D)

// All theme properties are set strictly to their Light Theme variants (Pink to Crimson palette)
val WarmRose: Color
    @Composable
    get() = Color(0xFFFDE7E9) // Light Pink

val SoftCream: Color
    @Composable
    get() = Color(0xFFFFFBFF) // Soft Cream

val DarkWine: Color
    @Composable
    get() = Color(0xFF211A1D) // Dark Charcoal/Wine

val SlateBackground = Color(0xFFFFFBFF)

// Cycle Phase specific colors in a Light Pink to Crimson palette
val ColorMenstruation: Color
    @Composable
    get() = Color(0xFFB12E33) // Crimson Accent

val ColorFollicular: Color
    @Composable
    get() = Color(0xFF8C1D1D)

val ColorOvulation: Color
    @Composable
    get() = Color(0xFF685D61)

val ColorLuteal: Color
    @Composable
    get() = Color(0xFFB35A5F)

// Sleek Light Interface theme colors
val SleekSurfaceVariant: Color
    @Composable
    get() = Color(0xFFFDF0F1)

val SleekOutline: Color
    @Composable
    get() = Color(0xFFF9D4D7)

val SleekBackgroundVariant: Color
    @Composable
    get() = Color(0xFFF5F2F4)

val SleekBorderVariant: Color
    @Composable
    get() = Color(0xFFE9E1E3)

val SleekCircleBase: Color
    @Composable
    get() = Color(0xFFF5EEF0)

val SleekTextPrimary: Color
    @Composable
    get() = Color(0xFF211A1D) // Dark Charcoal

val SleekTextSecondary: Color
    @Composable
    get() = Color(0xFF685D61)
