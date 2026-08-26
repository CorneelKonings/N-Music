package moe.rukamori.archivetune.ui.player.lyrics_0

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import moe.rukamori.archivetune.ui.player.player_0.buttons.PlayerAction
import moe.rukamori.archivetune.ui.state.PlayerUiState

@Composable
fun LyricsColumn(
    state: PlayerUiState,
    animateProgressProvider: () -> Float,
    progressMsProvider: () -> Long,
    onCloseClick: () -> Unit,
    onMoreClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (PlayerAction) -> Unit,
    onLineClick: (Long) -> Unit,
    onSeek: (Float) -> Unit,
    onSeekStarted: () -> Unit
) {
    val lazyListState = rememberLazyListState()

    key(state.isLyricsVisible) {
        BackHandler(enabled = state.isLyricsVisible) {
            onCloseClick()
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LyricsContentCard(
            state = state,
            animateProgressProvider = animateProgressProvider,
            progressMsProvider = progressMsProvider,
            onSearchClick = onSearchClick,
            lazyListState = lazyListState,
            onLineClick = onLineClick,
            onAction = onAction,
            onSeek = onSeek,
            onSeekStarted = onSeekStarted
        )
    }
}
