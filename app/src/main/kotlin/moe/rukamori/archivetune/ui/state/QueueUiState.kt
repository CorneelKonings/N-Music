package moe.rukamori.archivetune.ui.state

import androidx.compose.runtime.Immutable
import androidx.media3.common.Timeline

@Immutable
data class QueueUiState(
    val queueWindows: List<Timeline.Window> = emptyList(),
    val currentWindowIndex: Int = -1,
)
