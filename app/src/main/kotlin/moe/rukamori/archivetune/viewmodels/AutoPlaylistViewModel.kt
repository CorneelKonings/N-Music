/*
 * YumaPlayer (2026) | Modified work by MuwMix
 * ArchiveTune (2026) | Original work by © Rukamori
 * GPL-3.0 License | Contributors: see git history
 */

package moe.rukamori.archivetune.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.offline.Download
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.rukamori.archivetune.constants.AutoPlaylistSongSortDescendingKey
import moe.rukamori.archivetune.constants.AutoPlaylistSongSortType
import moe.rukamori.archivetune.constants.AutoPlaylistSongSortTypeKey
import moe.rukamori.archivetune.constants.HideExplicitKey
import moe.rukamori.archivetune.constants.HideVideoKey
import moe.rukamori.archivetune.constants.SongSortType
import moe.rukamori.archivetune.db.MusicDatabase
import moe.rukamori.archivetune.extensions.filterExplicit
import moe.rukamori.archivetune.extensions.reversed
import moe.rukamori.archivetune.extensions.toEnum
import moe.rukamori.archivetune.playback.DownloadUtil
import moe.rukamori.archivetune.utils.SyncUtils
import moe.rukamori.archivetune.utils.dataStore
import moe.rukamori.archivetune.utils.get
import moe.rukamori.archivetune.utils.reportException
import java.time.ZoneOffset
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AutoPlaylistViewModel
    @Inject
    constructor(
        @ApplicationContext context: Context,
        database: MusicDatabase,
        downloadUtil: DownloadUtil,
        savedStateHandle: SavedStateHandle,
        private val syncUtils: SyncUtils,
    ) : ViewModel() {
        val playlist = savedStateHandle.get<String>("playlist")!!

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing = _isRefreshing.asStateFlow()

        private fun AutoPlaylistSongSortType.toSongSortType(): SongSortType =
            when (this) {
                AutoPlaylistSongSortType.CREATE_DATE -> SongSortType.CREATE_DATE
                AutoPlaylistSongSortType.NAME -> SongSortType.NAME
                AutoPlaylistSongSortType.ARTIST -> SongSortType.ARTIST
                AutoPlaylistSongSortType.PLAY_TIME -> SongSortType.PLAY_TIME
            }

        private data class PlaylistConfig(
            val sortDesc: Pair<AutoPlaylistSongSortType, Boolean>,
            val hideExplicit: Boolean,
            val hideVideo: Boolean,
            val treeUriString: String
        )

        private fun isFlacDownloaded(
            context: Context,
            title: String,
            artist: String,
            album: String,
            treeUriString: String
        ): Boolean {
            val fileName = "${moe.rukamori.archivetune.download.sanitizeFileName(title)}.flac"
            if (treeUriString.isNotEmpty()) {
                val treeUri = android.net.Uri.parse(treeUriString)
                val rootDir = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, treeUri) ?: return false
                val artistDir = rootDir.findFile(moe.rukamori.archivetune.download.sanitizeFileName(artist)) ?: return false
                val albumDir = artistDir.findFile(moe.rukamori.archivetune.download.sanitizeFileName(album)) ?: return false
                val file = albumDir.findFile(fileName)
                return file != null && file.exists()
            } else {
                val musicDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_MUSIC)
                val yumaDir = java.io.File(musicDir, "YumaPlayer")
                val artistDir = java.io.File(yumaDir, moe.rukamori.archivetune.download.sanitizeFileName(artist))
                val albumDir = java.io.File(artistDir, moe.rukamori.archivetune.download.sanitizeFileName(album))
                val file = java.io.File(albumDir, fileName)
                return file.exists()
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        val likedSongs =
            context.dataStore.data
                .map {
                    PlaylistConfig(
                        it[AutoPlaylistSongSortTypeKey].toEnum(AutoPlaylistSongSortType.CREATE_DATE) to (
                            it[AutoPlaylistSongSortDescendingKey]
                                ?: true
                        ),
                        it[HideExplicitKey] ?: false,
                        it[HideVideoKey] ?: false,
                        it[moe.rukamori.archivetune.constants.DownloadLocationUriKey] ?: ""
                    )
                }.distinctUntilChanged()
                .flatMapLatest { config ->
                    val (sortType, descending) = config.sortDesc
                    val songSortType = sortType.toSongSortType()
                    when (playlist) {
                        "liked" -> {
                            database.likedSongs(songSortType, descending, config.hideVideo).map { it.filterExplicit(config.hideExplicit) }
                        }

                        "downloaded" -> {
                            downloadUtil.downloads.flatMapLatest { downloads ->
                                database
                                    .allSongs()
                                    .flowOn(Dispatchers.IO)
                                    .map { songs ->
                                        songs.filter {
                                            downloads[it.id]?.state == Download.STATE_COMPLETED || (it.song.dateDownload != null && isFlacDownloaded(context, it.song.title, it.artists.mapNotNull { artist -> artist.name.takeIf(String::isNotBlank) }.joinToString(", "), it.song.albumName.orEmpty(), config.treeUriString))
                                        }
                                    }.map { songs ->
                                        when (songSortType) {
                                            SongSortType.CREATE_DATE -> {
                                                songs.sortedBy {
                                                    val updateTime = downloads[it.id]?.updateTimeMs ?: 0L
                                                    if (updateTime == 0L) it.song.dateDownload?.toInstant(ZoneOffset.UTC)?.toEpochMilli() ?: 0L else updateTime
                                                }
                                            }

                                            SongSortType.NAME -> {
                                                songs.sortedBy { it.song.title }
                                            }

                                            SongSortType.ARTIST -> {
                                                songs.sortedBy { song ->
                                                    song.artists.joinToString(separator = "") { artist -> artist.name }
                                                }
                                            }

                                            SongSortType.PLAY_TIME -> {
                                                songs.sortedBy { it.song.totalPlayTime }
                                            }
                                        }.reversed(descending).filterExplicit(config.hideExplicit)
                                    }
                            }
                        }

                        else -> {
                            MutableStateFlow(emptyList())
                        }
                    }
                }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        fun refresh() {
            if (_isRefreshing.value) return
            viewModelScope.launch(Dispatchers.IO) {
                _isRefreshing.value = true
                try {
                    when (playlist) {
                        "liked" -> syncUtils.syncLikedSongs()
                        else -> Unit
                    }
                } catch (e: Exception) {
                    reportException(e)
                } finally {
                    _isRefreshing.value = false
                }
            }
        }

        fun syncLikedSongs() {
            refresh()
        }
    }
