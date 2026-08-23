@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package moe.rukamori.archivetune.ui.player.lyrics_0

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import moe.rukamori.archivetune.constants.ShowLyricsPlayerControlsKey
import moe.rukamori.archivetune.ui.component.LyricsEnhanced
import moe.rukamori.archivetune.ui.player.player_0.PlayerSeekBar
import moe.rukamori.archivetune.ui.player.player_0.buttons.PlayerAction
import moe.rukamori.archivetune.ui.state.PlayerUiState
import moe.rukamori.archivetune.ui.theme.transparentIconShadow
import moe.rukamori.archivetune.ui.utils.bounceClick
import moe.rukamori.archivetune.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsContentCard(
    state: PlayerUiState,
    animateProgressProvider: () -> Float,
    progressMsProvider: () -> Long,
    onSearchClick: () -> Unit = {},
    lazyListState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier,
    onLineClick: (Long) -> Unit = {},
    onAction: (PlayerAction) -> Unit,
    onSeek: (Float) -> Unit,
    onSeekStarted: () -> Unit,
) {
    val context = LocalContext.current
    val screenHeightPx = remember { context.resources.displayMetrics.heightPixels.toFloat() }

    val (showPlayerControls) = rememberPreference(ShowLyricsPlayerControlsKey, defaultValue = true)

    val animatedDarkMuted by animateColorAsState(
        targetValue = Color(state.darkMutedColor),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "LyricsBgAnimation",
    )

    val cardBackgroundBrush =
        remember(animatedDarkMuted) {
            val startColor = lerp(animatedDarkMuted, Color.Black, 0.7f)
            val midColor = animatedDarkMuted
            val endColor = Color(0xFF121212)

            Brush.verticalGradient(
                0.0f to startColor,
                0.2f to midColor,
                1.0f to endColor,
            )
        }

    Column(modifier = modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(120.dp))

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .graphicsLayer {
                        translationY = screenHeightPx * (1f - animateProgressProvider())
                    }.clipToBounds(),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .layout { measurable, constraints ->
                            val borderPx = 1.dp.roundToPx()
                            val expandedConstraints =
                                constraints.copy(
                                    minWidth = constraints.maxWidth + borderPx * 2,
                                    maxWidth = constraints.maxWidth + borderPx * 2,
                                    minHeight = constraints.maxHeight + borderPx,
                                    maxHeight = constraints.maxHeight + borderPx,
                                )
                            val placeable = measurable.measure(expandedConstraints)
                            layout(constraints.maxWidth, constraints.maxHeight) {
                                placeable.place(-borderPx, 0)
                            }
                        }.border(
                            BorderStroke(1.dp, Color(0x22FFFFFF)),
                            RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        ).clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(
                            if (state.isBlurBackgroundEnabled) {
                                Color.Black.copy(alpha = 0.2f)
                            } else {
                                Color.Transparent
                            },
                        ).then(
                            if (!state.isBlurBackgroundEnabled) {
                                Modifier.background(cardBackgroundBrush)
                            } else {
                                Modifier
                            },
                        ),
            ) {
                LyricsEnhanced(
                    sliderPositionProvider = progressMsProvider,
                    lyricsSyncOffset = state.lyricsSyncOffset,
                    textColorOverride = Color.White,
                    modifier = Modifier.fillMaxSize(),
                )

                if (showPlayerControls) {
                    val animatedAccentColor by animateColorAsState(
                        targetValue = Color(state.vibrantColor),
                        animationSpec = tween(500),
                        label = "LyricsTransportAccent",
                    )

                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 20.dp, vertical = 24.dp)
                                .fillMaxWidth()
                                .layout { measurable, constraints ->
                                    val placeable = measurable.measure(constraints)
                                    if (animateProgressProvider() >= 0.05f) {
                                        layout(placeable.width, placeable.height) {
                                            placeable.placeRelative(0, 0)
                                        }
                                    } else {
                                        layout(0, 0) {}
                                    }
                                }
                                .graphicsLayer {
                                    val progress = animateProgressProvider()
                                    alpha = progress
                                    scaleX = 0.88f + (0.12f * progress)
                                    scaleY = 0.88f + (0.12f * progress)
                                    translationY = 40f * (1f - progress)
                                }
                                .clip(RoundedCornerShape(32.dp))
                                .background(animatedAccentColor.copy(alpha = 0.12f))
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        Brush.verticalGradient(
                                            listOf(
                                                animatedAccentColor.copy(alpha = 0.18f),
                                                animatedAccentColor.copy(alpha = 0.08f),
                                            ),
                                        ),
                                    ),
                                    RoundedCornerShape(32.dp),
                                )
                                .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            PlayerSeekBar(
                                state = state,
                                progressMs = progressMsProvider(),
                                durationMs = state.durationMs,
                                vibrantColor = Color(state.vibrantColor),
                                slideOffset = { 1f },
                                onSeek = onSeek,
                                onSeekStarted = onSeekStarted,
                            )

                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val playPauseIcon = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow

                                Box(
                                    modifier =
                                        Modifier
                                            .bounceClick(pressedScale = 0.90f) {
                                                onAction(PlayerAction.Previous)
                                            }
                                            .size(48.dp)
                                            .transparentIconShadow(alpha = 0.1f, shadowRadius = 15.dp)
                                            .clip(CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Image(
                                        painter = rememberVectorPainter(Icons.Rounded.SkipPrevious),
                                        contentDescription = "Previous Track",
                                        modifier = Modifier.size(36.dp),
                                        colorFilter = ColorFilter.tint(Color.White),
                                    )
                                }

                                Box(
                                    modifier =
                                        Modifier
                                            .bounceClick(pressedScale = 0.92f) {
                                                if (!state.isLoading) onAction(PlayerAction.PlayPause)
                                            }
                                            .size(68.dp)
                                            .clip(CircleShape)
                                            .drawBehind {
                                                drawCircle(animatedAccentColor)
                                            },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (state.isLoading) {
                                        CircularWavyProgressIndicator(
                                            modifier = Modifier.size(42.dp),
                                            color = Color(0xFF121212),
                                        )
                                    } else {
                                        Image(
                                            painter = rememberVectorPainter(playPauseIcon),
                                            contentDescription = "Play/Pause",
                                            modifier = Modifier.size(48.dp),
                                            colorFilter = ColorFilter.tint(Color(0xFF121212)),
                                        )
                                    }
                                }

                                Box(
                                    modifier =
                                        Modifier
                                            .bounceClick(pressedScale = 0.90f) {
                                                onAction(PlayerAction.Next)
                                            }
                                            .size(48.dp)
                                            .transparentIconShadow(alpha = 0.1f, shadowRadius = 15.dp)
                                            .clip(CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Image(
                                        painter = rememberVectorPainter(Icons.Rounded.SkipNext),
                                        contentDescription = "Next Track",
                                        modifier = Modifier.size(36.dp),
                                        colorFilter = ColorFilter.tint(Color.White),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
