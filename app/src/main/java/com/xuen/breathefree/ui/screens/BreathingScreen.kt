package com.xuen.breathefree.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.xuen.breathefree.data.AudioAnalyzer
import com.xuen.breathefree.ui.theme.BurntOrange
import com.xuen.breathefree.ui.theme.DeepCyan
import kotlinx.coroutines.delay

import androidx.compose.runtime.collectAsState
import com.xuen.breathefree.data.UserStatsRepository
import com.xuen.breathefree.ui.theme.*

@Composable
fun BreathingScreen(
    repository: UserStatsRepository,
    onSessionComplete: () -> Unit
) {
    val context = LocalContext.current
    val selectedTheme by repository.selectedTheme.collectAsState()
    
    var progress by remember { mutableFloatStateOf(0f) }
    var amplitude by remember { mutableFloatStateOf(0f) }
    
    // Configurable "Game" Constants
    val passiveBurnRate = 1f / 120f 
    val activeBurnFactor = 0.015f
    val noiseThreshold = 0.05f 

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasAudioPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Audio Analyzer
    val analyzer = remember { AudioAnalyzer(context) }

    DisposableEffect(hasAudioPermission) {
        if (hasAudioPermission) {
            analyzer.start()
        }
        onDispose {
            analyzer.stop()
        }
    }

    // Main Game Loop
    LaunchedEffect(hasAudioPermission) {
        if (!hasAudioPermission) return@LaunchedEffect
        
        val updateInterval = 50L
        val dt = updateInterval / 1000f

        while (progress < 1f) {
            val rawAmp = analyzer.getAmplitude()
            val effectiveAmp = if (rawAmp > noiseThreshold) rawAmp else 0f
            
            amplitude = effectiveAmp 
            
            val passiveBurn = passiveBurnRate * dt
            val activeBurn = effectiveAmp * activeBurnFactor * dt
            
            progress = (progress + passiveBurn + activeBurn).coerceAtMost(1f)
            
            if (progress >= 1f) {
                onSessionComplete()
            }
            delay(updateInterval)
        }
    }

    // --- Visual Animation States ---
    val visualInput = (amplitude * 2.5f).coerceAtMost(1f)

    val animatedSpread by animateFloatAsState(
        targetValue = visualInput,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "smoke_spread"
    )

    val maxBeamHeight = 400.dp
    val burntHeight = (maxBeamHeight * progress).coerceAtMost(maxBeamHeight)
    
    // --- Dynamic Theme Colors ---
    // User logic:
    // Ember: Core(Orange) -> Mid(Blue) -> Outer(Purple)
    // Cyan(Green): Core(Lime) -> Mid(Emerald) -> Outer(Forest)
    // Midnight: Core(Blue) -> Mid(Cyan) -> Outer(Transparent)
    
    val (beamColor, smokeGradient) = when (selectedTheme) {
        "Inferno" -> { // Formerly Ember Orange
             val beam = CoreOrange
             val smoke = listOf(
                 CoreOrange.copy(alpha = 0.6f + (animatedSpread * 0.3f)), 
                 BrightBlue.copy(alpha = 0.4f + (animatedSpread * 0.2f)),
                 DeepPurple.copy(alpha = 0.2f),
                 Color.Transparent
             )
             beam to smoke
        }
        "Aurora" -> { // Formerly Electric Cyan (Green)
             val beam = NeonLime
             val smoke = listOf(
                 NeonLime.copy(alpha = 0.6f + (animatedSpread * 0.3f)),
                 EmeraldGreen.copy(alpha = 0.4f + (animatedSpread * 0.2f)),
                 DarkForest.copy(alpha = 0.2f),
                 Color.Transparent
             )
             beam to smoke
        }
        else -> { // Abyss (Formerly Midnight Blue)
             val beam = ElectricBlue
             val smoke = listOf(
                 ElectricBlue.copy(alpha = 0.6f + (animatedSpread * 0.3f)),
                 DeepCyan.copy(alpha = 0.3f + (animatedSpread * 0.2f)),
                 Color.Transparent
             )
             beam to smoke
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // --- SMOKE GLOW (Background Aura) ---
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxBeamHeight)
                .alpha(1f)
                .scale(scaleX = 1.5f + (animatedSpread * 4.0f), scaleY = 1.2f)
        ) {
            drawCircle(
                brush = Brush.radialGradient(colors = smokeGradient)
            )
        }

        // --- BEAM CONTAINER ---
        Box(
            modifier = Modifier
                .height(maxBeamHeight)
                .width(24.dp)
                .clip(RoundedCornerShape(50)), // Clip entire beam for round caps
            contentAlignment = Alignment.TopCenter
        ) {
            // 1. Base Active Beam
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                beamColor,
                                Color.White,
                                beamColor
                            )
                        )
                    )
            )

            // 2. Burnt Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(burntHeight)
                    .background(
                        color = Color(0xFF555555) // Ash Gray
                    )
            )
        }
        
        // Clip the entire beam container to ensure the overlay stays inside the capsule shape
        // We can do this by applying clip to the Container Box instead.

        // Floating Close Button
        Box(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = onSessionComplete,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 40.dp, start = 16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
            }
        }
    }
}
