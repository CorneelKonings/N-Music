package moe.rukamori.archivetune.playback.resolvers

import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import moe.rukamori.archivetune.constants.FlacQuality
import moe.rukamori.archivetune.db.entities.Song
import moe.rukamori.archivetune.db.entities.SongEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class StreamSourceRegistryOrderTest {

    @Test
    fun `registry resolves in correct order qbdlx arcod kennyy squid`() = runBlocking {
        val kennyyResolver = mockk<KennyyStreamResolver>()
        val squidResolver = mockk<SquidStreamResolver>()
        val arcodResolver = mockk<ArcodStreamResolver>()
        val qbdlxResolver = mockk<QbdlxStreamResolver>()

        val registry = StreamSourceRegistry(
            kennyyResolver,
            squidResolver,
            arcodResolver,
            qbdlxResolver
        )

        val song = Song(
            song = SongEntity(id = "1", title = "T"),
            artists = emptyList()
        )
        val quality = FlacQuality.HI_RES

        val expectedUrl = StreamUrl(
            url = "http://squid.test",
            expiresAtMs = 0L,
            origin = "squid"
        )

        coEvery { qbdlxResolver.resolve(any(), any()) } returns null
        coEvery { arcodResolver.resolve(any(), any()) } returns null
        coEvery { kennyyResolver.resolve(any(), any()) } returns null
        coEvery { squidResolver.resolve(any(), any()) } returns expectedUrl

        val result = registry.resolve(song, quality)

        assertEquals(expectedUrl, result)

        coVerifyOrder {
            qbdlxResolver.resolve(any(), any())
            arcodResolver.resolve(any(), any())
            kennyyResolver.resolve(any(), any())
            squidResolver.resolve(any(), any())
        }
    }
}
