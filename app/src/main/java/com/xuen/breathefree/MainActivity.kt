package com.xuen.breathefree

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xuen.breathefree.ui.navigation.Screen
import com.xuen.breathefree.ui.screens.BreathingScreen
import com.xuen.breathefree.ui.screens.DashboardScreen
import com.xuen.breathefree.ui.screens.SettingsScreen
import com.xuen.breathefree.ui.screens.SplashScreen
import com.xuen.breathefree.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    IgniteApp()
                }
            }
        }
    }
}

@Composable
fun IgniteApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { com.xuen.breathefree.data.UserStatsRepository(context) }
    val dashboardViewModel = remember { com.xuen.breathefree.ui.viewmodel.DashboardViewModel(repository) }
    val settingsViewModel = remember { com.xuen.breathefree.ui.viewmodel.SettingsViewModel(repository) }
    
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToBreathing = {
                    navController.navigate(Screen.Breathing.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(Screen.Breathing.route) {
            BreathingScreen(
                repository = repository, // Pass repository for theme access
                onSessionComplete = {
                    // Update stats when session completes
                    dashboardViewModel.onSessionCompleted()
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}