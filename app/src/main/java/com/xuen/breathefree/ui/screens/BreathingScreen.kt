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
    var instruction by remember { mutableStateOf("Inhale slowly") }
    var amplitude by remember { mutableFloatStateOf(0f) }
    
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

    // Polling for amplitude
    LaunchedEffect(Unit) {
        while (true) {
            val amp = analyzer.getAmplitude()
            // Smooth out the amplitude updates a bit if needed, or just set it
            amplitude = amp
            delay(50) // Update every 50ms
        }
    }

    // Simulating session progress
    LaunchedEffect(Unit) {
        val totalTime = 60000L // 60 seconds session
        val steps = 600
        val delayTime = totalTime / steps
        for (i in 1..steps) {
            progress = i / steps.toFloat()
            // Simple logic: inhale for first half, exhale second half of every 10s cycle
            val cycle = (i * delayTime) % 10000
            if (cycle < 5000) instruction = "Inhale slowly"
            else instruction = "Exhale"
            
            delay(delayTime)
        }
        onSessionComplete()
    }

    // Dynamic Scale based on Audio
    // Base scale 1f + amplitude influence
    // If amplitude is high (blowing into mic), scale increases.
    // Let's make it significant: up to 2.5x
    val targetScale = 1f + (amplitude * 2.5f)
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(100), // Quick response
        label = "audio_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = instruction,
                style = MaterialTheme.typography.displaySmall,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Dynamic Flame Visual
            Box(
                modifier = Modifier
                    .size(200.dp) // Base size
                    .scale(animatedScale) // Controlled by microphone
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

            Spacer(modifier = Modifier.height(48.dp))
        }

        // Progress Bar (Heat Streak) at bottom
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(4.dp),
            color = BurntOrange,
            trackColor = Color.DarkGray
        )

        // Close Button
        IconButton(
            onClick = onSessionComplete,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
        }
    }
}
