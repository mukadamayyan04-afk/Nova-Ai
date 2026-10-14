package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.AppSettings
import com.example.data.model.NotificationItem
import com.example.service.NovaNotificationHub
import com.example.ui.theme.NovaAmber
import com.example.ui.theme.NovaBackground
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaCyanBright
import com.example.ui.theme.NovaGreen
import com.example.ui.theme.NovaSurfaceCard
import com.example.ui.theme.NovaSurfaceDark
import com.example.ui.theme.NovaSurfaceElevated
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun NotificationsScreen(
    settings: AppSettings,
    notifications: List<NotificationItem>,
    onUpdateSettings: (AppSettings) -> Unit,
    onTestNotification: (NotificationItem) -> Unit,
    onClearNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPermissionGranted by remember {
        mutableStateOf(NovaNotificationHub.isPermissionGranted(context))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NovaBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))

            // Permission Status Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NovaSurfaceCard)
                    .border(
                        1.dp,
                        if (isPermissionGranted) NovaGreen.copy(alpha = 0.5f) else NovaAmber.copy(alpha = 0.6f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isPermissionGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isPermissionGranted) NovaGreen else NovaAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPermissionGranted) "LISTENER ACTIVE" else "ACCESS PERMISSION NEEDED",
                                color = if (isPermissionGranted) NovaGreen else NovaAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Button(
                            onClick = {
                                NovaNotificationHub.openSettings(context)
                                isPermissionGranted = NovaNotificationHub.isPermissionGranted(context)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NovaSurfaceElevated),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("open_notification_settings_button")
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

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Android requires explicit user permission in Special App Access > Notification Access for NOVA to intercept and speak incoming notifications.",
                        color = NovaTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Feature Toggles Card
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
                        text = "ANNOUNCEMENT PREFERENCES",
                        color = NovaCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    // Toggle 1: Announce new notifications
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Spoken Announcements",
                                color = NovaTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "e.g., 'Bro, tumhe ek naya message aaya hai.'",
                                color = NovaTextMuted,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = settings.notificationAnnounceEnabled,
                            onCheckedChange = {
                                onUpdateSettings(settings.copy(notificationAnnounceEnabled = it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NovaCyan,
                                checkedTrackColor = NovaSurfaceElevated
                            ),
                            modifier = Modifier.testTag("toggle_notification_announcements")
                        )
                    }

                    // Toggle 2: Read Message Content
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Read Message Content",
                                color = NovaTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Reads sender name and message body out loud",
                                color = NovaTextMuted,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = settings.readMessageContentEnabled,
                            onCheckedChange = {
                                onUpdateSettings(settings.copy(readMessageContentEnabled = it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NovaCyan,
                                checkedTrackColor = NovaSurfaceElevated
                            ),
                            modifier = Modifier.testTag("toggle_read_message_content")
                        )
                    }

                    // Test Announcement Simulation Button
                    Button(
                        onClick = {
                            val sample = NotificationItem(
                                id = UUID.randomUUID().toString(),
                                packageName = "com.whatsapp",
                                appName = "WhatsApp",
                                title = "Rahul Sharma",
                                content = "Bhai kab chalna hai match dekhne?",
                                timestamp = System.currentTimeMillis()
                            )
                            onTestNotification(sample)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NovaCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("simulate_test_notification_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = NovaBackground,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TEST NOTIFICATION ANNOUNCEMENT",
                            color = NovaBackground,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Intercepted Notifications Stream Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CAPTURED NOTIFICATIONS (${notifications.size})",
                    color = NovaCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                if (notifications.isNotEmpty()) {
                    IconButton(
                        onClick = onClearNotifications,
                        modifier = Modifier.testTag("clear_notifications_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear",
                            tint = NovaTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        if (notifications.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = NovaTextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No notifications intercepted yet.",
                            color = NovaTextMuted,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Tap 'TEST NOTIFICATION ANNOUNCEMENT' above to test.",
                            color = NovaCyan,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        } else {
            items(notifications, key = { it.id }) { item ->
                val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(item.timestamp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(NovaSurfaceElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = NovaCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.appName,
                                    color = NovaCyanBright,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = timeString,
                                color = NovaTextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (item.title.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.title,
                                color = NovaTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (item.content.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.content,
                                color = NovaTextSecondary,
                                fontSize = 12.sp
                            )
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
