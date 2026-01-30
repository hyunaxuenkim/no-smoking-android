package com.xuen.breathefree.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
    // 2 minutes to burn passively
    val passiveBurnRate = 1f / 120f 
    // Tuned factor for active breathing
    val activeBurnFactor = 0.015f
    val noiseThreshold = 0.1f

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
        
        val updateInterval = 50L // 50ms
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
    // Smooth out the amplitude for the smoke width to make it less jittery
    val animatedSpread by animateFloatAsState(
        targetValue = amplitude,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "smoke_spread"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // --- The "Cigarette/Beam" Visual ---
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f) // Fill upper space
                    .fillMaxWidth()
            ) {
                // 1. SMOKE/MIST LAYER (Behind)
                // Spreads horizontally based on breath
                Box(
                    modifier = Modifier
                        .height(300.dp) // Same height as beam
                        // Width = Base + (Screen Width * Spread)
                        // We use a fraction of max width for the spread
                        .fillMaxWidth(0.1f + (animatedSpread * 0.9f)) 
                        .alpha(0.6f + (animatedSpread * 0.4f)) // Gets more opaque when blowing
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent, // Fade far left
                                    Color(0xFF00BFFF).copy(alpha = 0.5f), // Soft Blue Mist
                                    Color.White.copy(alpha = 0.8f), // Dense center
                                    Color(0xFF00BFFF).copy(alpha = 0.5f), // Soft Blue Mist
                                    Color.Transparent // Fade far right
                                )
                            ),
                            shape = RoundedCornerShape(100) // Soft edges
                        )
                )

                // 2. GLOWING BEAM LAYER (The "Icon")
                // Always visible, anchors the visual
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(300.dp)
                        .scale(1f + (animatedSpread * 0.05f)) // Slight pulse
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF00BFFF), // Bright Blue
                                    Color.White,
                                    Color(0xFF00BFFF)
                                )
                            ),
                            shape = RoundedCornerShape(50)
                        )
                )
            }

            // --- Bottom Controls ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp)
            ) {
                Text(
                    text = "IGNITING...",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)
                )
                
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color.DarkGray, CircleShape),
                    color = BurntOrange, // Keep the orange accent for "Heat"
                    trackColor = Color.DarkGray,
                )
            }
        }
        
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
