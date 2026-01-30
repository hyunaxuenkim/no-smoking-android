package com.xuen.breathefree.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val BurntOrange = Color(0xFFFF4500)
val DeepCyan = Color(0xFF008B8B)
val ElectricBlue = Color(0xFF00BFFF)
val MutedSkyBlue = Color(0xFF5B9BD5)
val PitchBlack = Color(0xFF000000)
val DarkGray = Color(0xFF121212)
val LightGray = Color(0xFFCCCCCC)

// New Theme: Ember Orange (Gradient Components)
val CoreOrange = Color(0xFFFF4D00)
val BrightBlue = Color(0xFF00A3FF)
val DeepPurple = Color(0xFF4B0082)

// New Theme: Electric Cyan (Renamed internally to Nature/Green for clarity, or kept as is)
// User requested "Electric Cyan to this color" (Green palette)
val NeonLime = Color(0xFF32FF00)
val EmeraldGreen = Color(0xFF00C853)
val DarkForest = Color(0xFF1B5E20)

// User requested NO GRADIENT, simple solid color.
val ThemeGradient = Brush.verticalGradient(colors = listOf(MutedSkyBlue, MutedSkyBlue)) 

// Update Primary to the new requested color
val Primary = MutedSkyBlue
val Secondary = DeepCyan
val Tertiary = Color(0xFFFFA500) // Lighter orange
val Background = PitchBlack
val Surface = DarkGray
val OnPrimary = Color.White
val OnSecondary = Color.White
val OnTertiary = Color.Black
val OnBackground = Color.White
val OnSurface = Color.White