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

sealed interface SpotifyRecentItem {
    val id: String
    val name: String
    val imageUrl: String?

    data class Playlist(
        override val id: String,
        override val name: String,
        override val imageUrl: String?
    ) : SpotifyRecentItem

    data class Album(
        override val id: String,
        override val name: String,
        override val imageUrl: String?,
        val artists: List<SpotifyArtist>
    ) : SpotifyRecentItem
}

sealed interface SpotifyHomeScreenState {
    data object Loading : SpotifyHomeScreenState
    data class Success(
        val sections: List<SpotifyHomeSection>,
        val recentItems: List<SpotifyRecentItem> = emptyList(),
        val frequentArtists: List<SpotifyArtist> = emptyList()
    ) : SpotifyHomeScreenState
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
                var frequentArtists = emptyList<SpotifyArtist>()
                var recentItems = emptyList<SpotifyRecentItem>()

                val (topTracksResult, newReleasesResult, homeResult, topArtistsResult) = coroutineScope {
                    val topTracksDeferred = async { Spotify.topTracks(limit = 20) }
                    val newReleasesDeferred = async { Spotify.newReleases(limit = 20) }
                    val homeDeferred = async { Spotify.home(sectionItemsLimit = 10) }
                    val topArtistsDeferred = async { Spotify.topArtists(limit = 20) }
                    
                    data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
                    Quadruple(
                        topTracksDeferred.await(),
                        newReleasesDeferred.await(),
                        homeDeferred.await(),
                        topArtistsDeferred.await()
                    )
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

                topArtistsResult.onSuccess { topArtists ->
                    frequentArtists = topArtists.items
                }

                homeResult.onSuccess { feed ->
                    feed.sections.forEach { raw ->
                        val converted = convertHomeSection(raw)
                        if (converted != null) {
                            sections.add(converted)
                        }
                    }
                    
                    val allRecent = mutableListOf<SpotifyRecentItem>()
                    feed.sections.forEach { section ->
                        section.items.forEach { item ->
                            when (item) {
                                is SpotifyHomeFeedItem.Playlist -> {
                                    allRecent.add(
                                        SpotifyRecentItem.Playlist(
                                            id = item.id,
                                            name = item.name,
                                            imageUrl = item.imageUrl
                                        )
                                    )
                                }
                                is SpotifyHomeFeedItem.Album -> {
                                    allRecent.add(
                                        SpotifyRecentItem.Album(
                                            id = item.id,
                                            name = item.name,
                                            imageUrl = item.imageUrl,
                                            artists = item.artists.map {
                                                SpotifyArtist(
                                                    id = it.id ?: "",
                                                    name = it.name,
                                                    uri = it.uri
                                                )
                                            }
                                        )
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                    recentItems = allRecent.distinctBy { it.id }.take(10)
                }

                if (sections.isEmpty()) {
                    _screenState.update { SpotifyHomeScreenState.Empty }
                } else {
                    _screenState.update { 
                        SpotifyHomeScreenState.Success(
                            sections = sections,
                            recentItems = recentItems,
                            frequentArtists = frequentArtists
                        ) 
                    }
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
