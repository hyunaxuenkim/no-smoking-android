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

@Composable
fun BreathingScreen(
    onSessionComplete: () -> Unit
) {
    val context = LocalContext.current
    var progress by remember { mutableFloatStateOf(0f) }
    var amplitude by remember { mutableFloatStateOf(0f) }
    
    // Configurable "Game" Constants
    val passiveBurnRate = 1f / 120f 
    val activeBurnFactor = 0.015f
    // Tuned: Lower threshold to catch "off-axis" blowing
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
    
    // Sensitivity Boost
    val visualInput = (amplitude * 2.5f).coerceAtMost(1f)

    // Smooth spreading animation
    val animatedSpread by animateFloatAsState(
        targetValue = visualInput,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "smoke_spread"
    )

    // Beam Dimensions
    val maxBeamHeight = 400.dp
    // Burnt Height: Grows from 0 to max as progress increases
    val burntHeight = (maxBeamHeight * progress).coerceAtMost(maxBeamHeight)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // --- SMOKE GLOW (Background Aura) ---
        // Wide Radial Gradient: "Not round" but "Spread out"
        // Increased Alpha as requested (More visible)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxBeamHeight)
                .alpha(1f) // Fully opaque canvas, control alpha in brush
                .scale(scaleX = 1.5f + (animatedSpread * 4.0f), scaleY = 1.2f)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        // Brighter/More Visible Cores
                        Color(0xFF00E5FF).copy(alpha = 0.5f + (animatedSpread * 0.3f)), // Core
                        Color(0xFF004D40).copy(alpha = 0.2f + (animatedSpread * 0.2f)), // Outer
                        Color.Transparent // Edge
                    )
                )
            )
        }

        // --- BEAM CONTAINER ---
        Box(
            modifier = Modifier
                .height(maxBeamHeight)
                .width(24.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // 1. Base Active Beam (Full Size, Blue/White Gradient)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF00BFFF), // Bright Blue
                                Color.White,
                                Color(0xFF00BFFF)
                            )
                        ),
                        shape = RectangleShape
                    )
            )

            // 2. Burnt Overlay (Grows from Top)
            // Turns the top part Dark Gray/Ash
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(burntHeight)
                    .background(
                        color = Color(0xFF555555), // Lighter Ash Gray for visibility
                        shape = RectangleShape 
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
