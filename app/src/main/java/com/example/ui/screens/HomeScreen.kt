package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssistantLanguage
import com.example.data.model.AssistantState
import com.example.ui.components.HolographicCore
import com.example.ui.components.QuickCommandChips
import com.example.ui.theme.NovaAmber
import com.example.ui.theme.NovaBackground
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaCyanBright
import com.example.ui.theme.NovaElectricBlue
import com.example.ui.theme.NovaGreen
import com.example.ui.theme.NovaRed
import com.example.ui.theme.NovaSurfaceCard
import com.example.ui.theme.NovaSurfaceElevated
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary

@Composable
fun HomeScreen(
    state: AssistantState,
    amplitude: Float,
    liveSpeechText: String,
    errorMessage: String?,
    reducedAnimation: Boolean,
    isGeminiConfigured: Boolean,
    currentLanguage: AssistantLanguage,
    hasMicPermission: Boolean,
    onLanguageSelected: (AssistantLanguage) -> Unit,
    onRequestMicPermission: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onCoreClick: () -> Unit,
    onQuickCommand: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NovaBackground)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // State Pill Banner
        val (stateLabel, stateColor) = when (state) {
            AssistantState.IDLE -> "SYSTEM READY // STANDBY" to NovaCyan
            AssistantState.LISTENING -> "LISTENING (${currentLanguage.name})..." to NovaGreen
            AssistantState.PROCESSING -> "PROCESSING NEURAL QUERY..." to NovaAmber
            AssistantState.SPEAKING -> "TRANSMITTING RESPONSE..." to NovaCyanBright
            AssistantState.ERROR -> "SYSTEM ALERT" to NovaRed
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(NovaSurfaceElevated)
                .border(1.dp, stateColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(stateColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stateLabel,
                    color = stateColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Bilingual Language Switcher Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NovaSurfaceCard)
                .border(1.dp, NovaBorder, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AssistantLanguage.values().forEach { lang ->
                val isSelected = currentLanguage == lang
                val labelShort = when (lang) {
                    AssistantLanguage.HINGLISH -> "Hinglish (हिन्दी+Eng)"
                    AssistantLanguage.HINDI -> "हिंदी"
                    AssistantLanguage.ENGLISH -> "English"
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NovaCyan else NovaSurfaceElevated)
                        .border(
                            1.dp,
                            if (isSelected) NovaCyanBright else NovaBorder.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onLanguageSelected(lang) }
                        .padding(vertical = 6.dp)
                        .testTag("home_lang_chip_${lang.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = labelShort,
                        color = if (isSelected) NovaBackground else NovaTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Microphone Permission Warning Card if not granted
        if (!hasMicPermission) {
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(NovaSurfaceCard)
                    .border(1.dp, NovaAmber.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
                    .testTag("mic_permission_card")
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = NovaAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MICROPHONE PERMISSION NEEDED",
                            color = NovaAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "To speak voice commands in Hindi, Hinglish, or English, NOVA requires microphone access.",
                        color = NovaTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onRequestMicPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = NovaCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("grant_mic_permission_button")
                        ) {
                            Text(
                                text = "GRANT PERMISSION",
                                color = NovaBackground,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = onOpenAppSettings,
                            colors = ButtonDefaults.buttonColors(containerColor = NovaSurfaceElevated),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("app_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = NovaCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Settings", color = NovaCyan, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Central Holographic Arc Reactor
        HolographicCore(
            state = state,
            amplitude = amplitude,
            reducedAnimation = reducedAnimation,
            onCoreClick = onCoreClick
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Tap instructions
        Text(
            text = when (state) {
                AssistantState.LISTENING -> "Listening in ${currentLanguage.label}... Tap to finish"
                AssistantState.PROCESSING -> "NOVA is analyzing neural input..."
                AssistantState.SPEAKING -> "NOVA is speaking... Tap to stop"
                else -> "Tap core to speak in ${currentLanguage.name.lowercase()}"
            },
            color = NovaTextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Live streaming recognized speech card
        AnimatedVisibility(
            visible = liveSpeechText.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NovaSurfaceCard)
                    .border(1.dp, NovaBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (state == AssistantState.LISTENING) "STREAMING VOICE INPUT:" else "VOICE TRANSCRIPT:",
                            color = if (state == AssistantState.LISTENING) NovaGreen else NovaCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        if (state == AssistantState.LISTENING) {
                            Text(
                                text = "LIVE ●",
                                color = NovaGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"$liveSpeechText\"",
                        color = NovaTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Error banner if any
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NovaSurfaceCard)
                    .border(1.dp, NovaRed.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "ALERT",
                        color = NovaRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage,
                        color = NovaTextPrimary,
                        fontSize = 13.sp
                    )
                    if (!isGeminiConfigured) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onNavigateToSettings,
                            colors = ButtonDefaults.buttonColors(containerColor = NovaCyan),
                            modifier = Modifier.testTag("configure_key_home_button")
                        ) {
                            Text("Configure API Key in Settings", color = NovaBackground, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Command Chips
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "QUICK COMMANDS",
                color = NovaTextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
            QuickCommandChips(onCommandSelected = onQuickCommand)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Big Voice Mic Button
        Button(
            onClick = onCoreClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state == AssistantState.LISTENING) NovaGreen else NovaCyan
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .height(52.dp)
                .testTag("home_mic_action_button")
        ) {
            Icon(
                imageVector = if (state == AssistantState.LISTENING) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = null,
                tint = NovaBackground,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (state == AssistantState.LISTENING) "STOP LISTENING" else "START BILINGUAL VOICE INPUT",
                color = NovaBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
