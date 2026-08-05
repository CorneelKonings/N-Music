/*
 * YumaPlayer (2026) | Modified work by MuwMix
 * ArchiveTune (2026) | Original work by © Rukamori
 * GPL-3.0 License | Contributors: see git history
 */

package moe.rukamori.archivetune.spotify

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import moe.rukamori.archivetune.R
import moe.rukamori.archivetune.spotify.models.SpotifyTrack
import moe.rukamori.archivetune.utils.reportException
import javax.inject.Inject

@HiltViewModel
class SpotifyLikedSongsViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val repository: SpotifyLibraryRepository,
    ) : ViewModel() {
        private val _tracks = MutableStateFlow<List<SpotifyTrack>>(emptyList())
        val tracks = _tracks.asStateFlow()

        private val _total = MutableStateFlow(0)
        val total = _total.asStateFlow()

        private val _isLoading = MutableStateFlow(true)
        val isLoading = _isLoading.asStateFlow()

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing = _isRefreshing.asStateFlow()

        private val _error = MutableStateFlow<String?>(null)
        val error = _error.asStateFlow()

        init {
            loadLikedSongs()
        }

        fun refresh() {
            viewModelScope.launch(Dispatchers.IO) {
                _isRefreshing.value = true
                loadLikedSongsInternal()
                _isRefreshing.value = false
            }
        }

        fun retry() = loadLikedSongs()

        fun clearError() {
            _error.value = null
        }

        private fun loadLikedSongs() {
            viewModelScope.launch(Dispatchers.IO) {
                _isLoading.value = true
                loadLikedSongsInternal()
                _isLoading.value = false
            }
        }

        private suspend fun loadLikedSongsInternal() {
            _error.value = null

            val pageSize = 50
            val maxPages = 60
            val remoteTracks = mutableListOf<SpotifyTrack>()
            var offset = 0

            for (page in 0 until maxPages) {
                val result = Spotify.likedSongs(limit = pageSize, offset = offset).getOrNull()
                if (result == null || result.items.isEmpty()) break
                remoteTracks.addAll(result.items.mapNotNull { it.track?.takeUnless(SpotifyTrack::isLocal) })
                offset += result.items.size
                _total.value = result.total
                if (offset >= result.total || result.items.size < pageSize) break
            }

            _tracks.value = remoteTracks.toList()
        }

        companion object {
            private const val PAGE_SIZE = 50
            private const val PARALLEL_GROUP_SIZE = 5
        }
    }