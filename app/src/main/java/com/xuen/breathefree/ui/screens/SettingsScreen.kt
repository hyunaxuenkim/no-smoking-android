package com.xuen.breathefree.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xuen.breathefree.ui.theme.BurntOrange
import com.xuen.breathefree.ui.theme.DeepCyan
import com.xuen.breathefree.ui.theme.ElectricBlue
import com.xuen.breathefree.ui.theme.ThemeGradient
import com.xuen.breathefree.ui.theme.MutedSkyBlue

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    var hapticPulse by remember { mutableStateOf(true) }
    var ambientCrackle by remember { mutableStateOf(false) }
    var sensitivity by remember { mutableFloatStateOf(0.85f) }
    var selectedFlame by remember { mutableStateOf("Ember Orange") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding() 
            .padding(24.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ElectricBlue
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Flame Settings",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Content Area (Flexible)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
             // Flame Aura Selection
            Text(
                text = "Flame Aura",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                text = "SELECT PALETTE",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.End)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val flames = listOf("Ember Orange", "Electric Cyan", "Midnight Blue")
                items(flames) { flame ->
                    FlameCard(
                        name = flame,
                        isSelected = flame == selectedFlame,
                        onClick = { selectedFlame = flame }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Experience Controls
            Text(
                text = "Experience",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF121212), RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                // Haptic Pulse
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Haptic Pulse", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Vibrate during inhale peaks", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                    }
                    Switch(
                        checked = hapticPulse,
                        onCheckedChange = { hapticPulse = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ElectricBlue,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.DarkGray
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Breath Sensitivity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                     Column {
                        Text("Breath Sensitivity", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Adjust microphone response", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                    }
                    Text("${(sensitivity * 100).toInt()}%", color = ElectricBlue, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = sensitivity,
                    onValueChange = { sensitivity = it },
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricBlue,
                        activeTrackColor = ElectricBlue,
                        inactiveTrackColor = Color.DarkGray
                    )
                )

                 Spacer(modifier = Modifier.height(24.dp))

                // Ambient Crackle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Ambient Crackle", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Fireplace ASMR sounds", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                    }
                    Switch(
                        checked = ambientCrackle,
                        onCheckedChange = { ambientCrackle = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ElectricBlue,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.DarkGray
                        )
                    )
                }
            }
        }
       
        Spacer(modifier = Modifier.height(24.dp))

        // Apply Button

        Button(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MutedSkyBlue), // Solid Color
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "Apply Theme",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

@Composable
fun FlameCard(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFF1E1E1E) else Color(0xFF121212))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) ElectricBlue else Color(0xFF333333),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        // Flame Visual Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (name.contains("Orange")) BurntOrange else if (name.contains("Cyan")) DeepCyan else Color.Blue
                )
                .align(Alignment.TopCenter)
        )
        
        Column(
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                color = Color.White
            )
             Text(
                text = "Classic Wildfire", // subtext placeholder
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
        
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Add, // Checkmark placeholder
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(ElectricBlue.copy(alpha=0.5f), CircleShape)
                    .padding(4.dp)
                    .size(16.dp)
            )
        }
    }
}
