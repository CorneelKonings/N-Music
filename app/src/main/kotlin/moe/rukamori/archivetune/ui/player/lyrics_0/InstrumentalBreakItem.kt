package moe.rukamori.archivetune.ui.player.lyrics_0

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun InstrumentalBreakItem(
    durationMs: Long,
    currentPositionMs: Long,
    startTimeMs: Long,
    textColor: Color,
    inactiveAlpha: Float,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val targetFillFraction = when {
        durationMs <= 0L -> 0f
        currentPositionMs <= startTimeMs -> 0f
        currentPositionMs >= startTimeMs + durationMs -> 1f
        else -> ((currentPositionMs - startTimeMs).toDouble() / durationMs.toDouble()).toFloat().coerceIn(0f, 1f)
    }

    val fillFraction by animateFloatAsState(
        targetValue = targetFillFraction,
        animationSpec = spring(
            stiffness = Spring.StiffnessHigh,
            dampingRatio = Spring.DampingRatioNoBouncy,
        ),
        label = "instrumentalFill"
    )

    Canvas(modifier = modifier.size(width = 64.dp, height = 24.dp)) {
        val dotRadius = 4.dp.toPx()
        val spacing = 16.dp.toPx()
        val startX = center.x - spacing
        
        for (i in 0..2) {
            val dotPhase = phase - (i * (Math.PI.toFloat() / 2f))
            val bounceOffset = (sin(dotPhase) * 4.dp.toPx()).coerceAtMost(0f)
            
            val dotCenter = Offset(startX + (i * spacing), center.y + bounceOffset)
            
            drawCircle(
                color = textColor.copy(alpha = inactiveAlpha),
                radius = dotRadius,
                center = dotCenter
            )
            
            val dotStartFraction = i / 3f
            val dotEndFraction = (i + 1) / 3f
            if (fillFraction > dotStartFraction) {
                val dotFillRatio = ((fillFraction - dotStartFraction) / (dotEndFraction - dotStartFraction)).coerceIn(0f, 1f)
                val clipRight = dotCenter.x - dotRadius + (dotRadius * 2 * dotFillRatio)
                
                clipRect(
                    left = dotCenter.x - dotRadius,
                    top = dotCenter.y - dotRadius,
                    right = clipRight,
                    bottom = dotCenter.y + dotRadius
                ) {
                    drawCircle(
                        color = textColor,
                        radius = dotRadius,
                        center = dotCenter
                    )
                }
            }
        }
    }
}
