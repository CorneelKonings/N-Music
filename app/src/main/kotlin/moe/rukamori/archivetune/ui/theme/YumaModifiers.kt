/*
 * YumaPlayer (2026) | Original work by MuwMix
 * GPL-3.0 License | Contributors: see git history
 */

package moe.rukamori.archivetune.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import moe.rukamori.archivetune.ui.settings.SettingsAnimations
import moe.rukamori.archivetune.ui.settings.SettingsDimensions

fun Modifier.glassBorder(
    shape: Shape,
    strokeWidth: Dp = SettingsDimensions.GlassBorderThickness,
    topAlpha: Float = 0.20f,
    bottomAlpha: Float = 0.04f,
    baseColor: Color = Color.White
): Modifier = if (baseColor.isSpecified && baseColor != Color.Transparent) {
    this.border(
        width = strokeWidth,
        brush = Brush.verticalGradient(
            0.0f to baseColor.copy(alpha = topAlpha),
            1.0f to baseColor.copy(alpha = bottomAlpha)
        ),
        shape = shape
    )
} else {
    this
}

@Composable
fun Modifier.yumaGlassCard(
    shape: Shape = RoundedCornerShape(SettingsDimensions.GlassCornerRadius),
    backgroundColor: Color = LocalYumaColors.current.glassBackground,
    borderColor: Color = LocalYumaColors.current.glassBorder,
    strokeWidth: Dp = SettingsDimensions.GlassBorderThickness,
    topAlpha: Float = 0.20f,
    bottomAlpha: Float = 0.04f
): Modifier = this
    .clip(shape)
    .background(backgroundColor, shape)
    .glassBorder(
        shape = shape,
        strokeWidth = strokeWidth,
        topAlpha = topAlpha,
        bottomAlpha = bottomAlpha,
        baseColor = borderColor
    )
@Composable
fun Modifier.yumaClickable(
    enabled: Boolean = true,
    pressedScale: Float = SettingsAnimations.PressScale,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val disableAnimations = LocalDisableAnimations.current

    if (disableAnimations || !enabled) {
        return this.clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
    }

    val isPressedState = interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressedState.value) pressedScale else 1f,
        animationSpec = SettingsAnimations.pressSpring(),
        label = "yumaClickScale",
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = true,
            onClick = onClick
        )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.yumaCombinedClickable(
    enabled: Boolean = true,
    pressedScale: Float = SettingsAnimations.PressScale,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val disableAnimations = LocalDisableAnimations.current
    val haptic = LocalHapticFeedback.current

    val hapticOnClick = remember(onClick, haptic) {
        {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        }
    }

    val hapticOnLongClick = remember(onLongClick, haptic) {
        onLongClick?.let {
            {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                it()
            }
        }
    }

    if (disableAnimations || !enabled) {
        return this.combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onLongClick = if (onLongClick != null) hapticOnLongClick else null,
            onDoubleClick = onDoubleClick,
            onClick = hapticOnClick
        )
    }

    val isPressedState = interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressedState.value) pressedScale else 1f,
        animationSpec = SettingsAnimations.pressSpring(),
        label = "yumaCombinedClickScale",
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onLongClick = if (onLongClick != null) hapticOnLongClick else null,
            onDoubleClick = onDoubleClick,
            onClick = hapticOnClick
        )
}
