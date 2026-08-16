package moe.rukamori.archivetune.playback.resolvers

import kotlinx.coroutines.runBlocking
import moe.rukamori.archivetune.flaccore.streaming.FlacStreamRegistry
import moe.rukamori.archivetune.flaccore.streaming.FlacStreamUrl
import moe.rukamori.archivetune.flaccore.model.TrackQuery
import org.junit.Assert.assertEquals
import org.junit.Test

class StreamSourceRegistryOrderTest {

    @Test
    fun `registry resolves in correct order qbdlx arcod`() = runBlocking {
        val order = mutableListOf<String>()
        
        val expectedUrl = FlacStreamUrl(
            url = "http://arcod.test",
            expiresAtMs = 0L,
            origin = "arcod"
        )

        val registry = FlacStreamRegistry(
            qbdlx = { _, _ -> 
                order.add("qbdlx")
                null 
            },
            arcod = { _, _ -> 
                order.add("arcod")
                expectedUrl 
            }
        )

        val query = TrackQuery(artist = "A", title = "T", durationMs = 1000L)
        val result = registry.resolve(query, 7)

        assertEquals(expectedUrl, result)
        assertEquals(listOf("qbdlx", "arcod"), order)
    }
}
