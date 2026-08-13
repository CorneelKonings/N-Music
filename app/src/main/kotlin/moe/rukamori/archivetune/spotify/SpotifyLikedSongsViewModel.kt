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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
                resetAndLoadFirstChunk()
                _isRefreshing.value = false
            }
        }

        fun retry() = loadLikedSongs()

        fun clearError() {
            _error.value = null
        }

        fun loadMoreSongs() {
        }

        private fun loadLikedSongs() {
            viewModelScope.launch(Dispatchers.IO) {
                _isLoading.value = true
                _error.value = null
                try {
                    val fetchedTracks = repository.likedSongs()
                    _tracks.value = fetchedTracks
                    _total.value = fetchedTracks.size
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    reportException(e)
                    _error.value = e.message
                } finally {
                    _isLoading.value = false
                }
            }
        }

        private suspend fun resetAndLoadFirstChunk() {
            _error.value = null
            try {
                val fetchedTracks = repository.likedSongs()
                _tracks.value = fetchedTracks
                _total.value = fetchedTracks.size
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Throwable) {
                reportException(e)
                _error.value = e.message
            }
        }

    }