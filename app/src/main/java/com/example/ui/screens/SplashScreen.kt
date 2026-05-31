package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkPrimary
import com.example.ui.theme.DarkSecondary

@Composable
fun SplashScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "SplashTransition")

    // Breathing glow animation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "GlowAnimation"
    )

    // Simple live visual waveform simulation bounce
    val barScale1 by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "BarScale1"
    )
    val barScale2 by infiniteTransition.animateFloat(
        initialValue = 14f,
        targetValue = 32f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "BarScale2"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(500f, 600f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Logo Outer Glow Container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                // Outer Blur Aura
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .alpha(glowAlpha)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                )

                // Animated Mic Ring Base
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Orbiting Subtle Ring
                    Canvas(modifier = Modifier.size(94.dp)) {
                        drawCircle(
                            color = DarkPrimary.copy(alpha = 0.25f),
                            style = Stroke(
                                width = 1.5.dp.toPx(),
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                    floatArrayOf(12f, 12f), 0f
                                )
                            )
                        )
                    }

                    // Inner Mic Draw
                    Canvas(modifier = Modifier.size(44.dp)) {
                        val w = size.width
                        val h = size.height
                        // capsule
                        drawRoundRect(
                            color = DarkPrimary,
                            topLeft = Offset(w * 0.35f, h * 0.15f),
                            size = Size(w * 0.3f, h * 0.45f),
                            cornerRadius = CornerRadius(w * 0.15f, w * 0.15f)
                        )
                        // stand arm
                        drawArc(
                            color = DarkPrimary,
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(w * 0.25f, h * 0.3f),
                            size = Size(w * 0.5f, h * 0.42f),
                            style = Stroke(width = w * 0.08f)
                        )
                        // post
                        drawRect(
                            color = DarkPrimary,
                            topLeft = Offset(w * 0.46f, h * 0.72f),
                            size = Size(w * 0.08f, h * 0.15f)
                        )
                        // base
                        drawRect(
                            color = DarkPrimary,
                            topLeft = Offset(w * 0.3f, h * 0.85f),
                            size = Size(w * 0.4f, h * 0.05f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Branding Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Voice",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "Vault",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Capture Every Moment",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Animated Simulated Waveform
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.height(48.dp)
            ) {
                val heights = listOf(
                    barScale1, barScale2, barScale1 * 0.8f, barScale2 * 1.2f, barScale1 * 1.5f,
                    barScale2 * 1.1f, barScale1 * 1.3f, barScale2 * 0.9f, barScale1 * 0.6f
                )
                heights.forEach { heightVal ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.5.dp)
                            .width(4.dp)
                            .height(heightVal.dp)
                            .background(
                                color = DarkSecondary,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }

        // Encryption lock label
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(0.5f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Canvas(modifier = Modifier.size(14.dp)) {
                    val w = size.width
                    val h = size.height
                    drawRoundRect(
                        color = DarkPrimary,
                        topLeft = Offset(w * 0.15f, h * 0.45f),
                        size = Size(w * 0.7f, h * 0.45f),
                        cornerRadius = CornerRadius(w * 0.1f, w * 0.1f)
                    )
                    drawArc(
                        color = DarkPrimary,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.3f, h * 0.15f),
                        size = Size(w * 0.4f, h * 0.6f),
                        style = Stroke(width = w * 0.1f)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ENCRYPTED & SECURE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                )
            }
        }
    }
}
