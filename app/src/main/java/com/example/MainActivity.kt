package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.api.GeminiClient
import com.example.ui.NovaViewModel
import com.example.ui.components.HudHeader
import com.example.ui.screens.AppCommandsScreen
import com.example.ui.screens.AppLauncherScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NovaBackground
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaCyanBright
import com.example.ui.theme.NovaSurfaceCard
import com.example.ui.theme.NovaSurfaceElevated
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary

enum class NovaScreen(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    CHAT("Chat", Icons.AutoMirrored.Filled.Chat),
    COMMANDS("Launcher", Icons.Default.Apps),
    NOTIFICATIONS("Alerts", Icons.Default.Notifications),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: NovaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                NovaApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun NovaApp(viewModel: NovaViewModel) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(NovaScreen.HOME) }

    val assistantState by viewModel.assistantState.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val amplitude by viewModel.audioAmplitude.collectAsState()
    val liveSpeechText by viewModel.liveSpeechText.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    val isGeminiConfigured = GeminiClient.getEffectiveApiKey(settings.customGeminiApiKey).isNotEmpty()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Permission launcher for RECORD_AUDIO
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            viewModel.startListening()
        } else {
            Toast.makeText(context, "Microphone permission is required for voice commands.", Toast.LENGTH_LONG).show()
        }
    }

    val onOpenAppSettings: () -> Unit = {
        val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", context.packageName, null)
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    val onMicAction: () -> Unit = {
        hasMicPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (hasMicPermission) {
            viewModel.toggleListening()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Back handler for sub-screens to return to Home
    if (currentScreen != NovaScreen.HOME) {
        BackHandler {
            currentScreen = NovaScreen.HOME
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = NovaBackground,
        topBar = {
            HudHeader(
                assistantState = assistantState,
                isGeminiConfigured = isGeminiConfigured,
                soundEffectsEnabled = settings.soundEffectsEnabled,
                reducedAnimation = settings.reducedAnimationMode,
                onToggleSoundEffects = {
                    viewModel.updateSettings(settings.copy(soundEffectsEnabled = !settings.soundEffectsEnabled))
                },
                onToggleReducedAnimation = {
                    viewModel.updateSettings(settings.copy(reducedAnimationMode = !settings.reducedAnimationMode))
                },
                onStopSpeaking = { viewModel.stopSpeaking() }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = NovaSurfaceCard,
                modifier = Modifier
                    .border(1.dp, NovaBorder)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                NovaScreen.values().forEach { screen ->
                    val selected = currentScreen == screen
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.label,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NovaCyanBright,
                            selectedTextColor = NovaCyanBright,
                            indicatorColor = NovaSurfaceElevated,
                            unselectedIconColor = NovaTextMuted,
                            unselectedTextColor = NovaTextMuted
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                NovaScreen.HOME -> {
                    HomeScreen(
                        state = assistantState,
                        amplitude = amplitude,
                        liveSpeechText = liveSpeechText,
                        errorMessage = errorMessage,
                        reducedAnimation = settings.reducedAnimationMode,
                        isGeminiConfigured = isGeminiConfigured,
                        currentLanguage = settings.language,
                        hasMicPermission = hasMicPermission,
                        onLanguageSelected = { newLang ->
                            viewModel.updateSettings(settings.copy(language = newLang))
                        },
                        onRequestMicPermission = {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        onOpenAppSettings = onOpenAppSettings,
                        onCoreClick = onMicAction,
                        onQuickCommand = { prompt ->
                            viewModel.submitTextMessage(prompt)
                            currentScreen = NovaScreen.CHAT
                        },
                        onNavigateToSettings = { currentScreen = NovaScreen.SETTINGS }
                    )
                }
                NovaScreen.CHAT -> {
                    ChatScreen(
                        messages = chatMessages,
                        assistantState = assistantState,
                        onSendMessage = { viewModel.submitTextMessage(it) },
                        onToggleMic = onMicAction,
                        onSpeakMessage = { viewModel.speakText(it) },
                        onClearChat = { viewModel.clearChatHistory() }
                    )
                }
                NovaScreen.COMMANDS -> {
                    AppLauncherScreen(
                        onTriggerVoiceCommand = { command ->
                            viewModel.submitTextMessage(command)
                            currentScreen = NovaScreen.CHAT
                        }
                    )
                }
                NovaScreen.NOTIFICATIONS -> {
                    NotificationsScreen(
                        settings = settings,
                        notifications = notifications,
                        onUpdateSettings = { viewModel.updateSettings(it) },
                        onTestNotification = { item ->
                            viewModel.speakText("Bro, ${item.appName} se ek naya message aaya hai. ${item.title} kehta hai: ${item.content}")
                        },
                        onClearNotifications = { viewModel.clearNotifications() }
                    )
                }
                NovaScreen.SETTINGS -> {
                    SettingsScreen(
                        settings = settings,
                        onUpdateSettings = { viewModel.updateSettings(it) },
                        onTestVoice = { viewModel.speakText(it) },
                        onClearAllData = {
                            viewModel.clearChatHistory()
                            viewModel.clearNotifications()
                            Toast.makeText(context, "Memory & Cache Purged", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}
