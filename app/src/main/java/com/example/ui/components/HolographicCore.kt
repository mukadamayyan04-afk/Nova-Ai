package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.AssistantState
import com.example.ui.theme.NovaAmber
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaCyanBright
import com.example.ui.theme.NovaElectricBlue
import com.example.ui.theme.NovaGreen
import com.example.ui.theme.NovaPurple
import com.example.ui.theme.NovaRed
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HolographicCore(
    state: AssistantState,
    amplitude: Float,
    reducedAnimation: Boolean,
    onCoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "core_anim")

    // Rotation angles
    val rotationFast by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (reducedAnimation) 0 else 8000, easing = LinearEasing)
        ),
        label = "rot_fast"
    )

    val rotationSlow by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (reducedAnimation) 0 else 16000, easing = LinearEasing)
        ),
        label = "rot_slow"
    )

    // Pulse
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (reducedAnimation) 0 else 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val effectivePulse = if (reducedAnimation) 1.0f else when (state) {
        AssistantState.LISTENING -> (1.0f + amplitude * 0.25f)
        AssistantState.PROCESSING -> pulseScale
        AssistantState.SPEAKING -> pulseScale * 1.05f
        else -> pulseScale
    }

    val primaryCoreColor = when (state) {
        AssistantState.IDLE -> NovaCyan
        AssistantState.LISTENING -> NovaGreen
        AssistantState.PROCESSING -> NovaAmber
        AssistantState.SPEAKING -> NovaElectricBlue
        AssistantState.ERROR -> NovaRed
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(240.dp)
            .testTag("holographic_core_container")
    ) {
        // Futuristic Arc Reactor Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2f) * 0.88f * effectivePulse

            // Outer subtle glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryCoreColor.copy(alpha = 0.22f),
                        primaryCoreColor.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.35f
                ),
                radius = baseRadius * 1.35f,
                center = center
            )

            // Outer ring with notches
            drawCircle(
                color = primaryCoreColor.copy(alpha = 0.45f),
                radius = baseRadius,
                center = center,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 15f, 10f, 15f), 0f)
                )
            )

            // Rotating outer cyber ring
            rotate(degrees = if (reducedAnimation) 45f else rotationFast, pivot = center) {
                drawCircle(
                    color = primaryCoreColor.copy(alpha = 0.7f),
                    radius = baseRadius * 0.88f,
                    center = center,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(45f, 30f), 0f)
                    )
                )

                // 4 satellite node points
                for (i in 0 until 4) {
                    val angle = (i * 90) * (PI / 180.0)
                    val r = baseRadius * 0.88f
                    val px = center.x + (r * cos(angle)).toFloat()
                    val py = center.y + (r * sin(angle)).toFloat()
                    drawCircle(
                        color = NovaCyanBright,
                        radius = 4.dp.toPx(),
                        center = Offset(px, py)
                    )
                }
            }

            // Counter-rotating middle ring
            rotate(degrees = if (reducedAnimation) 120f else rotationSlow, pivot = center) {
                drawCircle(
                    color = NovaPurple.copy(alpha = 0.55f),
                    radius = baseRadius * 0.72f,
                    center = center,
                    style = Stroke(
                        width = 2.5f.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f, 60f, 20f), 0f)
                    )
                )
            }

            // Inner energy containment circle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryCoreColor.copy(alpha = 0.45f),
                        primaryCoreColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 0.58f
                ),
                radius = baseRadius * 0.58f,
                center = center
            )

            drawCircle(
                color = primaryCoreColor,
                radius = baseRadius * 0.52f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Center Action Core (Microphone & Interactive trigger)
        val interactionSource = remember { MutableInteractionSource() }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            primaryCoreColor.copy(alpha = 0.4f),
                            Color(0xFF091226)
                        )
                    )
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true, color = NovaCyanBright),
                    onClick = onCoreClick
                )
                .testTag("core_mic_button")
        ) {
            when (state) {
                AssistantState.IDLE -> {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Activate Voice Assistant",
                        tint = NovaCyanBright,
                        modifier = Modifier.size(42.dp)
                    )
                }
                AssistantState.LISTENING -> {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop Listening",
                        tint = NovaGreen,
                        modifier = Modifier.size(42.dp)
                    )
                }
                AssistantState.PROCESSING -> {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = "Processing Query",
                        tint = NovaAmber,
                        modifier = Modifier.size(38.dp)
                    )
                }
                AssistantState.SPEAKING -> {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "NOVA Speaking",
                        tint = NovaCyanBright,
                        modifier = Modifier.size(42.dp)
                    )
                }
                AssistantState.ERROR -> {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error state",
                        tint = NovaRed,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
        }
    }
}
