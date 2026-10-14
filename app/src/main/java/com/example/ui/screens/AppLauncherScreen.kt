package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.ui.theme.NovaBackground
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaCyanBright
import com.example.ui.theme.NovaElectricBlue
import com.example.ui.theme.NovaGreen
import com.example.ui.theme.NovaSurfaceCard
import com.example.ui.theme.NovaSurfaceElevated
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LauncherAppModel(
    val name: String,
    val packageName: String,
    val icon: Drawable? = null,
    val isSystemApp: Boolean = false
)

enum class AppFilterCategory(val label: String) {
    ALL("All Apps"),
    GOOGLE("Google"),
    SYSTEM("System"),
    THIRD_PARTY("User Apps")
}

@Composable
fun AppLauncherScreen(
    onTriggerVoiceCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(AppFilterCategory.ALL) }
    var appsList by remember { mutableStateOf<List<LauncherAppModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Query installed applications on background thread
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolvedList = pm.queryIntentActivities(mainIntent, 0)
            val parsed = resolvedList.mapNotNull { ri ->
                val pkg = ri.activityInfo.packageName
                if (pkg == context.packageName) return@mapNotNull null
                val label = ri.loadLabel(pm).toString()
                val icon = try {
                    ri.loadIcon(pm)
                } catch (_: Exception) {
                    null
                }
                val isSys = (ri.activityInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                LauncherAppModel(name = label, packageName = pkg, icon = icon, isSystemApp = isSys)
            }.distinctBy { it.packageName }.sortedBy { it.name.lowercase() }

            appsList = parsed
            isLoading = false
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    // Real-time filtering by query and category as the user types
    val displayedApps = remember(searchQuery, selectedCategory, appsList) {
        val query = searchQuery.trim().lowercase()
        appsList.filter { app ->
            val matchesCategory = when (selectedCategory) {
                AppFilterCategory.ALL -> true
                AppFilterCategory.GOOGLE -> app.packageName.startsWith("com.google.")
                AppFilterCategory.SYSTEM -> app.isSystemApp
                AppFilterCategory.THIRD_PARTY -> !app.isSystemApp
            }
            val matchesSearch = query.isEmpty() ||
                    app.name.lowercase().contains(query) ||
                    app.packageName.lowercase().contains(query)

            matchesCategory && matchesSearch
        }
    }

    // Scroll back to top immediately when search query changes
    LaunchedEffect(searchQuery) {
        if (displayedApps.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NovaBackground)
            .testTag("app_launcher_screen")
    ) {
        // Futuristic Launcher HUD Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(NovaSurfaceCard)
                .border(1.dp, NovaBorder, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(NovaGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "APP LAUNCH PROTOCOL",
                            color = NovaCyanBright,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isLoading) "Scanning installed packages..." else "${appsList.size} Applications Linked",
                        color = NovaTextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NovaSurfaceElevated)
                        .border(1.dp, NovaBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "INTENTS: LIVE",
                        color = NovaCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Real-time Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = "Type to filter apps in real-time...",
                    color = NovaTextMuted,
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search apps",
                    tint = NovaCyan,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = searchQuery.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(
                        onClick = {
                            searchQuery = ""
                            keyboardController?.hide()
                        },
                        modifier = Modifier.testTag("app_launcher_clear_search")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = NovaTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = NovaTextPrimary,
                unfocusedTextColor = NovaTextPrimary,
                focusedBorderColor = NovaCyan,
                unfocusedBorderColor = NovaBorder,
                cursorColor = NovaCyan
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("app_launcher_search_field")
        )

        // Real-Time Query Filter Status Banner
        AnimatedVisibility(
            visible = searchQuery.isNotBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MATCHING: \"$searchQuery\" • ${displayedApps.size} apps found",
                    color = NovaCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "CLEAR",
                    color = NovaCyanBright,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .clickable {
                            searchQuery = ""
                            keyboardController?.hide()
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .testTag("app_launcher_clear_filter_badge")
                )
            }
        }

        // Category Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(AppFilterCategory.values()) { category ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) NovaCyan else NovaSurfaceElevated)
                        .border(
                            1.dp,
                            if (isSelected) NovaCyanBright else NovaBorder.copy(alpha = 0.3f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedCategory = category }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("filter_chip_${category.name.lowercase()}")
                ) {
                    Text(
                        text = category.label,
                        color = if (isSelected) NovaBackground else NovaTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Content Area
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = NovaCyan,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "DISCOVERING SYSTEM PACKAGES...",
                        color = NovaCyanBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
            }
        } else if (displayedApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = null,
                        tint = NovaTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No applications match your criteria",
                        color = NovaTextSecondary,
                        fontSize = 14.sp
                    )
                    if (searchQuery.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { searchQuery = "" },
                            colors = ButtonDefaults.buttonColors(containerColor = NovaSurfaceElevated),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Reset Search", color = NovaCyan, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            // LazyColumn displaying installed applications
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .testTag("app_launcher_lazy_column"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "RESULTS: ${displayedApps.size} APPS",
                        color = NovaTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                items(displayedApps, key = { it.packageName }) { app ->
                    AppLauncherCard(
                        app = app,
                        onLaunch = {
                            try {
                                val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                if (launchIntent != null) {
                                    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    context.startActivity(launchIntent)
                                } else {
                                    Toast.makeText(context, "Cannot launch ${app.name}", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Launch failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onVoiceLaunch = {
                            onTriggerVoiceCommand("Nova, open ${app.name}")
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun AppLauncherCard(
    app: LauncherAppModel,
    onLaunch: () -> Unit,
    onVoiceLaunch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NovaSurfaceCard)
            .border(1.dp, NovaBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
            .testTag("launcher_card_${app.packageName.replace(".", "_")}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Icon
                AppIconThumbnail(
                    drawable = app.icon,
                    modifier = Modifier.size(46.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // App Label and Package details
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = app.name,
                            color = NovaTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (app.isSystemApp) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NovaSurfaceElevated)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "SYS",
                                    color = NovaCyan,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = app.packageName,
                        color = NovaTextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Direct Launch Intent Button
                Button(
                    onClick = onLaunch,
                    colors = ButtonDefaults.buttonColors(containerColor = NovaCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("btn_launch_${app.packageName.replace(".", "_")}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Launch,
                        contentDescription = "Launch application",
                        tint = NovaBackground,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LAUNCH",
                        color = NovaBackground,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Voice Command Chip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(NovaSurfaceElevated)
                    .clickable { onVoiceLaunch() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = null,
                    tint = NovaCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Command: \"Nova, open ${app.name}\"",
                    color = NovaElectricBlue,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AppIconThumbnail(
    drawable: Drawable?,
    modifier: Modifier = Modifier
) {
    if (drawable == null) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(10.dp))
                .background(NovaSurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Android,
                contentDescription = null,
                tint = NovaCyan,
                modifier = Modifier.size(24.dp)
            )
        }
        return
    }

    val bitmap = remember(drawable) {
        try {
            drawable.toBitmap(width = 96, height = 96)
        } catch (_: Exception) {
            null
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier
                .clip(RoundedCornerShape(10.dp))
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(10.dp))
                .background(NovaSurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Android,
                contentDescription = null,
                tint = NovaCyan,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
