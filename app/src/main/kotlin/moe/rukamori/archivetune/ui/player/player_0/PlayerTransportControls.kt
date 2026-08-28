package moe.rukamori.archivetune.ui.player.player_0

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import moe.rukamori.archivetune.R
import moe.rukamori.archivetune.ui.player.player_0.buttons.PlayerAction
import moe.rukamori.archivetune.ui.state.PlayerUiState
import moe.rukamori.archivetune.ui.theme.transparentIconShadow
import moe.rukamori.archivetune.ui.utils.bounceClick

private val CapsuleHorizontalPad = 12.dp
private val CapsuleHeight = 76.dp
private val CapsulePadHorizontal = 20.dp
private val CapsuleBorderWidth = 0.5.dp

private val SideButtonSize = 40.dp
private val SideIconSize = 24.dp

private val OuterButtonSize = 48.dp
private val OuterIconSize = 30.dp
private val SideShadowAlpha = 0.1f
private val SideShadowRadius = 15.dp

private val CenterButtonSize = 64.dp
private val CenterIconSize = 42.dp

@Composable
fun PlayerTransportControls(
    state: PlayerUiState,
    isPlaying: Boolean,
    vibrantColor: Color,
    slideOffset: () -> Float,
    onAction: (PlayerAction) -> Unit,
    modifier: Modifier = Modifier,
    isLarge: Boolean = true,
) {
    val animatedAccentColor by animateColorAsState(
        targetValue = vibrantColor,
        animationSpec = tween(500),
        label = "AccentPaletteColor"
    )

    val playPauseIcon = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow

    val isShuffleActive = state.shuffleState != "off"
    val shuffleColor = if (isShuffleActive) animatedAccentColor else Color.White.copy(alpha = 0.5f)
    val shuffleIcon = when (state.shuffleState) {
        "smart" -> R.drawable.ic_shuffle_mix
        else -> R.drawable.ic_shuffle
    }

    val isRepeatActive = state.repeatState != "off"
    val repeatColor = if (isRepeatActive) animatedAccentColor else Color.White.copy(alpha = 0.5f)
    val repeatIcon = when (state.repeatState) {
        "one" -> R.drawable.ic_repeat_one
        "all", "on" -> R.drawable.ic_repeat_on
        else -> R.drawable.ic_repeat
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CapsuleHorizontalPad)
            .graphicsLayer {
                val offset = slideOffset()
                translationY = 80f * (1f - offset)
                alpha = if (offset > 0.3f) ((offset - 0.3f) / 0.7f) else 0f
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .bounceClick(pressedScale = 0.90f) {
                        onAction(PlayerAction.Shuffle)
                    }
                    .size(OuterButtonSize)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = shuffleIcon),
                    contentDescription = "Shuffle",
                    modifier = Modifier.size(OuterIconSize),
                    colorFilter = ColorFilter.tint(shuffleColor),
                )
            }

            Row(
                modifier = Modifier
                    .height(CapsuleHeight)
                    .clip(CircleShape)
                    .background(animatedAccentColor.copy(alpha = 0.12f))
                    .border(
                        BorderStroke(
                            CapsuleBorderWidth,
                            Brush.verticalGradient(
                                listOf(
                                    animatedAccentColor.copy(alpha = 0.18f),
                                    animatedAccentColor.copy(alpha = 0.08f),
                                ),
                            ),
                        ),
                        CircleShape,
                    )
                    .padding(horizontal = CapsulePadHorizontal),
                horizontalArrangement = Arrangement.spacedBy(22.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .bounceClick(pressedScale = 0.90f) {
                            onAction(PlayerAction.Previous)
                        }
                        .size(SideButtonSize)
                        .transparentIconShadow(alpha = SideShadowAlpha, shadowRadius = SideShadowRadius)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = rememberVectorPainter(Icons.Rounded.SkipPrevious),
                        contentDescription = "Previous Track",
                        modifier = Modifier.size(SideIconSize),
                        colorFilter = ColorFilter.tint(Color.White),
                    )
                }

                Box(
                    modifier = Modifier
                        .bounceClick(pressedScale = 0.92f) { if (!state.isLoading) onAction(PlayerAction.PlayPause) }
                        .size(CenterButtonSize)
                        .clip(CircleShape)
                        .drawBehind {
                            drawCircle(animatedAccentColor)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.isLoading) {
                        CircularWavyProgressIndicator(
                            modifier = Modifier.size(CenterIconSize - 10.dp),
                            color = Color(0xFF121212),
                        )
                    } else {
                        Image(
                            painter = rememberVectorPainter(playPauseIcon),
                            contentDescription = "Play/Pause",
                            modifier = Modifier.size(CenterIconSize),
                            colorFilter = ColorFilter.tint(Color(0xFF121212)),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .bounceClick(pressedScale = 0.90f) {
                            onAction(PlayerAction.Next)
                        }
                        .size(SideButtonSize)
                        .transparentIconShadow(alpha = SideShadowAlpha, shadowRadius = SideShadowRadius)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = rememberVectorPainter(Icons.Rounded.SkipNext),
                        contentDescription = "Next Track",
                        modifier = Modifier.size(SideIconSize),
                        colorFilter = ColorFilter.tint(Color.White),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .bounceClick(pressedScale = 0.90f) {
                        onAction(PlayerAction.Repeat)
                    }
                    .size(OuterButtonSize)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = repeatIcon),
                    contentDescription = "Repeat",
                    modifier = Modifier.size(OuterIconSize),
                    colorFilter = ColorFilter.tint(repeatColor),
                )
            }
        }
    }


}


@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PlayerTransportControlsPreview() {
    PlayerTransportControls(
        state = PlayerUiState(),
        isPlaying = true,
        vibrantColor = Color(0xFFFFEB3B),
        slideOffset = { 1f },
        onAction = {}
    )
}

