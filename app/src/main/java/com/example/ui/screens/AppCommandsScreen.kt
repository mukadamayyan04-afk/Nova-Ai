package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SearchableInstalledAppsList
import com.example.ui.theme.NovaBackground
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaCyanBright
import com.example.ui.theme.NovaElectricBlue
import com.example.ui.theme.NovaSurfaceCard
import com.example.ui.theme.NovaSurfaceElevated
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.util.NovaAppLauncher

data class CommandFeature(
    val title: String,
    val description: String,
    val voiceTriggers: List<String>,
    val icon: ImageVector,
    val onExecute: (android.content.Context) -> Unit
)

enum class AppCommandsTab(val label: String, val icon: ImageVector) {
    INSTALLED_APPS("Installed Apps", Icons.Default.Apps),
    FEATURED_INTENTS("Featured Intents", Icons.Default.Tune)
}

@Composable
fun AppCommandsScreen(
    onTriggerVoiceCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(AppCommandsTab.INSTALLED_APPS) }

    val features = listOf(
        CommandFeature(
            title = "YouTube",
            description = "Opens the YouTube application or falls back to youtube.com",
            voiceTriggers = listOf("Nova, open YouTube", "YouTube kholo", "Play YouTube"),
            icon = Icons.Default.PlayArrow,
            onExecute = { NovaAppLauncher.launchYouTube(it) }
        ),
        CommandFeature(
            title = "Google Chrome",
            description = "Launches Chrome browser or default device browser",
            voiceTriggers = listOf("Nova, open Chrome", "Chrome browser kholo"),
            icon = Icons.Default.Language,
            onExecute = { NovaAppLauncher.launchChrome(it) }
        ),
        CommandFeature(
            title = "Google Search",
            description = "Opens the Google App or performs quick web search queries",
            voiceTriggers = listOf("Nova, open Google", "Google search [query]"),
            icon = Icons.Default.Search,
            onExecute = { NovaAppLauncher.launchGoogle(it) }
        ),
        CommandFeature(
            title = "WhatsApp",
            description = "Launches WhatsApp messenger directly via package intent",
            voiceTriggers = listOf("Nova, open WhatsApp", "WhatsApp kholo"),
            icon = Icons.Default.SmartToy,
            onExecute = { NovaAppLauncher.launchWhatsApp(it) }
        ),
        CommandFeature(
            title = "Camera",
            description = "Triggers the native Android Camera capture intent",
            voiceTriggers = listOf("Nova, open camera", "Camera chalao", "Take a picture"),
            icon = Icons.Default.CameraAlt,
            onExecute = { NovaAppLauncher.launchCamera(it) }
        ),
        CommandFeature(
            title = "Device Settings",
            description = "Opens phone system settings for quick connectivity and sound access",
            voiceTriggers = listOf("Nova, open settings", "Phone settings kholo"),
            icon = Icons.Default.Settings,
            onExecute = { NovaAppLauncher.launchSettings(it) }
        ),
        CommandFeature(
            title = "Phone Dialer",
            description = "Opens phone dialer with number ready for user verification",
            voiceTriggers = listOf("Nova, call 100", "Open dialer", "Dial [number]"),
            icon = Icons.Default.Phone,
            onExecute = { NovaAppLauncher.launchDialer(it) }
        ),
        CommandFeature(
            title = "Custom Website",
            description = "Opens any web URL in default Android browser",
            voiceTriggers = listOf("Nova, open github.com", "Open website wikipedia.org"),
            icon = Icons.Default.Web,
            onExecute = { NovaAppLauncher.openUrl(it, "https://github.com", "GitHub") }
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NovaBackground)
    ) {
        // Segmented Tab Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NovaSurfaceCard)
                .border(1.dp, NovaBorder, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AppCommandsTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NovaSurfaceElevated else NovaBackground)
                        .border(
                            1.dp,
                            if (isSelected) NovaCyan else NovaBorder.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedTab = tab }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        tint = if (isSelected) NovaCyanBright else NovaTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tab.label,
                        color = if (isSelected) NovaCyanBright else NovaTextMuted,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        when (selectedTab) {
            AppCommandsTab.INSTALLED_APPS -> {
                SearchableInstalledAppsList(
                    onTriggerVoiceCommand = onTriggerVoiceCommand,
                    headerContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(NovaSurfaceElevated)
                                .border(1.dp, NovaBorder, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "💡 Tap 'Open' to trigger the launch intent directly, or tap the voice command chip to command NOVA via natural voice.",
                                color = NovaTextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                )
            }

            AppCommandsTab.FEATURED_INTENTS -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        // Header Info Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(NovaSurfaceElevated)
                                .border(1.dp, NovaBorder, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "ANDROID INTENTS ENGINE",
                                    color = NovaCyanBright,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "NOVA runs as a native Android app with explicit Package & Intent bridges to control supported installed apps, dialers, and web links seamlessly.",
                                    color = NovaTextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    items(features) { feature ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(NovaSurfaceCard)
                                .border(1.dp, NovaBorder, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                                .testTag("command_card_${feature.title.lowercase().replace(" ", "_")}")
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(NovaSurfaceElevated),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = feature.icon,
                                                contentDescription = null,
                                                tint = NovaCyan,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = feature.title,
                                                color = NovaTextPrimary,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = feature.description,
                                                color = NovaTextMuted,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { feature.onExecute(context) },
                                        colors = ButtonDefaults.buttonColors(containerColor = NovaCyan),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .height(34.dp)
                                            .testTag("launch_test_${feature.title.lowercase().replace(" ", "_")}")
                                    ) {
                                        Text(
                                            text = "Launch",
                                            color = NovaBackground,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Voice triggers:",
                                    color = NovaTextMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    feature.voiceTriggers.take(2).forEach { trigger ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(NovaSurfaceElevated)
                                                .clickable { onTriggerVoiceCommand(trigger) }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "\"$trigger\"",
                                                color = NovaElectricBlue,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}
