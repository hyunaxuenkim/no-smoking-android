package com.xuen.breathefree.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xuen.breathefree.ui.theme.ElectricBlue
import com.xuen.breathefree.ui.theme.DeepCyan
import com.xuen.breathefree.ui.theme.ThemeGradient
import com.xuen.breathefree.ui.viewmodel.DashboardViewModel

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import com.xuen.breathefree.ui.theme.MutedSkyBlue

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToBreathing: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding() // Handled safe area for Status Bar and Navigation Bar
            .padding(24.dp)
    ) {
        // Header (Fixed)
        Column {
            Text(
                text = "IGNITE CONTROL",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = "REFINED BREATH MASTERY",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Content Area (Flexible)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Stats Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatsCard(
                    title = "${uiState.daysClean}",
                    subtitle = "DAYS CLEAN",
                    icon = Icons.Default.DateRange,
                    modifier = Modifier.weight(1f)
                )
                StatsCard(
                    title = "${uiState.totalSessions}",
                    subtitle = "SESSIONS",
                    icon = Icons.Default.Info,
                    modifier = Modifier.weight(1f)
                )
            }

            // Central Graphic Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.2f) // Give more space to the visualization
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1E1E)),
                contentAlignment = Alignment.Center
            ) {
                Text("Bonfire Visualization", color = Color.Gray)
            }
            
            // Weekly Chart 
            WeeklyChart(weeklyStats = uiState.weeklyStats)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom Actions (Fixed)
        Column(
           verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Action Button

            Button(
                onClick = onNavigateToBreathing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MutedSkyBlue), // Solid color
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "START SESSION",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
            
            // Settings Button (Matching Style, Solid Color)
            Button(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MutedSkyBlue), // Matching Start Session
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "SETTINGS",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun WeeklyChart(weeklyStats: List<Int>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("RESPIRATION FLOW", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .background(Color(0xFF2D2D2D), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("WEEKLY", color = Color.Gray, fontSize = 10.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Bar Chart using Real Data
        // Max value for scaling
        val maxSessions = (weeklyStats.maxOrNull() ?: 1).coerceAtLeast(10) // Minimum 10 scale
        
        Row(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            weeklyStats.forEachIndexed { index, count ->
                val barHeightRatio = count / maxSessions.toFloat()
                // Min height 4dp so it's visible even if 0
                val heightPercent = barHeightRatio.coerceAtLeast(0.05f) 
                
                // Highlight today (last item)
                val isToday = index == weeklyStats.lastIndex
                
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .fillMaxHeight(heightPercent)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isToday) ElectricBlue else Color(0xFF333333)
                        )
                )
            }
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(120.dp)
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(24.dp))
            .background(Color(0xFF121212), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ElectricBlue,
            modifier = Modifier.align(Alignment.TopEnd)
        )
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}
