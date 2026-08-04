package moe.rukamori.archivetune.ui.player.player_0.buttons

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.rukamori.archivetune.R
import moe.rukamori.archivetune.ui.state.PlayerUiState

private val ButtonClickAreaSize = 48.dp
private val StandardIconSize = 26.dp
private val LyricsIconSize = 32.dp

@Composable
fun PlayerBottomBar(
    state: PlayerUiState,
    onAction: (PlayerAction) -> Unit,
    modifier: Modifier = Modifier,
    colorScheme: ColorScheme = MaterialTheme.colorScheme
) {
    val isLightTheme = colorScheme.surface.luminance() > 0.5f
    val isImmersive = state.isImmersiveEnabled && !state.isLyricsVisible
    val isBlur = state.isBlurBackgroundEnabled
    val isDarkOrIsolated = !isLightTheme || isImmersive || isBlur

    val inactiveColor = if (isDarkOrIsolated) {
        Color.White.copy(alpha = 0.45f)
    } else {
        colorScheme.onSurface.copy(alpha = 0.45f)
    }

    val inactiveButtonColor = if (isDarkOrIsolated) {
        Color.White.copy(alpha = 0.75f)
    } else {
        colorScheme.onSurface.copy(alpha = 0.75f)
    }

    val rawActiveColor = remember(state.vibrantColor, colorScheme) {
        if (state.vibrantColor == 0) {
            colorScheme.primary
        } else {
            Color(state.vibrantColor).copy(alpha = 1f)
        }
    }

    val activeColor = remember(rawActiveColor, isLightTheme, isImmersive, isBlur, colorScheme) {
        if (isImmersive || isBlur) {
            Color.White
        } else if (isLightTheme) {
            val lum = rawActiveColor.luminance()
            if (lum > 0.65f) {
                colorScheme.primary
            } else {
                rawActiveColor
            }
        } else {
            val lum = rawActiveColor.luminance()
            if (lum < 0.35f) {
                lerp(rawActiveColor, Color.White, 0.5f)
            } else {
                rawActiveColor
            }
        }
    }

    val isShuffleActive = state.shuffleState != "off"
    val isRepeatActive = state.repeatState != "off"
    val isLyricsActive = state.isLyricsVisible

    val shuffleColor = if (isShuffleActive) activeColor else inactiveColor
    val repeatColor = if (isRepeatActive) activeColor else inactiveColor
    val lyricsColor = if (isLyricsActive) activeColor else inactiveButtonColor

    val shuffleIcon = if (state.shuffleState == "smart") R.drawable.ic_shuffle_mix else R.drawable.ic_shuffle
    val repeatIcon = if (state.repeatState == "one") R.drawable.ic_repeat_one else R.drawable.ic_repeat

    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AiryIconButton(iconRes = shuffleIcon, tint = shuffleColor, size = StandardIconSize) {
            onAction(PlayerAction.Shuffle)
        }

        AiryIconButton(iconRes = R.drawable.ic_lyrics, tint = lyricsColor, size = LyricsIconSize) {
            onAction(PlayerAction.Lyrics)
        }

        AiryIconButton(iconRes = repeatIcon, tint = repeatColor, size = StandardIconSize) {
            onAction(PlayerAction.Repeat)
        }
    }
}

@Composable
private fun AiryIconButton(
    iconRes: Int,
    tint: Color,
    size: androidx.compose.ui.unit.Dp,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
        label = "AiryButtonBounce"
    )

    Column(
        modifier = Modifier
            .size(ButtonClickAreaSize)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size)
        )
    }
}
