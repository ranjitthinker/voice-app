package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.VoiceVaultViewModel
import com.example.translate
import com.example.AppLanguage
import com.example.LanguageManager

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RecordScreen(
    viewModel: VoiceVaultViewModel,
    modifier: Modifier = Modifier
) {
    val duration by viewModel.recordingSeconds.collectAsState()
    val recordStatus by viewModel.recordStatus.collectAsState()
    val levels by viewModel.inputLevels.collectAsState()
    val isHdActive by viewModel.isHdQuality.collectAsState()

    val isPaused = recordStatus == VoiceVaultViewModel.RecordStatus.PAUSED

    var showSaveNamingDialog by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("MEETING") }

    val formattedTime = remember(duration) {
        val h = duration / 3600
        val m = (duration % 3600) / 60
        val s = duration % 60
        String.format("%02d:%02d:%02d", h, m, s)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
    val pulseRadius1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Restart
        ), label = "Rad1"
    )
    val pulseRadius2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Restart
        ), label = "Rad2"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Atmosphere glows
        Box(
            modifier = Modifier
                .size(400.dp)
                .alpha(0.08f)
                .blur(80.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    translate("app_title"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Ultra Quality Pill Badge
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                            CircleShape
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                        )
                        Text(
                            text = if (isHdActive) "ULTRA HD (256kb)" else "STANDARD (64kb)",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Central Pulsing Microphone Core Card
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1.0f)
            ) {
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.02).sp
                    ),
                    modifier = Modifier.testTag("recording_duration_timer")
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isPaused) translate("rec_status_paused") else translate("rec_status_active"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isPaused) 0.5f else 1.0f)
                )

                Spacer(modifier = Modifier.height(48.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(240.dp)
                ) {
                    if (!isPaused) {
                        // Pulsing background radar aura rings
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .scale(pulseRadius1)
                                .alpha((1.3f - pulseRadius1).coerceIn(0f, 1f))
                                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .scale(pulseRadius2)
                                .alpha((1.3f - pulseRadius2).coerceIn(0f, 1f))
                                .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                        )
                    }

                    // Large glowing central stop recording pill action
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .shadow(
                                elevation = 32.dp,
                                shape = CircleShape,
                                ambientColor = MaterialTheme.colorScheme.primaryContainer,
                                spotColor = MaterialTheme.colorScheme.primaryContainer
                            )
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primaryContainer
                                    )
                                ),
                                CircleShape
                            )
                            .clickable {
                                customName = "Recording Session #${viewModel.recordingsList.value.size + 1}"
                                showSaveNamingDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = isPaused,
                            transitionSpec = {
                                fadeIn() with fadeOut()
                            }, label = "MicMorphShape"
                        ) { paused ->
                            if (paused) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Resume mic",
                                    tint = Color.White,
                                    modifier = Modifier.size(64.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                )
                            }
                        }
                    }

                    // Small absolute floating encrypted label
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                                CircleShape
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock secure icon info",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                "Encrypted",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Bottom Waveform + Active db levels panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .padding(horizontal = 32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val barsToShow = 35
                    for (i in 0 until barsToShow) {
                        val bounceHeight = if (!isPaused) {
                            remember(levels, i) {
                                finalHeightFactor(i, levels) * 32.dp.value
                            }
                        } else {
                            4.dp.value
                        }

                        val barColor = if (i in 13..21) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.primary

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(bounceHeight.dp)
                                .clip(CircleShape)
                                .background(
                                    barColor.copy(alpha = if (isPaused) 0.15f else 0.8f)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "INPUT LEVEL METER",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val dotCount = 5
                            for (x in 1..dotCount) {
                                val dotAlpha = if (isPaused) 0.12f else if (x <= 3) 1f else 0.35f
                                Box(
                                    modifier = Modifier
                                        .size(10.dp, 4.dp)
                                        .clip(CircleShape)
                                        .background(
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = dotAlpha)
                                        )
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "VAULT SPACE LIMIT",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        val storageVal by viewModel.storageUsedGb.collectAsState()
                        Text(
                            "${String.format("%.1f", storageVal)} GB / 50 GB",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Bottom Actions Cluster Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { viewModel.cancelRecording() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Recording Memo",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Cancel",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Pause / Resume central control
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { viewModel.pauseResumeRecording() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Pause recorder toggle",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isPaused) translate("rec_btn_resume") else translate("rec_btn_pause"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Save recording triggers naming layout
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        customName = "Recording Session #${viewModel.recordingsList.value.size + 1}"
                        showSaveNamingDialog = true
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "Save Action trigger",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = translate("rec_btn_save"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showSaveNamingDialog) {
        AlertDialog(
            onDismissRequest = { showSaveNamingDialog = false },
            title = {
                Text(
                    "Secure Recording Vault Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Vault Title") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Select Category Tag",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val tags = listOf("MEETING", "INTERVIEW", "MEMO", "URGENT")
                            tags.forEach { tagItem ->
                                val active = selectedTag == tagItem
                                val chipBg = if (active) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                val textCol = if (active) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(CircleShape)
                                        .background(chipBg)
                                        .clickable { selectedTag = tagItem }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tagItem,
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = textCol
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.stopAndSaveRecording(customName, selectedTag)
                        showSaveNamingDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Secure Vault", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveNamingDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun finalHeightFactor(index: Int, levelsList: List<Float>): Float {
    if (levelsList.isEmpty()) return 0.25f
    val levelsIdx = index % levelsList.size
    val ampVal = levelsList[levelsIdx]
    val sineOffset = kotlin.math.sin(index.toDouble() * 0.4).toFloat().coerceAtLeast(0f) * 0.35f
    return (ampVal + sineOffset).coerceIn(0.12f, 0.95f)
}
