package com.xuen.breathefree.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val BurntOrange = Color(0xFFFF4500)
val DeepCyan = Color(0xFF008B8B)
val PitchBlack = Color(0xFF000000)
val DarkGray = Color(0xFF121212)
val LightGray = Color(0xFFCCCCCC)

val ThemeGradient = Brush.verticalGradient(colors = listOf(DeepCyan, BurntOrange))

// Material Colors mapped
val Primary = BurntOrange
val Secondary = DeepCyan
val Tertiary = Color(0xFFFFA500) // Lighter orange
val Background = PitchBlack
val Surface = DarkGray
val OnPrimary = Color.White
val OnSecondary = Color.White
val OnTertiary = Color.Black
val OnBackground = Color.White
val OnSurface = Color.White