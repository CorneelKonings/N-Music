package moe.rukamori.archivetune.ui.player.player_0

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import moe.rukamori.archivetune.ui.player.lyrics_0.LyricsColumn
import moe.rukamori.archivetune.ui.player.lyrics_0.LyricsHeader
import moe.rukamori.archivetune.ui.player.player_0.buttons.PlayerAction
import moe.rukamori.archivetune.ui.player.player_0.scoped.ActiveDragSheet
import moe.rukamori.archivetune.ui.player.player_0.scoped.FullPlayerVisualState
import moe.rukamori.archivetune.ui.player.player_0.scoped.SheetVerticalDragGestureHandler
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
    expansionFractionProvider: () -> Float,
    lyricsFractionProvider: () -> Float,
    queueFractionProvider: () -> Float,
    progressMsProvider: () -> Long,
    fullPlayerVisualState: FullPlayerVisualState,
    onAction: (PlayerAction) -> Unit,
    onCloseLyricsClick: () -> Unit,
    onCloseQueueClick: () -> Unit = {},
    onMoreLyricsClick: () -> Unit,
    onSearchLyricsClick: () -> Unit,
    onCollapseClick: () -> Unit,
    onExpandClick: () -> Unit,
    onSeek: (Float) -> Unit,
    onBackgroundStyleChanged: (Boolean) -> Unit,
    onImmersiveChanged: (Boolean) -> Unit,
    onOpenSettingsMenu: (PlayerMenuScreen) -> Unit,
    modifier: Modifier = Modifier,
    onSeekStarted: () -> Unit,
    dragHandler: SheetVerticalDragGestureHandler? = null
) {
    val density = LocalDensity.current.density

    val lyricsListState = rememberLazyListState()
    val canDragLyrics by remember(lyricsListState) {
        derivedStateOf {
            lyricsListState.firstVisibleItemIndex == 0 && lyricsListState.firstVisibleItemScrollOffset == 0
        }
    }
    val lyricsNestedScrollConnection = remember(dragHandler, canDragLyrics) {
        dragHandler?.createNestedScrollConnection(
            canDragProvider = { canDragLyrics },
            targetSheet = ActiveDragSheet.LYRICS
        )
    }

    var isQueueReordering by remember { mutableStateOf(false) }
    val queueListState = rememberLazyListState()
    val canDragQueue by remember(queueListState, isQueueReordering) {
        derivedStateOf {
            !isQueueReordering && queueListState.firstVisibleItemIndex == 0 && queueListState.firstVisibleItemScrollOffset == 0
        }
    }
    val queueNestedScrollConnection = remember(dragHandler, canDragQueue) {
        dragHandler?.createNestedScrollConnection(
            canDragProvider = { canDragQueue },
            targetSheet = ActiveDragSheet.QUEUE
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = expansionFractionProvider()
                }
        ) {
            moe.rukamori.archivetune.ui.player.player_0.PlayerBackgroundLayers(
                state = state,
                lyricsFractionProvider = lyricsFractionProvider,
                queueFractionProvider = queueFractionProvider,
            )
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

        if (hasTrack) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val maxFraction = maxOf(lyricsFractionProvider(), queueFractionProvider())
                        val expansionFraction = expansionFractionProvider()
                        val baseAlpha = if (expansionFraction < 0.005f) 0f else fullPlayerVisualState.contentAlpha
                        alpha = baseAlpha * (1f - maxFraction)
                        translationY = fullPlayerVisualState.translationY - (200f * density * maxFraction)
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
                    onSeekStarted = onSeekStarted,
                    lyricsFractionProvider = lyricsFractionProvider,
                    queueFractionProvider = queueFractionProvider
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val fraction = lyricsFractionProvider()
                        alpha = fraction
                        translationY = if (fraction <= 0f) size.height else (1f - fraction) * (200f * density)
                    }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    LyricsHeader(
                        state = state,
                        animateProgressProvider = lyricsFractionProvider,
                        onCloseClick = onCloseLyricsClick,
                        onMoreClick = onMoreLyricsClick
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .then(
                                if (lyricsNestedScrollConnection != null) {
                                    Modifier.nestedScroll(lyricsNestedScrollConnection)
                                } else {
                                    Modifier
                                }
                            )
                            .sheetBackground(state)
                    ) {
                        LyricsColumn(
                            state = state,
                            animateProgressProvider = lyricsFractionProvider,
                            progressMsProvider = progressMsProvider,
                            onCloseClick = onCloseLyricsClick,
                            onMoreClick = onMoreLyricsClick,
                            onSearchClick = onSearchLyricsClick,
                            lazyListState = lyricsListState,
                            onAction = onAction,
                            onLineClick = { timeMs -> onSeek(timeMs.toFloat()) },
                            onSeek = onSeek,
                            onSeekStarted = onSeekStarted
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val fraction = queueFractionProvider()
                        alpha = fraction
                        translationY = if (fraction <= 0f) size.height else (1f - fraction) * (200f * density)
                    }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    LyricsHeader(
                        state = state,
                        animateProgressProvider = queueFractionProvider,
                        onCloseClick = onCloseQueueClick,
                        onMoreClick = onMoreLyricsClick
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .then(
                                if (queueNestedScrollConnection != null && !isQueueReordering) {
                                    Modifier.nestedScroll(queueNestedScrollConnection)
                                } else {
                                    Modifier
                                }
                            )
                            .sheetBackground(state)
                    ) {
                        QueueScreen(
                            state = queueState,
                            onAction = onAction,
                            lazyListState = queueListState,
                            contentPadding = PaddingValues(
                                top = 8.dp,
                                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp
                            ),
                            queueFractionProvider = queueFractionProvider,
                            onReorderStateChange = { isQueueReordering = it },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Modifier.sheetBackground(state: PlayerUiState): Modifier {
    val animatedDarkMuted by animateColorAsState(
        targetValue = Color(state.darkMutedColor),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "SheetDarkMutedAnimation",
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

    return this
        .glassBorder(
            shape = cardShape,
            strokeWidth = SettingsDimensions.GlassBorderThickness,
            topAlpha = 0.20f,
            bottomAlpha = 0.04f,
        )
        .clip(cardShape)
        .background(
            if (state.isBlurBackgroundEnabled || state.isImmersiveEnabled) {
                Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.2f)))
            } else {
                cardBackgroundBrush
            }
        )
}

