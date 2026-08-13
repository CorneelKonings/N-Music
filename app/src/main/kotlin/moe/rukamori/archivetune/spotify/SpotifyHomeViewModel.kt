package moe.rukamori.archivetune.spotify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import moe.rukamori.archivetune.R
import moe.rukamori.archivetune.spotify.models.SpotifyAlbum
import moe.rukamori.archivetune.spotify.models.SpotifyArtist
import moe.rukamori.archivetune.spotify.models.SpotifyHomeFeedItem
import moe.rukamori.archivetune.spotify.models.SpotifyHomeFeedSection
import moe.rukamori.archivetune.spotify.models.SpotifyImage
import moe.rukamori.archivetune.spotify.models.SpotifyPlaylist
import moe.rukamori.archivetune.spotify.models.SpotifyPlaylistOwner
import moe.rukamori.archivetune.spotify.models.SpotifyPlaylistTracksRef
import javax.inject.Inject

sealed interface SpotifyHomeScreenState {
    data object Loading : SpotifyHomeScreenState
    data class Success(val sections: List<SpotifyHomeSection>) : SpotifyHomeScreenState
    data object Empty : SpotifyHomeScreenState
    data class Error(val messageResId: Int, val notAuthenticated: Boolean = false) : SpotifyHomeScreenState
}

sealed interface SpotifyHomeAction {
    data object Refresh : SpotifyHomeAction
}

@HiltViewModel
class SpotifyHomeViewModel @Inject constructor(
    private val repository: SpotifyLibraryRepository
) : ViewModel() {

    private val _screenState = MutableStateFlow<SpotifyHomeScreenState>(SpotifyHomeScreenState.Loading)
    val screenState: StateFlow<SpotifyHomeScreenState> = _screenState.asStateFlow()

    init {
        load()
    }

    fun onAction(action: SpotifyHomeAction) {
        when (action) {
            SpotifyHomeAction.Refresh -> load()
        }
    }

    private fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            _screenState.update { SpotifyHomeScreenState.Loading }

            try {
                val session = repository.restoreSession()
                if (!session.isAuthenticated) {
                    _screenState.update { SpotifyHomeScreenState.Error(R.string.spotify_not_connected, notAuthenticated = true) }
                    return@launch
                }

                val sections = mutableListOf<SpotifyHomeSection>()

                val (topTracksResult, newReleasesResult, homeResult) = coroutineScope {
                    val topTracksDeferred = async { Spotify.topTracks(limit = 20) }
                    val newReleasesDeferred = async { Spotify.newReleases(limit = 20) }
                    val homeDeferred = async { Spotify.home(sectionItemsLimit = 10) }
                    Triple(topTracksDeferred.await(), newReleasesDeferred.await(), homeDeferred.await())
                }

                topTracksResult.onSuccess { topTracks ->
                    if (topTracks.items.isNotEmpty()) {
                        sections.add(
                            SpotifyHomeSection(
                                title = "spotify_top_tracks",
                                type = SectionType.TRACKS,
                                tracks = topTracks.items
                            )
                        )
                    }
                }

                newReleasesResult.onSuccess { newReleases ->
                    val albums = newReleases.albums?.items.orEmpty()
                    if (albums.isNotEmpty()) {
                        sections.add(
                            SpotifyHomeSection(
                                title = "spotify_new_releases",
                                type = SectionType.ALBUMS,
                                albums = albums
                            )
                        )
                    }
                }

                homeResult.onSuccess { feed ->
                    feed.sections.forEach { raw ->
                        val converted = convertHomeSection(raw)
                        if (converted != null) {
                            sections.add(converted)
                        }
                    }
                }

                if (sections.isEmpty()) {
                    _screenState.update { SpotifyHomeScreenState.Empty }
                } else {
                    _screenState.update { SpotifyHomeScreenState.Success(sections) }
                }

            } catch (e: Exception) {
                if (e is Spotify.SpotifyException && e.statusCode == 401) {
                    _screenState.update { SpotifyHomeScreenState.Error(R.string.spotify_not_connected, notAuthenticated = true) }
                } else {
                    _screenState.update { SpotifyHomeScreenState.Error(R.string.error_unknown) }
                }
            }
        }
    }

    private fun convertHomeSection(feedSection: SpotifyHomeFeedSection): SpotifyHomeSection? {
        val title = feedSection.title ?: return null

        val playlists = feedSection.items.filterIsInstance<SpotifyHomeFeedItem.Playlist>()
        val albums = feedSection.items.filterIsInstance<SpotifyHomeFeedItem.Album>()
        val artists = feedSection.items.filterIsInstance<SpotifyHomeFeedItem.Artist>()

        val counts = listOf(
            SectionType.PLAYLISTS to playlists.size,
            SectionType.ALBUMS to albums.size,
            SectionType.ARTISTS to artists.size,
        )
        val (dominant, size) = counts.maxByOrNull { it.second } ?: return null
        if (size == 0) return null

        return when (dominant) {
            SectionType.PLAYLISTS -> SpotifyHomeSection(
                title = title,
                type = SectionType.PLAYLISTS,
                playlists = playlists.map {
                    SpotifyPlaylist(
                        id = it.id,
                        name = it.name,
                        description = it.description,
                        images = listOfNotNull(it.imageUrl?.let { url -> SpotifyImage(url, null, null) }),
                        owner = it.ownerName?.let { owner -> SpotifyPlaylistOwner(id = "", displayName = owner) },
                        tracks = SpotifyPlaylistTracksRef(total = it.totalCount),
                        uri = it.uri
                    )
                }
            )
            SectionType.ALBUMS -> SpotifyHomeSection(
                title = title,
                type = SectionType.ALBUMS,
                albums = albums.map {
                    SpotifyAlbum(
                        id = it.id,
                        name = it.name,
                        albumType = it.albumType,
                        artists = it.artists,
                        images = listOfNotNull(it.imageUrl?.let { url -> SpotifyImage(url, null, null) }),
                        uri = it.uri
                    )
                }
            )
            SectionType.ARTISTS -> SpotifyHomeSection(
                title = title,
                type = SectionType.ARTISTS,
                artists = artists.map {
                    SpotifyArtist(
                        id = it.id,
                        name = it.name,
                        images = listOfNotNull(it.imageUrl?.let { url -> SpotifyImage(url, null, null) }),
                        uri = it.uri
                    )
                }
            )
            else -> null
        }
    }
}
