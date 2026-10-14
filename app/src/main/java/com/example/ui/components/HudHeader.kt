package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssistantState
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaCyanBright
import com.example.ui.theme.NovaGreen
import com.example.ui.theme.NovaRed
import com.example.ui.theme.NovaSurfaceElevated
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary

@Composable
fun HudHeader(
    assistantState: AssistantState,
    isGeminiConfigured: Boolean,
    soundEffectsEnabled: Boolean,
    reducedAnimation: Boolean,
    onToggleSoundEffects: () -> Unit,
    onToggleReducedAnimation: () -> Unit,
    onStopSpeaking: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App title & protocol
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(if (isGeminiConfigured) NovaGreen else NovaCyan)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "NOVA AI",
                    color = NovaCyanBright,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                )
            }
            Text(
                text = "JARVIS PROTOCOL v2.4",
                color = NovaTextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Action controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Stop speaking button if active
            if (assistantState == AssistantState.SPEAKING) {
                IconButton(
                    onClick = onStopSpeaking,
                    modifier = Modifier.testTag("hud_stop_speaking_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.StopCircle,
                        contentDescription = "Stop Speech",
                        tint = NovaRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Reduced Animation / Battery Saver
            IconButton(
                onClick = onToggleReducedAnimation,
                modifier = Modifier.testTag("hud_battery_saver_button")
            ) {
                Icon(
                    imageVector = Icons.Default.BatterySaver,
                    contentDescription = "Toggle Battery Saver Animation Mode",
                    tint = if (reducedAnimation) NovaCyan else NovaTextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Mute / Unmute Sound FX
            IconButton(
                onClick = onToggleSoundEffects,
                modifier = Modifier.testTag("hud_mute_sound_button")
            ) {
                Icon(
                    imageVector = if (soundEffectsEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
                    contentDescription = "Toggle Sound Effects",
                    tint = if (soundEffectsEnabled) NovaCyan else NovaTextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }

            // AI Status Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(NovaSurfaceElevated)
                    .border(1.dp, NovaBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isGeminiConfigured) "AI: GEMINI" else "AI: SETUP",
                    color = if (isGeminiConfigured) NovaGreen else NovaCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
