/*
 * YumaPlayer (2026) | Modified work by MuwMix
 * GPL-3.0 License | Contributors: see git history
 */

package moe.rukamori.archivetune.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.composed
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.yumaGlassCard(
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = LocalYumaColors.current.glassBackground,
    borderColor: Color = LocalYumaColors.current.glassBorder
): Modifier {
    val mod = this
        .clip(shape)
        .background(backgroundColor, shape)
    return if (borderColor.isSpecified && borderColor != Color.Transparent) {
        mod.border(1.dp, borderColor, shape)
    } else {
        mod
    }
}
fun Modifier.yumaClickable(
    enabled: Boolean = true,
    pressedScale: Float = 0.96f,
    onClick: () -> Unit
): Modifier = composed {
    if (!enabled) return@composed this

    val interactionSource = remember { MutableInteractionSource() }
    val disableAnimations = LocalDisableAnimations.current

    if (disableAnimations) {
        return@composed this.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    }

    val isPressed by interactionSource.collectIsPressedAsState()

    val scaleState = animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "yumaClickScale"
    )

    this
        .graphicsLayer {
            scaleX = scaleState.value
            scaleY = scaleState.value
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}
