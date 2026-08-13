/*
 * YumaPlayer (2026) | Original work by MuwMix
 * GPL-3.0 License | Contributors: see git history
 */

package moe.rukamori.archivetune.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import moe.rukamori.archivetune.ui.settings.SettingsAnimations
import moe.rukamori.archivetune.ui.settings.SettingsDimensions

@Composable
fun Modifier.yumaGlassCard(
    shape: Shape = RoundedCornerShape(SettingsDimensions.GlassCornerRadius),
    backgroundColor: Color = LocalYumaColors.current.glassBackground,
    borderColor: Color = LocalYumaColors.current.glassBorder
): Modifier {
    val mod = this
        .clip(shape)
        .background(backgroundColor, shape)
    return if (borderColor.isSpecified && borderColor != Color.Transparent) {
        mod.border(SettingsDimensions.GlassBorderThickness, borderColor, shape)
    } else {
        mod
    }
}
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

    return this
        .graphicsLayer {
            val isPressed = isPressedState.value
            scaleX = if (isPressed) pressedScale else 1f
            scaleY = if (isPressed) pressedScale else 1f
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

    return this
        .graphicsLayer {
            val isPressed = isPressedState.value
            scaleX = if (isPressed) pressedScale else 1f
            scaleY = if (isPressed) pressedScale else 1f
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
