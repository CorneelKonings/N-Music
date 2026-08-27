package moe.rukamori.archivetune.ui.player.player_0

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
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
    selectedPage: Int,
    onPageSelected: (Int) -> Unit,
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
                .graphicsLayer {
                    alpha = expansionFractionProvider()
                }
        ) {
            moe.rukamori.archivetune.ui.player.player_0.PlayerBackgroundLayers(state = state)
        }

        if (expansionFractionProvider() < 1f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
        }

        val hasTrack by remember(state.title) {
            derivedStateOf { state.title.isNotEmpty() }
        }

        if (hasTrack && expansionFractionProvider() > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
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

        if (layerTwoFractionProvider() > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
                        selectedPage = selectedPage,
                        onPageSelected = onPageSelected,
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
                        AnimatedContent(
                            targetState = selectedPage,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInHorizontally(animationSpec = tween(300)) { width -> width } + fadeIn(animationSpec = tween(300)))
                                        .togetherWith(slideOutHorizontally(animationSpec = tween(300)) { width -> -width } + fadeOut(animationSpec = tween(300)))
                                } else {
                                    (slideInHorizontally(animationSpec = tween(300)) { width -> -width } + fadeIn(animationSpec = tween(300)))
                                        .togetherWith(slideOutHorizontally(animationSpec = tween(300)) { width -> width } + fadeOut(animationSpec = tween(300)))
                                }
                            },
                            label = "LayerTwoContentAnimation",
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
                                        layerTwoFractionProvider = layerTwoFractionProvider,
                                        modifier = Modifier.fillMaxSize(),
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

@Composable
private fun UnifiedLayerTwoHeader(
    state: PlayerUiState,
    selectedPage: Int,
    onPageSelected: (Int) -> Unit,
    animateProgressProvider: () -> Float,
    onCloseClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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

        val indicatorOffset by animateFloatAsState(
            targetValue = if (selectedPage == 1) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "LayerTwoSegmentedIndicatorOffset"
        )

        val barShape = RoundedCornerShape(20.dp)
        val indicatorShape = RoundedCornerShape(16.dp)

        val containerColor = if (state.isBlurBackgroundEnabled) {
            Color.Black.copy(alpha = 0.35f)
        } else {
            Color(state.darkMutedColor).copy(alpha = 0.6f)
        }

        val borderColor = if (state.isBlurBackgroundEnabled) {
            Color.White.copy(alpha = 0.15f)
        } else {
            Color.White.copy(alpha = 0.10f)
        }

        val indicatorColor = if (state.isBlurBackgroundEnabled) {
            Color.White.copy(alpha = 0.25f)
        } else {
            Color(state.vibrantColor).copy(alpha = 0.35f)
        }

        Box(
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, top = 6.dp, bottom = 8.dp)
                .width(220.dp)
                .height(40.dp)
                .clip(barShape)
                .background(containerColor)
                .border(SettingsDimensions.GlassBorderThickness, borderColor, barShape),
            contentAlignment = Alignment.CenterStart
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp)
            ) {
                val tabWidth = maxWidth / 2

                Surface(
                    shape = indicatorShape,
                    color = indicatorColor,
                    modifier = Modifier
                        .size(width = tabWidth, height = maxHeight)
                        .offset {
                            IntOffset(
                                x = (tabWidth.toPx() * indicatorOffset).roundToInt(),
                                y = 0
                            )
                        }
                ) {}

                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(indicatorShape)
                            .clickable {
                                onPageSelected(0)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.lyrics),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedPage == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedPage == 0) Color.White else Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(indicatorShape)
                            .clickable {
                                onPageSelected(1)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.queue),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedPage == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedPage == 1) Color.White else Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

