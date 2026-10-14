package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaSurfaceCard
import com.example.ui.theme.NovaTextPrimary

data class QuickPrompt(
    val label: String,
    val promptText: String,
    val icon: ImageVector
)

@Composable
fun QuickCommandChips(
    onCommandSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val prompts = listOf(
        QuickPrompt("Open YouTube", "Nova, open YouTube", Icons.Default.PlayArrow),
        QuickPrompt("Kaise ho Nova?", "Kaise ho Nova?", Icons.AutoMirrored.Filled.Chat),
        QuickPrompt("Open Chrome", "Nova, open Chrome", Icons.Default.Language),
        QuickPrompt("Open WhatsApp", "Nova, open WhatsApp", Icons.Default.SmartToy),
        QuickPrompt("Open Camera", "Nova, open camera", Icons.Default.CameraAlt),
        QuickPrompt("Build APK Guide", "How do I build the Android APK for this project?", Icons.Default.Android)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        prompts.forEach { prompt ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(NovaSurfaceCard)
                    .border(1.dp, NovaBorder, RoundedCornerShape(16.dp))
                    .clickable { onCommandSelected(prompt.promptText) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("quick_chip_${prompt.label.lowercase().replace(" ", "_")}")
            ) {
                Icon(
                    imageVector = prompt.icon,
                    contentDescription = null,
                    tint = NovaCyan,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 4.dp)
                )
                Text(
                    text = prompt.label,
                    color = NovaTextPrimary,
                    fontSize = 12.sp
                )
            }
        }
    }
}
