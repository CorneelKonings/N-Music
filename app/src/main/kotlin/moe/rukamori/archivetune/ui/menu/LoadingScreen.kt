/*
 * YumaPlayer (2026) | Modified work by MuwMix
 * ArchiveTune (2026) | Original work by © Rukamori
 * GPL-3.0 License | Contributors: see git history
 */

package moe.rukamori.archivetune.ui.menu

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun CustomProgressBar(
    progress: Float?,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "progressBarTransition")
    val animationOffset = if (progress == null) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "offset"
        ).value
    } else 0f

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val radius = height / 2f

        // Draw track
        drawRoundRect(
            color = trackColor,
            size = Size(width, height),
            cornerRadius = CornerRadius(radius, radius)
        )

        if (progress != null) {
            // Draw determinate progress with gradient
            val progressWidth = width * progress.coerceIn(0f, 1f)
            if (progressWidth > 0) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.7f), primaryColor)
                    ),
                    size = Size(progressWidth, height),
                    cornerRadius = CornerRadius(radius, radius)
                )
            }
        } else {
            // Draw indeterminate sliding glow capsule
            val capsuleWidth = width * 0.3f
            val startX = (width + capsuleWidth) * animationOffset - capsuleWidth
            val clampedStartX = startX.coerceIn(-capsuleWidth, width)
            val drawWidth = if (clampedStartX < 0) {
                capsuleWidth + clampedStartX
            } else if (clampedStartX + capsuleWidth > width) {
                width - clampedStartX
            } else {
                capsuleWidth
            }
            val actualStartX = clampedStartX.coerceAtLeast(0f)

            if (drawWidth > 0) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.3f), primaryColor, primaryColor.copy(alpha = 0.3f)),
                        startX = actualStartX,
                        endX = actualStartX + drawWidth
                    ),
                    topLeft = Offset(actualStartX, 0f),
                    size = Size(drawWidth, height),
                    cornerRadius = CornerRadius(radius, radius)
                )
            }
        }
    }
}

@Composable
fun LoadingScreen(
    isVisible: Boolean,
    value: Int,
    title: String? = null,
    stepText: String? = null,
    indeterminate: Boolean = false,
    cancelLabel: String? = null,
    onCancel: (() -> Unit)? = null,
) {
    if (isVisible) {
        val percent = value.coerceIn(0, 100)
        val cancelAction = onCancel
        val resolvedCancelLabel = cancelLabel?.takeIf(String::isNotBlank)
        Dialog(onDismissRequest = {}) {
            Card(
                modifier =
                    Modifier
                        .widthIn(min = 280.dp, max = 380.dp)
                        .padding(16.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (!title.isNullOrBlank()) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!stepText.isNullOrBlank()) {
                            Text(
                                text = stepText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        if (indeterminate) {
                            CustomProgressBar(
                                progress = null,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(6.dp),
                            )
                        } else {
                            CustomProgressBar(
                                progress = percent / 100f,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(6.dp),
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "$percent%",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        if (cancelAction != null && resolvedCancelLabel != null) {
                            TextButton(
                                onClick = cancelAction,
                                shapes = ButtonDefaults.shapes(),
                            ) {
                                Text(text = resolvedCancelLabel)
                            }
                        }
                    }
                }
            }
        }
    }
}
