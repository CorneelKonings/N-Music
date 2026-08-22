package moe.rukamori.archivetune.ui.player.player_0

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import moe.rukamori.archivetune.ui.player.lyrics_0.LyricsColumn
import moe.rukamori.archivetune.ui.player.player_0.scoped.FullPlayerVisualState
import moe.rukamori.archivetune.ui.state.PlayerUiState
import moe.rukamori.archivetune.ui.state.UpdateState
import moe.rukamori.archivetune.ui.player.player_0.buttons.PlayerAction
import moe.rukamori.archivetune.ui.player.player_0.sett.PlayerMenuScreen

@Composable
internal fun UnifiedPlayerSheetLayers(
    state: PlayerUiState,
    updateState: UpdateState,
    expansionFractionProvider: () -> Float,
    lyricsFractionProvider: () -> Float,
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

        // ==========================================
        // СЛОЙ 1: МИНИ-ПЛЕЕР
        // ==========================================
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

        // ==========================================
        // СЛОЙ 2: БОЛЬШОЙ ПУЛЬТ
        // ==========================================
        val hasTrack by remember(state.title) {
            derivedStateOf { state.title.isNotEmpty() }
        }

        val isCollapsed by remember {
            derivedStateOf { expansionFractionProvider() < 0.01f }
        }

        val effectiveState = if (isCollapsed) {
            remember(
                state.title,
                state.artist,
                state.coverUrl,
                state.coverDrawable,
                state.trackUrl,
                state.isPlaying,
                state.isLiked,
                state.vibrantColor,
                state.gradientColor,
                state.durationMs,
                state.placeholderResId,
                state.isLyricsVisible,
                state.isBlurBackgroundEnabled,
                state.isImmersiveEnabled,
                state.showCodecInfo,
                state.isAlbumCoverGlowEnabled,
                state.codecInfo
            ) {
                state.copy(progressMs = 0L)
            }
        } else {
            state
        }

        if (hasTrack) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .conditionalPlacement {
                        expansionFractionProvider() >= 0.005f && lyricsFractionProvider() < 0.995f
                    }
                    .graphicsLayer {
                        val expansionFraction = expansionFractionProvider()
                        val lyricsFraction = lyricsFractionProvider()
                        val baseAlpha = if (expansionFraction < 0.005f) 0f else fullPlayerVisualState.contentAlpha
                        alpha = baseAlpha * (1f - lyricsFraction)
                        translationY = fullPlayerVisualState.translationY - (200f * density * lyricsFraction)
                    }
            ) {
                FullPlayer(
                    state = effectiveState,
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

        // ==========================================
        // СЛОЙ 3: ЭКРАН ЛИРИКИ
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .conditionalPlacement { lyricsFractionProvider() >= 0.005f }
                .graphicsLayer {
                    alpha = lyricsFractionProvider()
                }
        ) {
            LyricsColumn(
                state = effectiveState,
                animateProgressProvider = lyricsFractionProvider,
                progressMsProvider = progressMsProvider,
                onCloseClick = onCloseLyricsClick,
                onPlayPauseClick = { onAction(PlayerAction.PlayPause) },
                onMoreClick = onMoreLyricsClick,
                onSearchClick = onSearchLyricsClick,
                onLineClick = { timeMs -> onSeek(timeMs.toFloat()) },
                swipeOffsetY = lyricsSwipeOffsetY,
                onSwipeOffsetChange = onLyricsSwipeOffsetChanged
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
