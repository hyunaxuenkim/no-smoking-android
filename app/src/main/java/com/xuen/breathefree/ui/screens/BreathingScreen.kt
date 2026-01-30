package com.xuen.breathefree.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
    // 2 minutes (120s) to burn passively -> 1/120 per second
    val passiveBurnRate = 1f / 120f 
    // Tuned: LOWERED factor to prevent 'too fast' finish. 
    // Was 0.035, now 0.015. Requires really intentional breathing.
    val activeBurnFactor = 0.015f
    // Noise gate: ignore quiet sounds (background noise)
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

    // Main Game Loop: Updates amplitude and progress
    LaunchedEffect(hasAudioPermission) {
        if (!hasAudioPermission) return@LaunchedEffect
        
        val updateInterval = 50L // 50ms
        val dt = updateInterval / 1000f // Delta time in seconds

        while (progress < 1f) {
            val rawAmp = analyzer.getAmplitude()
            // Apply Noise Gate
            val effectiveAmp = if (rawAmp > noiseThreshold) rawAmp else 0f
            
            amplitude = effectiveAmp // Use gated value for visual too
            
            // Calculate progress increment
            val passiveBurn = passiveBurnRate * dt
            val activeBurn = effectiveAmp * activeBurnFactor * dt
            
            progress = (progress + passiveBurn + activeBurn).coerceAtMost(1f)
            
            if (progress >= 1f) {
                onSessionComplete()
            }
            
            delay(updateInterval)
        }
    }

    // Dynamic Scale based on Audio
    val targetScale = 1f + (amplitude * 2.5f)
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(100), // Quick response
        label = "audio_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp), // Global padding
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Dynamic Flame Visual
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(animatedScale) 
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                BurntOrange.copy(alpha=0.8f), 
                                DeepCyan.copy(alpha=0.6f), 
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
                    .alpha(0.8f)
            )
        }

        // Progress Bar (Heat Streak)
        // Moved up and made thicker for visibility
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 40.dp) // Lift up from gesture bar
        ) {
            Text(
                text = "Burning...",
                color = Color.Gray,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)
            )
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp) // Thicker
                    .background(Color.DarkGray, CircleShape),
                color = BurntOrange,
                trackColor = Color.DarkGray,
            )
        }

        // Close Button
        IconButton(
            onClick = onSessionComplete,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 24.dp) // Safe area
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
        }
    }
}
