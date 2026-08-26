package moe.rukamori.archivetune.ui.player.player_0

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import moe.rukamori.archivetune.R
import moe.rukamori.archivetune.ui.player.lyrics_0.LyricsColumn
import moe.rukamori.archivetune.ui.player.lyrics_0.LyricsHeader
import moe.rukamori.archivetune.ui.player.player_0.buttons.PlayerAction
import moe.rukamori.archivetune.ui.player.player_0.scoped.FullPlayerVisualState
import moe.rukamori.archivetune.ui.player.player_0.sett.PlayerMenuScreen
import moe.rukamori.archivetune.ui.player.queue_0.QueueScreen
import moe.rukamori.archivetune.ui.settings.SettingsDimensions
import moe.rukamori.archivetune.ui.state.PlayerUiState
import moe.rukamori.archivetune.ui.state.QueueUiState
import moe.rukamori.archivetune.ui.state.UpdateState
import moe.rukamori.archivetune.ui.theme.glassBorder

@Composable
internal fun UnifiedPlayerSheetLayers(
    state: PlayerUiState,
    queueState: QueueUiState,
    updateState: UpdateState,
    pagerState: PagerState,
    expansionFractionProvider: () -> Float,
    layerTwoFractionProvider: () -> Float,
    progressMsProvider: () -> Long,
    fullPlayerVisualState: FullPlayerVisualState,
    onAction: (PlayerAction) -> Unit,
    onCloseLyricsClick: () -> Unit,
    onMoreLyricsClick: () -> Unit,
    onSearchLyricsClick: () -> Unit,
    onCollapseClick: () -> Unit,
    onExpandClick: () -> Unit,
    onSeek: (Float) -> Unit,
    onBackgroundStyleChanged: (Boolean) -> Unit,
    onImmersiveChanged: (Boolean) -> Unit,
    onOpenSettingsMenu: (PlayerMenuScreen) -> Unit,
    modifier: Modifier = Modifier,
    onSeekStarted: () -> Unit
) {
    val density = LocalDensity.current.density

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .conditionalPlacement { expansionFractionProvider() < 0.99f }
                .graphicsLayer {
                    val fraction = expansionFractionProvider()
                    alpha = (1f - (fraction / 0.3f)).coerceIn(0f, 1f)
                }
        ) {
            MiniPlayerContentInternal(
                state = state,
                expansionFractionProvider = expansionFractionProvider,
                onAction = onAction,
                onMediaAreaClick = onExpandClick
            )
        }

        val hasTrack by remember(state.title) {
            derivedStateOf { state.title.isNotEmpty() }
        }

        if (hasTrack) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .conditionalPlacement {
                        expansionFractionProvider() >= 0.005f && layerTwoFractionProvider() < 0.995f
                    }
                    .graphicsLayer {
                        val expansionFraction = expansionFractionProvider()
                        val layerTwoFraction = layerTwoFractionProvider()
                        val baseAlpha = if (expansionFraction < 0.005f) 0f else fullPlayerVisualState.contentAlpha
                        alpha = baseAlpha * (1f - layerTwoFraction)
                        translationY = fullPlayerVisualState.translationY - (200f * density * layerTwoFraction)
                    }
            ) {
                FullPlayer(
                    state = state,
                    progressMsProvider = progressMsProvider,
                    updateState = updateState,
                    slideOffset = expansionFractionProvider,
                    density = density,
                    onCollapseClick = onCollapseClick,
                    onAction = onAction,
                    onSeek = onSeek,
                    onBackgroundStyleChanged = onBackgroundStyleChanged,
                    onImmersiveChanged = onImmersiveChanged,
                    onOpenSettingsMenu = onOpenSettingsMenu,
                    onSeekStarted = onSeekStarted
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .conditionalPlacement { layerTwoFractionProvider() > 0.005f }
                .graphicsLayer {
                    val fraction = layerTwoFractionProvider()
                    alpha = fraction
                    translationY = (1f - fraction) * (200f * density)
                }
        ) {
            val animatedDarkMuted by animateColorAsState(
                targetValue = Color(state.darkMutedColor),
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                label = "LayerTwoDarkMutedAnimation",
            )

            val cardBackgroundBrush = remember(animatedDarkMuted) {
                val startColor = lerp(animatedDarkMuted, Color.Black, 0.7f)
                val midColor = animatedDarkMuted
                val endColor = Color(0xFF121212)

                Brush.verticalGradient(
                    0.0f to startColor,
                    0.2f to midColor,
                    1.0f to endColor,
                )
            }

            val cardShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)

            Column(modifier = Modifier.fillMaxSize()) {
                UnifiedLayerTwoHeader(
                    state = state,
                    pagerState = pagerState,
                    animateProgressProvider = layerTwoFractionProvider,
                    onCloseClick = onCloseLyricsClick,
                    onMoreClick = onMoreLyricsClick
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .glassBorder(
                            shape = cardShape,
                            strokeWidth = SettingsDimensions.GlassBorderThickness,
                            topAlpha = 0.20f,
                            bottomAlpha = 0.04f,
                        )
                        .clip(cardShape)
                        .background(
                            if (state.isBlurBackgroundEnabled) {
                                Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.2f)))
                            } else {
                                cardBackgroundBrush
                            }
                        )
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> {
                                LyricsColumn(
                                    state = state,
                                    animateProgressProvider = layerTwoFractionProvider,
                                    progressMsProvider = progressMsProvider,
                                    onCloseClick = onCloseLyricsClick,
                                    onAction = onAction,
                                    onMoreClick = onMoreLyricsClick,
                                    onSearchClick = onSearchLyricsClick,
                                    onLineClick = { timeMs -> onSeek(timeMs.toFloat()) },
                                    onSeek = onSeek,
                                    onSeekStarted = onSeekStarted
                                )
                            }
                            1 -> {
                                QueueScreen(
                                    state = queueState,
                                    onAction = onAction,
                                    contentPadding = WindowInsets.systemBars.asPaddingValues(),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnifiedLayerTwoHeader(
    state: PlayerUiState,
    pagerState: PagerState,
    animateProgressProvider: () -> Float,
    onCloseClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LyricsHeader(
            state = state,
            animateProgressProvider = animateProgressProvider,
            onCloseClick = onCloseClick,
            onMoreClick = onMoreClick
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = pagerState.currentPage == 0,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                },
                label = {
                    Text(
                        text = stringResource(R.string.lyrics),
                        fontWeight = if (pagerState.currentPage == 0) FontWeight.Bold else FontWeight.Normal
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = if (state.isBlurBackgroundEnabled) Color.White.copy(alpha = 0.25f) else Color(state.vibrantColor).copy(alpha = 0.35f),
                    selectedLabelColor = Color.White,
                    containerColor = if (state.isBlurBackgroundEnabled) Color.Black.copy(alpha = 0.3f) else Color(state.darkMutedColor).copy(alpha = 0.6f),
                    labelColor = Color.White.copy(alpha = 0.7f)
                ),
                border = null
            )

            FilterChip(
                selected = pagerState.currentPage == 1,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                },
                label = {
                    Text(
                        text = stringResource(R.string.queue),
                        fontWeight = if (pagerState.currentPage == 1) FontWeight.Bold else FontWeight.Normal
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = if (state.isBlurBackgroundEnabled) Color.White.copy(alpha = 0.25f) else Color(state.vibrantColor).copy(alpha = 0.35f),
                    selectedLabelColor = Color.White,
                    containerColor = if (state.isBlurBackgroundEnabled) Color.Black.copy(alpha = 0.3f) else Color(state.darkMutedColor).copy(alpha = 0.6f),
                    labelColor = Color.White.copy(alpha = 0.7f)
                ),
                border = null
            )
        }
    }
}

private fun Modifier.conditionalPlacement(shouldPlace: () -> Boolean): Modifier = this.layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    if (shouldPlace()) {
        layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    } else {
        layout(0, 0) {
        }
    }
}
