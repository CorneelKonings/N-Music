package moe.rukamori.archivetune.ui

import android.app.Application
import android.util.LruCache
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import io.mockk.spyk
import io.mockk.verify
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import moe.rukamori.archivetune.db.MusicDatabase
import moe.rukamori.archivetune.db.entities.LyricsEntity
import moe.rukamori.archivetune.lyrics.LyricsHelper
import moe.rukamori.archivetune.playback.PlayerConnection
import moe.rukamori.archivetune.playback.PlayerConnectionHolder
import moe.rukamori.archivetune.data.repository.SettingsRepository
import moe.rukamori.archivetune.ui.state.PlayerUiState
import moe.rukamori.archivetune.ui.player.player_0.buttons.PlayerAction
import moe.rukamori.archivetune.models.MediaMetadata
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Field

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelHandleActionTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: PlayerViewModel
    private lateinit var lyricsHelper: LyricsHelper
    private lateinit var database: MusicDatabase
    private lateinit var connectionHolder: PlayerConnectionHolder

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Color::class)
        every { Color.parseColor(any()) } returns 0
        
        mockkConstructor(LruCache::class)
        every { anyConstructed<LruCache<Any, Any>>().get(any()) } returns null
        every { anyConstructed<LruCache<Any, Any>>().put(any(), any()) } returns null

        val application = mockk<Application>(relaxed = true)
        lyricsHelper = mockk(relaxed = true)
        database = mockk(relaxed = true)
        
        val playerConnection = mockk<PlayerConnection>(relaxed = true)
        every { playerConnection.database } returns database
        
        val metadataFlow = MutableStateFlow<MediaMetadata?>(
            MediaMetadata(
                id = "test_track_url",
                title = "Title",
                artists = listOf(MediaMetadata.Artist(id = null, name = "Artist")),
                duration = 100,
                album = null
            )
        )
        every { playerConnection.mediaMetadata } returns metadataFlow
        
        connectionHolder = PlayerConnectionHolder()
        connectionHolder.connection.value = playerConnection

        val settingsRepository = mockk<SettingsRepository>(relaxed = true)
        every { settingsRepository.isBlurBackgroundEnabled() } returns false
        every { settingsRepository.isAutoDownloadLyricsEnabled() } returns false
        every { settingsRepository.isImmersiveEnabled() } returns false
        every { settingsRepository.isShowCodecInfoEnabled() } returns false
        every { settingsRepository.isAlbumCoverGlowEnabled() } returns false

        viewModel = spyk(PlayerViewModel(
            application = application,
            connectionHolder = connectionHolder,
            settingsRepository = settingsRepository,
            lyricsHelper = lyricsHelper
        ), recordPrivateCalls = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Color::class)
        unmockkConstructor(LruCache::class)
    }

    private fun setUiState(state: PlayerUiState) {
        val field: Field = PlayerViewModel::class.java.getDeclaredField("_uiState")
        field.isAccessible = true
        val stateFlow = field.get(viewModel) as MutableStateFlow<PlayerUiState>
        stateFlow.value = state
    }

    @Test
    fun `handleAction routes lyrics actions correctly`() = runTest {
        // Arrange
        val trackUrl = "test_track_url"
        setUiState(PlayerUiState(trackUrl = trackUrl, title = "Title", artist = "Artist"))

        // Act & Assert: SearchLyrics
        viewModel.handleAction(PlayerAction.SearchLyrics)
        verify { viewModel.refreshLyrics() }

        // Act & Assert: ForceRefresh
        viewModel.handleAction(PlayerAction.ForceRefresh)
        verify(exactly = 2) { viewModel.refreshLyrics() }

        // Act & Assert: SetLyricsSyncOffset
        viewModel.handleAction(PlayerAction.SetLyricsSyncOffset(500))
        assertEquals(500, viewModel.uiState.value.lyricsSyncOffset)

        // Act & Assert: PrepareLyricsEdit
        viewModel.handleAction(PlayerAction.PrepareLyricsEdit)
        verify { viewModel["prepareLyricsEditText"]() }

        // Act & Assert: SaveLyrics
        viewModel.handleAction(PlayerAction.SaveLyrics("new lyrics"))
        verify { viewModel["saveLyrics"]("new lyrics") }

        // Act & Assert: TranslateLyrics
        viewModel.handleAction(PlayerAction.TranslateLyrics("RU", false))
        verify { viewModel["translateLyrics"]("RU", false) }
    }
}
