package com.xuen.breathefree.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Dashboard : Screen("dashboard")
    object Breathing : Screen("breathing")
    object Settings : Screen("settings")
}
