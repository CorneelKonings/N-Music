package moe.rukamori.archivetune.playback.resolvers

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import moe.rukamori.archivetune.flaccore.streaming.FlacStreamRegistry
import moe.rukamori.archivetune.flaccore.streaming.FlacStreamUrl
import moe.rukamori.archivetune.flaccore.model.TrackQuery
import org.junit.Assert.assertEquals
import org.junit.Test

class StreamSourceRegistryTimeoutTest {

    @Test
    fun `registry continues to next provider when one times out`() = runBlocking {
        val expectedUrl = FlacStreamUrl(
            url = "http://arcod.test",
            expiresAtMs = 0L,
            origin = "arcod"
        )

        val registry = FlacStreamRegistry(
            qbdlx = { _, _ -> 
                delay(40_000) // FlacStreamRegistry has 35s timeout
                null 
            },
            arcod = { _, _ -> 
                expectedUrl 
            }
        )

        val query = TrackQuery(artist = "A", title = "T", durationMs = 1000L)
        val result = registry.resolve(query, 7)

        assertEquals(expectedUrl, result)
    }
}
