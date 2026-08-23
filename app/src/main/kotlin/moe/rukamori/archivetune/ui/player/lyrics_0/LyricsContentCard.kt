package moe.rukamori.archivetune.ui.player.lyrics_0

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moe.rukamori.archivetune.constants.ShowLyricsPlayerControlsKey
import moe.rukamori.archivetune.ui.component.LyricsEnhanced
import moe.rukamori.archivetune.ui.player.player_0.buttons.PlayerAction
import moe.rukamori.archivetune.ui.state.PlayerUiState
import moe.rukamori.archivetune.utils.makeTimeString
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

    val currentMsState =
        produceState(initialValue = 0L, progressMsProvider) {
            while (true) {
                value = progressMsProvider()
                kotlinx.coroutines.delay(32)
            }
        }

    var sliderPosition by remember { mutableStateOf(0f) }
    val isDragging = remember { mutableStateOf(false) }

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
                    sliderPositionProvider = { if (isDragging.value) sliderPosition.toLong() else progressMsProvider() },
                    lyricsSyncOffset = state.lyricsSyncOffset,
                    textColorOverride = Color.White,
                    modifier = Modifier.fillMaxSize(),
                )

                if (showPlayerControls) {
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 24.dp)
                                .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .height(72.dp)
                                    .clip(RoundedCornerShape(36.dp))
                                    .background(Color(0x33000000))
                                    .border(BorderStroke(1.dp, Color(0x33FFFFFF)), RoundedCornerShape(36.dp))
                                    .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .clickable { onAction(PlayerAction.Previous) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = rememberVectorPainter(Icons.Rounded.SkipPrevious),
                                    contentDescription = "Previous",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp),
                                )
                            }

                            Box(
                                modifier =
                                    Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .clickable { onAction(PlayerAction.PlayPause) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter =
                                        rememberVectorPainter(
                                            if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                        ),
                                    contentDescription = "Play/Pause",
                                    tint = Color.Black,
                                    modifier = Modifier.size(36.dp),
                                )
                            }

                            Box(
                                modifier =
                                    Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .clickable { onAction(PlayerAction.Next) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = rememberVectorPainter(Icons.Rounded.SkipNext),
                                    contentDescription = "Next",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp),
                                )
                            }

                            val interactionSource = remember { MutableInteractionSource() }
                            val maxRange = maxOf(1f, state.durationMs.toFloat())
                            val baseProgress = if (isDragging.value) sliderPosition else currentMsState.value.toFloat()

                            val animatedProgress by animateFloatAsState(
                                targetValue = baseProgress.coerceIn(0f, maxRange),
                                animationSpec = if (isDragging.value) snap() else tween(durationMillis = 250, easing = LinearEasing),
                                label = "SliderLineFluidAnimation",
                            )

                            Column(
                                modifier = Modifier.width(130.dp),
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Slider(
                                    value = baseProgress.coerceIn(0f, maxRange),
                                    onValueChange = {
                                        isDragging.value = true
                                        sliderPosition = it
                                        onSeekStarted()
                                    },
                                    onValueChangeFinished = {
                                        isDragging.value = false
                                        onSeek(sliderPosition)
                                    },
                                    valueRange = 0f..maxRange,
                                    interactionSource = interactionSource,
                                    track = { _ ->
                                        val fraction = (animatedProgress / maxRange).coerceIn(0f, 1f)
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.25f)),
                                            contentAlignment = Alignment.CenterStart,
                                        ) {
                                            Box(
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth(fraction)
                                                        .fillMaxHeight()
                                                        .clip(CircleShape)
                                                        .background(Color.White),
                                            )
                                        }
                                    },
                                    thumb = { Box(modifier = Modifier.size(0.dp)) },
                                    colors =
                                        SliderDefaults.colors(
                                            thumbColor = Color.Transparent,
                                            activeTickColor = Color.Transparent,
                                            inactiveTickColor = Color.Transparent,
                                            disabledThumbColor = Color.Transparent,
                                        ),
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(24.dp),
                                )

                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = makeTimeString(baseProgress.toLong()),
                                        style =
                                            MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                            ),
                                        color = Color.White.copy(alpha = 0.7f),
                                    )
                                    Text(
                                        text = makeTimeString(state.durationMs),
                                        style =
                                            MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                            ),
                                        color = Color.White.copy(alpha = 0.7f),
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
