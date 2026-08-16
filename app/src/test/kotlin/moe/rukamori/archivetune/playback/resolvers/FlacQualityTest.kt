package moe.rukamori.archivetune.playback.resolvers

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import moe.rukamori.archivetune.constants.FlacQuality
import moe.rukamori.archivetune.db.entities.ArtistEntity
import moe.rukamori.archivetune.db.entities.Song
import moe.rukamori.archivetune.db.entities.SongEntity
import moe.rukamori.archivetune.playback.resolvers.qobuz.SquidApiClient
import moe.rukamori.archivetune.playback.resolvers.qobuz.KennyyDownloadData
import moe.rukamori.archivetune.playback.resolvers.qobuz.KennyySearchData
import moe.rukamori.archivetune.playback.resolvers.qobuz.QobuzTrackList
import moe.rukamori.archivetune.playback.resolvers.qobuz.QobuzTrack
import org.junit.Assert.assertEquals
import org.junit.Test

class FlacQualityTest {

    @Test
    fun `SquidStreamResolver passes correct quality to apiClient`() = runBlocking {
        val mockApiClient = mockk<SquidApiClient>()
        val resolver = SquidStreamResolver(mockApiClient)

        val song = Song(
            song = SongEntity(id = "1", title = "Test", albumName = "Album"),
            artists = listOf(ArtistEntity(id = "1", name = "Artist"))
        )

        val searchResult = KennyySearchData(
            tracks = QobuzTrackList(
                items = listOf(
                    QobuzTrack(
                        id = 123,
                        title = "Test",
                        duration = 180,
                        streamable = true,
                        maximum_bit_depth = 16,
                        maximum_sampling_rate = 44.1f,
                        album = null
                    )
                )
            )
        )

        coEvery { mockApiClient.search("Artist Test") } returns searchResult
        
        coEvery { mockApiClient.getFileUrl(123, 6) } returns KennyyDownloadData(url = "http://test.com/cd?etsp=1234567890")
        coEvery { mockApiClient.getFileUrl(123, 7) } returns KennyyDownloadData(url = "http://test.com/hires?etsp=1234567890")
        coEvery { mockApiClient.getFileUrl(123, 27) } returns KennyyDownloadData(url = "http://test.com/max?etsp=1234567890")

        val cdResult = resolver.resolve(song, FlacQuality.CD)
        assertEquals("http://test.com/cd?etsp=1234567890", cdResult?.url)

        val hiresResult = resolver.resolve(song, FlacQuality.HI_RES)
        assertEquals("http://test.com/hires?etsp=1234567890", hiresResult?.url)

        val maxResult = resolver.resolve(song, FlacQuality.MAX)
        assertEquals("http://test.com/max?etsp=1234567890", maxResult?.url)
    }
}
