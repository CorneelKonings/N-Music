package moe.rukamori.archivetune.ui.player.player_0

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
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
                // Сдвигаем ТАЧ-ЗОНУ на фазе Layout
                .offset {
                    val fraction = expansionFractionProvider()
                    if (fraction > 0.99f) IntOffset(99999, 0) else IntOffset(0, 0)
                }
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
            derivedStateOf { expansionFractionProvider() == 0f }
        }

        var frozenState by remember { mutableStateOf(state) }
        if (!isCollapsed) {
            frozenState = state
        }

        if (hasTrack) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        val fraction = expansionFractionProvider()
                        if (fraction < 0.01f) IntOffset(99999, 0) else IntOffset(0, 0)
                    }
                    .graphicsLayer {
                        val expansionFraction = expansionFractionProvider()
                        val lyricsFraction = lyricsFractionProvider()
                        val baseAlpha = if (expansionFraction < 0.005f) 0f else fullPlayerVisualState.contentAlpha
                        alpha = baseAlpha * (1f - lyricsFraction)
                        translationY = fullPlayerVisualState.translationY - (200f * density * lyricsFraction)
                    }
            ) {
                // ЧИСТЫЙ ВЫЗОВ: без удалённых параметров
                FullPlayer(
                    state = frozenState,
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
                // Сдвигаем ТАЧ-ЗОНУ: пока лирика закрыта, её сетка физически не существует для тач-системы
                .offset {
                    val lyricsFraction = lyricsFractionProvider()
                    if (lyricsFraction < 0.01f) IntOffset(99999, 0) else IntOffset(0, 0)
                }
                .graphicsLayer {
                    alpha = lyricsFractionProvider()
                }
        ) {
            LyricsColumn(
                state = frozenState,
                animateProgressProvider = lyricsFractionProvider,
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
