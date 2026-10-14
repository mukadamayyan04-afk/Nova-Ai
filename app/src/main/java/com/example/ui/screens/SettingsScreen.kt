package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiClient
import com.example.data.model.AppSettings
import com.example.data.model.AssistantLanguage
import com.example.ui.theme.NovaAmber
import com.example.ui.theme.NovaBackground
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaCyanBright
import com.example.ui.theme.NovaGreen
import com.example.ui.theme.NovaRed
import com.example.ui.theme.NovaSurfaceCard
import com.example.ui.theme.NovaSurfaceElevated
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onUpdateSettings: (AppSettings) -> Unit,
    onTestVoice: (String) -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var customApiKeyInput by remember(settings.customGeminiApiKey) {
        mutableStateOf(settings.customGeminiApiKey)
    }

    val isConfigured = GeminiClient.getEffectiveApiKey(settings.customGeminiApiKey).isNotEmpty()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NovaBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            // AI Service Configuration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NovaSurfaceCard)
                    .border(1.dp, NovaBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = NovaCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GEMINI AI ENGINE",
                                color = NovaCyanBright,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = if (isConfigured) "● ACTIVE" else "● NO KEY",
                            color = if (isConfigured) NovaGreen else NovaAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "NOVA uses Google Gemini 2.5/3.5 Flash for conversational responses. Get a 100% free API key from Google AI Studio (https://aistudio.google.com/).",
                        color = NovaTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = customApiKeyInput,
                        onValueChange = { customApiKeyInput = it },
                        placeholder = {
                            Text(
                                "Enter custom Gemini API Key (Optional)",
                                color = NovaTextMuted,
                                fontSize = 12.sp
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NovaTextPrimary,
                            unfocusedTextColor = NovaTextPrimary,
                            focusedBorderColor = NovaCyan,
                            unfocusedBorderColor = NovaBorder
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_api_key_field")
                    )

                    Button(
                        onClick = {
                            onUpdateSettings(settings.copy(customGeminiApiKey = customApiKeyInput.trim()))
                            Toast.makeText(context, "API Key Saved", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NovaCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("save_api_key_button")
                    ) {
                        Text(
                            text = "SAVE API KEY",
                            color = NovaBackground,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Language & Persona Section
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NovaSurfaceCard)
                    .border(1.dp, NovaBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = NovaCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LANGUAGE & ACCENT",
                            color = NovaCyanBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    AssistantLanguage.values().forEach { lang ->
                        val isSelected = settings.language == lang
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) NovaSurfaceElevated else NovaBackground)
                                .border(
                                    1.dp,
                                    if (isSelected) NovaCyan else NovaBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = lang.label,
                                color = if (isSelected) NovaCyanBright else NovaTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )

                            Button(
                                onClick = { onUpdateSettings(settings.copy(language = lang)) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) NovaCyan else NovaSurfaceElevated
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(30.dp)
                                    .testTag("select_language_${lang.name.lowercase()}")
                            ) {
                                Text(
                                    text = if (isSelected) "Selected" else "Select",
                                    color = if (isSelected) NovaBackground else NovaTextPrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Voice & Speech Tuning
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NovaSurfaceCard)
                    .border(1.dp, NovaBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = NovaCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SPEECH & TEXT-TO-SPEECH",
                                color = NovaCyanBright,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Switch(
                            checked = settings.voiceOutputEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(voiceOutputEnabled = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NovaCyan,
                                checkedTrackColor = NovaSurfaceElevated
                            ),
                            modifier = Modifier.testTag("toggle_voice_output")
                        )
                    }

                    // Speech Speed
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Speech Speed", color = NovaTextPrimary, fontSize = 13.sp)
                            Text(
                                text = String.format("%.2fx", settings.speechRate),
                                color = NovaCyan,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Slider(
                            value = settings.speechRate,
                            onValueChange = { onUpdateSettings(settings.copy(speechRate = it)) },
                            valueRange = 0.6f..1.6f,
                            colors = SliderDefaults.colors(
                                thumbColor = NovaCyan,
                                activeTrackColor = NovaCyan,
                                inactiveTrackColor = NovaSurfaceElevated
                            ),
                            modifier = Modifier.testTag("slider_speech_speed")
                        )
                    }

                    // Speech Pitch
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Voice Pitch", color = NovaTextPrimary, fontSize = 13.sp)
                            Text(
                                text = String.format("%.2fx", settings.speechPitch),
                                color = NovaCyan,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Slider(
                            value = settings.speechPitch,
                            onValueChange = { onUpdateSettings(settings.copy(speechPitch = it)) },
                            valueRange = 0.7f..1.4f,
                            colors = SliderDefaults.colors(
                                thumbColor = NovaCyan,
                                activeTrackColor = NovaCyan,
                                inactiveTrackColor = NovaSurfaceElevated
                            ),
                            modifier = Modifier.testTag("slider_speech_pitch")
                        )
                    }

                    Button(
                        onClick = {
                            val testPhrase = when (settings.language) {
                                AssistantLanguage.HINDI -> "नमस्ते, मैं नोवा हूँ। आवाज़ की जांच सफल रही।"
                                AssistantLanguage.HINGLISH -> "Hello boss, main NOVA hoon. Voice testing successful!"
                                AssistantLanguage.ENGLISH -> "Systems operational. NOVA voice test completed."
                            }
                            onTestVoice(testPhrase)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NovaSurfaceElevated),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("test_voice_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = NovaCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("TEST VOICE OUTPUT", color = NovaCyan, fontSize = 12.sp)
                    }
                }
            }
        }

        // Toggles & Wake Word
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NovaSurfaceCard)
                    .border(1.dp, NovaBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "WAKE WORD & CONTROLS",
                        color = NovaCyanBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    // Wake Word
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Wake Word 'Nova'", color = NovaTextPrimary, fontSize = 13.sp)
                            Text("Responds with 'Haan bro, bolo?'", color = NovaTextMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = settings.wakeWordEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(wakeWordEnabled = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NovaCyan,
                                checkedTrackColor = NovaSurfaceElevated
                            ),
                            modifier = Modifier.testTag("toggle_wake_word")
                        )
                    }

                    // Sound Effects
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Futuristic Sound Effects", color = NovaTextPrimary, fontSize = 13.sp)
                            Text("High-tech audio chimes for voice actions", color = NovaTextMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = settings.soundEffectsEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(soundEffectsEnabled = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NovaCyan,
                                checkedTrackColor = NovaSurfaceElevated
                            ),
                            modifier = Modifier.testTag("toggle_sound_effects")
                        )
                    }

                    // Battery Saver / Reduced Animations
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reduced Animation Mode", color = NovaTextPrimary, fontSize = 13.sp)
                            Text("Battery saver for Redmi 13 5G", color = NovaTextMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = settings.reducedAnimationMode,
                            onCheckedChange = { onUpdateSettings(settings.copy(reducedAnimationMode = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NovaCyan,
                                checkedTrackColor = NovaSurfaceElevated
                            ),
                            modifier = Modifier.testTag("toggle_reduced_animation")
                        )
                    }

                    // Background Wake Word explanation card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(NovaSurfaceElevated)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "ℹ️ BACKGROUND WAKE WORD NOTE",
                                color = NovaAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "In-app 'Nova' detection is fully functional while foreground mic is active. For continuous 24/7 background detection with screen off, Android 14+ requires a persistent Foreground Service with microphone permissions and disabling MIUI/HyperOS aggressive battery saver for NOVA.",
                                color = NovaTextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // How to Build APK Guide
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NovaSurfaceCard)
                    .border(1.dp, NovaBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = null,
                            tint = NovaCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HOW TO BUILD ANDROID APK",
                            color = NovaCyanBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "This project is 100% native Android (Kotlin + Jetpack Compose + Gradle). You can build and install the APK on any phone (like Redmi 13 5G):",
                        color = NovaTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Text(
                        text = "1. GitHub Actions (Automated & Free):\n" +
                                "Push this repo to GitHub. A workflow (.github/workflows/build-apk.yml) compiles the project and generates the APK under Actions > Artifacts.\n\n" +
                                "2. Local Android Studio (Computer):\n" +
                                "Export ZIP from AI Studio settings > Open in Android Studio > Build > Build Bundle(s) / APK(s) > Build APK(s).\n\n" +
                                "3. Directly on Android Phone (Termux):\n" +
                                "Install Termux > Install openjdk-17 > Run 'gradle assembleDebug' inside the project folder.",
                        color = NovaTextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Privacy & Data
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NovaSurfaceCard)
                    .border(1.dp, NovaBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = NovaCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PRIVACY & LOCAL DATA",
                            color = NovaCyanBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "NOVA stores chat history and preferences locally on your device. We do not track, upload, or sell your data.",
                        color = NovaTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Button(
                        onClick = onClearAllData,
                        colors = ButtonDefaults.buttonColors(containerColor = NovaRed.copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("clear_all_data_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = NovaBackground,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CLEAR ALL CHAT HISTORY & MEMORY",
                            color = NovaBackground,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
