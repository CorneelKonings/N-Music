package moe.rukamori.archivetune.playback.resolvers

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import moe.rukamori.archivetune.constants.FlacQuality
import moe.rukamori.archivetune.db.entities.Song
import moe.rukamori.archivetune.db.entities.SongEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class StreamSourceRegistryTimeoutTest {

    @Test
    fun `registry continues to next provider when one times out`() = runBlocking {
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

        // kennyy hangs
        coEvery { kennyyResolver.resolve(any(), any()) } coAnswers {
            delay(10_000)
            null
        }

        // squid returns valid result
        coEvery { squidResolver.resolve(any(), any()) } returns expectedUrl

        // arcod and qbdlx return null
        coEvery { arcodResolver.resolve(any(), any()) } returns null
        coEvery { qbdlxResolver.resolve(any(), any()) } returns null

        val result = registry.resolve(song, quality)

        assertEquals(expectedUrl, result)
    }
}