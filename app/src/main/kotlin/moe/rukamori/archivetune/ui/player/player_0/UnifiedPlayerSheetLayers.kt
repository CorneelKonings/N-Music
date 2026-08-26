package moe.rukamori.archivetune.ui.player.player_0

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import moe.rukamori.archivetune.ui.player.lyrics_0.LyricsColumn
import moe.rukamori.archivetune.ui.player.player_0.buttons.PlayerAction
import moe.rukamori.archivetune.ui.player.player_0.scoped.FullPlayerVisualState
import moe.rukamori.archivetune.ui.player.player_0.sett.PlayerMenuScreen
import moe.rukamori.archivetune.ui.player.queue_0.QueueScreen
import moe.rukamori.archivetune.ui.state.PlayerUiState
import moe.rukamori.archivetune.ui.state.QueueUiState
import moe.rukamori.archivetune.ui.state.UpdateState

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
    lyricsSwipeOffsetY: Float,
    onLyricsSwipeOffsetChanged: (Float) -> Unit,
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
            val scope = rememberCoroutineScope()
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
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { offset ->
                                if (expansionFractionProvider() > 0.95f && layerTwoFractionProvider() < 0.05f) {
                                    val screenWidth = size.width.toFloat()
                                    val targetPage = if (offset.x < screenWidth / 2f) 0 else 1
                                    scope.launch {
                                        pagerState.scrollToPage(targetPage)
                                    }
                                }
                            },
                            onVerticalDrag = { change, dragAmount ->
                                if (expansionFractionProvider() > 0.95f && layerTwoFractionProvider() < 0.05f) {
                                    if (dragAmount < -5f) {
                                        change.consume()
                                        onAction(PlayerAction.Lyrics)
                                    }
                                }
                            }
                        )
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
                .graphicsLayer {
                    val fraction = layerTwoFractionProvider()
                    alpha = fraction
                    translationY = (1f - fraction) * (200f * density)
                }
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
                            onSeekStarted = onSeekStarted,
                            swipeOffsetY = lyricsSwipeOffsetY,
                            onSwipeOffsetChange = onLyricsSwipeOffsetChanged
                        )
                    }
                    1 -> {
                        val nestedScrollConnection = remember {
                            object : NestedScrollConnection {
                                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                                    if (lyricsSwipeOffsetY > 0f && available.y < 0f) {
                                        val newOffset = (lyricsSwipeOffsetY + available.y).coerceAtLeast(0f)
                                        onLyricsSwipeOffsetChanged(newOffset)
                                        return Offset(0f, available.y)
                                    }
                                    return Offset.Zero
                                }

                                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                                    if (available.y > 0f) {
                                        onLyricsSwipeOffsetChanged(lyricsSwipeOffsetY + available.y)
                                        return Offset(0f, available.y)
                                    }
                                    return Offset.Zero
                                }

                                override suspend fun onPreFling(available: Velocity): Velocity {
                                    if (lyricsSwipeOffsetY > 100f) {
                                        onCloseLyricsClick()
                                    } else {
                                        onLyricsSwipeOffsetChanged(0f)
                                    }
                                    return super.onPreFling(available)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(nestedScrollConnection)
                                .graphicsLayer {
                                    translationY = lyricsSwipeOffsetY
                                }
                        ) {
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
