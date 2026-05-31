package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.db.Recording
import com.example.viewmodel.VoiceVaultViewModel
import com.example.translate
import com.example.AppLanguage
import com.example.LanguageManager
import com.example.viewmodel.RepeatMode

@Composable
fun PlaybackScreen(
    viewModel: VoiceVaultViewModel,
    modifier: Modifier = Modifier
) {
    val recording by viewModel.activePlaybackRecording.collectAsState()
    val isPlaying by viewModel.isPlaybackPlaying.collectAsState()
    val playPosSec by viewModel.playbackSeconds.collectAsState()
    val speed by viewModel.playbackSpeed.collectAsState()

    // AI states
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val aiResult by viewModel.aiResultText.collectAsState()
    val aiLoadingText by viewModel.aiTranscriptLoadingSubtext.collectAsState()

    val repeatMode by viewModel.repeatMode.collectAsState()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()

    var showRenameDialog by remember { mutableStateOf(false) }
    var renameTxt by remember { mutableStateOf("") }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    if (recording == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No active session selected")
        }
        return
    }

    val currentRec = recording!!

    val progress = remember(playPosSec, currentRec.durationSeconds) {
        if (currentRec.durationSeconds > 0) {
            (playPosSec.toFloat() / currentRec.durationSeconds.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    val waveformBars = remember(currentRec.waveformCSV) {
        try {
            currentRec.waveformCSV.split(",").mapNotNull { it.trim().substringBeforeLast('.').trim().toIntOrNull() }
        } catch (e: Exception) {
            List(40) { (15..95).random() }
        }
    }

    Scaffold(
        topBar = {
            PlaybackAppBar(
                title = currentRec.title,
                onBackClick = {
                    viewModel.goBack()
                }
            )
        },
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.0f)) {
                    Text(
                        text = currentRec.title,
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Calendar stamp indication",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                            val calDate = remember(currentRec.dateMillis) {
                                val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                                sdf.format(java.util.Date(currentRec.dateMillis))
                            }
                            Text(
                                text = calDate,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Timer stamp",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                            val durationString = remember(currentRec.durationSeconds) {
                                val m = currentRec.durationSeconds / 60
                                val s = currentRec.durationSeconds % 60
                                String.format("%02d:%02d", m, s)
                            }
                            Text(
                                text = durationString,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = {
                        renameTxt = currentRec.title
                        showRenameDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Rename recording details",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), CircleShape)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        currentRec.tag.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                val secondLabel = if (currentRec.isFavorite) "FAVORITE" else "SECURE VAULT"
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                        .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), CircleShape)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        secondLabel,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // High-fidelity Waveform Progress
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        val totalBars = waveformBars.size
                        waveformBars.forEachIndexed { idx, value ->
                            val scaleHeight = (value.toFloat() / 100f) * 120.dp.value
                            val isPlayed = (idx.toFloat() / totalBars.toFloat()) <= progress

                            val barColor = if (isPlayed) MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(scaleHeight.dp)
                                    .clip(CircleShape)
                                    .background(barColor)
                                    .clickable {
                                        val newProgress = idx.toFloat() / totalBars.toFloat()
                                        val targetSeconds = (newProgress * currentRec.durationSeconds).toInt()
                                        viewModel.skipPlayback(targetSeconds - playPosSec)
                                    }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentPosStr = remember(playPosSec) {
                        String.format("%d:%02d", playPosSec / 60, playPosSec % 60)
                    }
                    val totalDurationStr = remember(currentRec.durationSeconds) {
                        String.format("%d:%02d", currentRec.durationSeconds / 60, currentRec.durationSeconds % 60)
                    }

                    Text(
                        currentPosStr,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        totalDurationStr,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.cyclePlaybackSpeed() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${speed}x SPEED",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { Toast.makeText(context, "Bookmark added at current timestamp!", Toast.LENGTH_SHORT).show() }) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdd,
                            contentDescription = "Add voice bookmark notes marker",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = { Toast.makeText(context, "More configuration options", Toast.LENGTH_SHORT).show() }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options menu playback popup",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.playPrevious() },
                        modifier = Modifier.size(40.dp).testTag("play_prev_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous track session",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.skipPlayback(-10) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Rewind ten seconds",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = CircleShape,
                                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            .clickable { viewModel.togglePlayback() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play toggle",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.skipPlayback(10) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Skip ten seconds forward",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.playNext() },
                        modifier = Modifier.size(40.dp).testTag("play_next_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next track session",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ClusterItemButton(
                        icon = Icons.Default.Share,
                        label = translate("play_share"),
                        onClick = {
                            Toast.makeText(context, LanguageManager.translate("copylink_toast"), Toast.LENGTH_SHORT).show()
                        }
                    )
                    ClusterItemButton(
                        icon = if (currentRec.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        label = translate("play_favorite"),
                        onClick = { viewModel.toggleFavorite(currentRec) }
                    )
                    ClusterItemButton(
                        icon = Icons.Default.ContentCopy,
                        label = translate("play_copy"),
                        onClick = {
                            clipboardManager.setText(AnnotatedString(currentRec.title))
                            Toast.makeText(context, "Title copied to secure clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    ClusterItemButton(
                        icon = Icons.Default.Delete,
                        label = translate("play_delete"),
                        onClick = { viewModel.deleteRecording(currentRec) }
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Playback Options",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = translate("quick_actions"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // --- REPEAT OPTION ---
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = translate("play_repeat_mode"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val modes = listOf(
                                Triple(RepeatMode.OFF, Icons.Default.TrendingFlat, translate("play_repeat_off")),
                                Triple(RepeatMode.ONE, Icons.Default.RepeatOne, translate("play_repeat_one")),
                                Triple(RepeatMode.STARRED, Icons.Default.Star, translate("play_repeat_starred")),
                                Triple(RepeatMode.ALL, Icons.Default.Repeat, translate("play_repeat_all"))
                            )
                            modes.forEach { (mode, icon, displayName) ->
                                val isSelected = repeatMode == mode
                                Button(
                                    onClick = { viewModel.setRepeatMode(mode) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("repeat_${mode.name.lowercase()}_btn"),
                                    contentPadding = PaddingValues(horizontal = 2.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                                    border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)) else null
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = displayName,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = displayName,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // --- LANGUAGE OPTION ---
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = translate("settings_language"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppLanguage.values().forEach { lang ->
                                val isSelected = currentLanguage == lang
                                Button(
                                    onClick = { LanguageManager.setLanguage(context, lang) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("lang_${lang.name.lowercase()}_btn"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                                    border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)) else null
                                ) {
                                    Text(
                                        text = lang.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = {
                Text(
                    "Rename Session Vault",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = renameTxt,
                    onValueChange = { renameTxt = it },
                    label = { Text("Vault Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renameRecording(currentRec, renameTxt)
                        showRenameDialog = false
                    }
                ) {
                    Text("Rename", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackAppBar(
    title: String,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "Secure Audio Vault",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back navigation"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun ClusterItemButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun BoxBorderSimulator(isAiLoading: Boolean): androidx.compose.foundation.BorderStroke {
    val borderColor = if (isAiLoading) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val width = if (isAiLoading) 2.dp else 1.dp
    return androidx.compose.foundation.BorderStroke(width, borderColor)
}

