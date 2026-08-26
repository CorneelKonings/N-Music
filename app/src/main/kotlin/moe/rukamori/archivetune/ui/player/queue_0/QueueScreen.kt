/*
 * YumaPlayer (2026) | Modified work by MuwMix
 * ArchiveTune (2026) | Original work by © Rukamori
 * GPL-3.0 License | Contributors: see git history
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package moe.rukamori.archivetune.ui.player.queue_0

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Timeline
import moe.rukamori.archivetune.R
import moe.rukamori.archivetune.extensions.metadata
import moe.rukamori.archivetune.ui.component.MediaMetadataListItem
import moe.rukamori.archivetune.ui.player.player_0.buttons.PlayerAction
import moe.rukamori.archivetune.ui.state.QueueUiState

private val Timeline.Window.queueItemKey: Long
    get() =
        (uid.hashCode().toLong() shl Int.SIZE_BITS) xor
            (mediaItem.mediaId.hashCode().toLong() and UInt.MAX_VALUE.toLong())

@Composable
fun QueueScreen(
    state: QueueUiState,
    onAction: (PlayerAction) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        itemsIndexed(
            items = state.queueWindows,
            key = { index, window -> "${window.queueItemKey}_$index" },
            contentType = { _, _ -> "queue_item" },
        ) { index, window ->
            val metadata = window.mediaItem.metadata ?: return@itemsIndexed
            val isActive = index == state.currentWindowIndex

            val dismissState =
                rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                            onAction(PlayerAction.RemoveQueueItem(index))
                            true
                        } else {
                            false
                        }
                    },
                    positionalThreshold = { totalDistance -> totalDistance * 0.5f },
                )

            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {},
            ) {
                MediaMetadataListItem(
                    mediaMetadata = metadata,
                    isActive = isActive,
                    isPlaying = isActive,
                    trailingContent = {
                        IconButton(
                            onClick = { onAction(PlayerAction.RemoveQueueItem(index)) },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.close),
                                contentDescription = stringResource(R.string.remove_from_queue),
                            )
                        }
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                onAction(PlayerAction.PlayQueueItem(index))
                            },
                )
            }
        }
    }
}
