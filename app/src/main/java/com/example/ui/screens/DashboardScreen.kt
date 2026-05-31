package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.db.Recording
import com.example.viewmodel.VoiceVaultViewModel
import com.example.viewmodel.VaultScreen
import com.example.translate
import com.example.AppLanguage
import com.example.LanguageManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: VoiceVaultViewModel,
    modifier: Modifier = Modifier
) {
    val recordings by viewModel.recordingsList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val storageUsed by viewModel.storageUsedGb.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isHdQuality by viewModel.isHdQuality.collectAsState()

    var activeTab by remember { mutableStateOf("Home") } // "Home", "Favs", "Recent", "Shared", "Settings"
    var showRenameDialogForRecording by remember { mutableStateOf<Recording?>(null) }
    var showRenameText by remember { mutableStateOf("") }

    val filteredList = when (activeTab) {
        "Favs" -> recordings.filter { it.isFavorite }
        else -> recordings
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (activeTab == "Settings") {
                // Settings Window Panel
                SettingsPanelContent(
                    viewModel = viewModel,
                    isDarkMode = isDarkMode,
                    isHdQuality = isHdQuality,
                    onBack = { activeTab = "Home" }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        // User Profile Appbar section
                        HomeTopAppBarCustom(
                            viewModel = viewModel,
                            isDarkMode = isDarkMode,
                            onThemeToggle = { viewModel.toggleTheme() },
                            onSettingsToggle = { activeTab = "Settings" }
                        )
                    }

                    // Search layout bar
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val searchIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f)
                                Canvas(modifier = Modifier.size(20.dp)) {
                                    drawCircle(
                                        color = searchIconColor,
                                        radius = size.width * 0.3f,
                                        center = Offset(size.width * 0.4f, size.height * 0.4f),
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                    drawLine(
                                        color = searchIconColor,
                                        start = Offset(size.width * 0.6f, size.height * 0.6f),
                                        end = Offset(size.width * 0.85f, size.height * 0.85f),
                                        strokeWidth = 2.dp.toPx()
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                val keyboard = LocalSoftwareKeyboardController.current
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.setSearchQuery(it) },
                                    placeholder = {
                                        Text(
                                            translate("search_placeholder"),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        )
                                    },
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("search_field")
                                )
                            }
                        }
                    }

                    // Bento Grid Storage System
                    item {
                        BentoStatsGridCustom(
                            storageUsed = storageUsed,
                            totalCapacity = viewModel.totalStorageLimitGb,
                            totalRecordingsCount = recordings.size,
                            totalHours = recordings.sumOf { it.durationSeconds } / 3600.0
                        )
                    }

                    // Action grid buttons
                    item {
                        QuickActionsModuleCustom(
                            onNewClick = { viewModel.triggerStartRecording() },
                            onFavsClick = { activeTab = "Favs" },
                            onRecentClick = { activeTab = "Home" },
                            currentTab = activeTab
                        )
                    }

                    // Section titles
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (activeTab == "Favs") "Favorite Vaults" else "Recent Vaults",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            AnimatedVisibility(visible = searchQuery.isNotBlank() || activeTab == "Favs") {
                                Text(
                                    text = "Reset List",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.clickable {
                                        viewModel.setSearchQuery("")
                                        activeTab = "Home"
                                    }
                                )
                            }
                        }
                    }

                    // Recording Items Loop or Placeholder
                    if (filteredList.isEmpty()) {
                        item {
                            EmptyRecordsPlaceholderCustom(searchQuery, activeTab == "Favs")
                        }
                    } else {
                        items(filteredList) { recording ->
                            RecordingCardCustom(
                                recording = recording,
                                onSelect = { viewModel.selectRecordingForPlayback(recording.id) },
                                onFavoriteToggle = { viewModel.toggleFavorite(recording) },
                                onRenameClick = {
                                    showRenameDialogForRecording = recording
                                    showRenameText = recording.title
                                },
                                onDeleteClick = { viewModel.deleteRecording(recording) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal dialog to support rename operation
    if (showRenameDialogForRecording != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialogForRecording = null },
            title = {
                Text(
                    text = "Rename Audio Vault",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                OutlinedTextField(
                    value = showRenameText,
                    onValueChange = { showRenameText = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_input_field")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val rec = showRenameDialogForRecording
                        if (rec != null) {
                            viewModel.renameRecording(rec, showRenameText)
                        }
                        showRenameDialogForRecording = null
                    },
                    modifier = Modifier.testTag("confirm_rename_button")
                ) {
                    Text("Save", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialogForRecording = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun HomeTopAppBarCustom(
    viewModel: VoiceVaultViewModel,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    onSettingsToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        ),
                        shape = CircleShape
                    )
                    .padding(2.dp)
            ) {
                val bgCircleColor = MaterialTheme.colorScheme.primary
                val fgIconColor = MaterialTheme.colorScheme.onPrimary
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = bgCircleColor)
                    drawCircle(
                        color = fgIconColor,
                        radius = size.width * 0.22f,
                        center = Offset(size.width * 0.5f, size.height * 0.38f)
                    )
                    drawArc(
                        color = fgIconColor,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(size.width * 0.2f, size.height * 0.62f),
                        size = Size(size.width * 0.6f, size.height * 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = translate("app_title"),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onThemeToggle) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Theme",
                    tint = if (isDarkMode) Color(0xFFFFB72B) else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(onClick = onSettingsToggle) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BentoStatsGridCustom(
    storageUsed: Double,
    totalCapacity: Double,
    totalRecordingsCount: Int,
    totalHours: Double
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = translate("settings_storage").uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        letterSpacing = 1.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "%.1f".format(storageUsed),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "GB",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "/ %.0f GB".format(totalCapacity),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                            shape = CircleShape
                        )
                ) {
                    val progressFraction = (storageUsed / totalCapacity).toFloat()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progressFraction)
                            .fillMaxHeight()
                            .background(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = CircleShape
                            )
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    val primColor = MaterialTheme.colorScheme.primary
                    Canvas(modifier = Modifier.size(24.dp)) {
                        drawCircle(color = primColor.copy(alpha = 0.15f))
                        drawRoundRect(
                            color = primColor,
                            topLeft = Offset(size.width * 0.38f, size.height * 0.15f),
                            size = Size(size.width * 0.24f, size.height * 0.45f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        drawArc(
                            color = primColor,
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(size.width * 0.24f, size.height * 0.32f),
                            size = Size(size.width * 0.52f, size.height * 0.36f),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = translate("stat_total_recordings"),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "$totalRecordingsCount",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    val tertColor = MaterialTheme.colorScheme.tertiary
                    Canvas(modifier = Modifier.size(24.dp)) {
                        drawCircle(color = tertColor.copy(alpha = 0.15f))
                        drawCircle(
                            color = tertColor,
                            radius = size.width * 0.35f,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                        drawLine(
                            color = tertColor,
                            start = center,
                            end = Offset(center.x, center.y - size.width * 0.22f),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = translate("stat_total_time"),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "%.1fh".format(totalHours),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsModuleCustom(
    onNewClick: () -> Unit,
    onFavsClick: () -> Unit,
    onRecentClick: () -> Unit,
    currentTab: String
) {
    Column {
        Text(
            text = translate("quick_actions"),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickActionItem(
                title = translate("action_new"),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New recording",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = onNewClick
            )

            val isFavActive = currentTab == "Favs"
            QuickActionItem(
                title = translate("action_favs"),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Favorites",
                        tint = if (isFavActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                containerColor = if (isFavActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                onClick = onFavsClick
            )

            val isRecentActive = currentTab == "Home"
            QuickActionItem(
                title = translate("action_recent"),
                icon = {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Recent",
                        tint = if (isRecentActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                containerColor = if (isRecentActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                onClick = onRecentClick
            )

            QuickActionItem(
                title = translate("action_shared"),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Shared",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                onClick = {}
            )
        }
    }
}

@Composable
fun QuickActionItem(
    title: String,
    icon: @Composable () -> Unit,
    containerColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = containerColor,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
            )
        )
    }
}

@Composable
fun RecordingCardCustom(
    recording: Recording,
    onSelect: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("recording_vault_item_${recording.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recording.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val dateFormatted = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(recording.dateMillis))
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    )
                }

                val (bgColor, textColor) = when (recording.tag) {
                    "MEETING" -> Pair(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                    "INTERVIEW" -> Pair(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
                    "MEMO" -> Pair(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                    else -> Pair(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = bgColor,
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = textColor.copy(alpha = 0.25f),
                            shape = CircleShape
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = recording.tag,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                ) {
                    val peaks = remember(recording.waveformCSV) {
                        recording.waveformCSV.split(",")
                            .mapNotNull { it.toIntOrNull() }
                    }
                    val visiblePeaks = peaks.take(18)
                    visiblePeaks.forEach { peak ->
                        val hFraction = peak / 100f
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 1.dp)
                                .weight(1f)
                                .height(maxOf(4.dp, (32 * hFraction).dp))
                                .background(
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.65f),
                                    shape = RoundedCornerShape(1.dp)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                val mins = recording.durationSeconds / 60
                val secs = recording.durationSeconds % 60
                val durationStr = "%02d:%02d".format(mins, secs)
                Text(
                    text = durationStr,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (recording.isFavorite) Icons.Default.Star else Icons.Outlined.Star,
                        contentDescription = "Toggle Favorite",
                        tint = if (recording.isFavorite) Color(0xFFFFB72B) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                }
                IconButton(onClick = onRenameClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Rename Recording",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Recording",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyRecordsPlaceholderCustom(searchQuery: String, isFilteringFavorites: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            val placeholderCircleColor = MaterialTheme.colorScheme.onSurfaceVariant
            Canvas(modifier = Modifier.size(32.dp)) {
                drawCircle(color = placeholderCircleColor.copy(alpha = 0.15f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (searchQuery.isNotBlank()) "No records found matching query"
            else if (isFilteringFavorites) "No favorite audio records stored yet"
            else "Your VoiceVault is empty",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = translate("dashboard_mic_prompt"),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun SettingsPanelContent(
    viewModel: VoiceVaultViewModel,
    isDarkMode: Boolean,
    isHdQuality: Boolean,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "App Settings",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Dark Mode Theme",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "Toggle professional deep night visual theme layer.",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleTheme() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        )
                ) {
                    Text(if (isDarkMode) "🌙" else "☀️")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Ultra HD lossless sampling",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "Record high density studio parameters at 48khz lossless encoding.",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = { viewModel.toggleHdQuality() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (isHdQuality) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                ) {
                    Text(if (isHdQuality) "ON" else "OFF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Dashboard", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}
