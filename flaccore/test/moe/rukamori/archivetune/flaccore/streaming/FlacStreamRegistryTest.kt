package moe.rukamori.archivetune.flaccore.streaming

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import moe.rukamori.archivetune.flaccore.model.TrackQuery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FlacStreamRegistryTest {

    private val query = TrackQuery(artist = "Artist", title = "Title", durationMs = 1000)
    private val quality = 27

    @Test
    fun `qbdlx is called first and stops chain on success`() = runTest {
        val expected = FlacStreamUrl("url", 0, origin = "qbdlx")
        var arcodCalled = false

        val registry = FlacStreamRegistry(
            qbdlx = { _, _ -> expected },
            arcod = { _, _ ->
                arcodCalled = true
                null
            }
        )

        val result = registry.resolve(query, quality)
        assertEquals(expected, result)
        assertEquals(false, arcodCalled)
    }

    @Test
    fun `arcod is called if qbdlx returns null`() = runTest {
        val expected = FlacStreamUrl("url", 0, origin = "arcod")

        val registry = FlacStreamRegistry(
            qbdlx = { _, _ -> null },
            arcod = { _, _ -> expected }
        )

        val result = registry.resolve(query, quality)
        assertEquals(expected, result)
    }

    @Test
    fun `returns null if both return null`() = runTest {
        val registry = FlacStreamRegistry(
            qbdlx = { _, _ -> null },
            arcod = { _, _ -> null }
        )

        val result = registry.resolve(query, quality)
        assertNull(result)
    }

    @Test
    fun `arcod is called if qbdlx times out`() = runTest {
        val expected = FlacStreamUrl("url", 0, origin = "arcod")

        val registry = FlacStreamRegistry(
            qbdlx = { _, _ ->
                delay(200)
                FlacStreamUrl("qbdlx", 0, origin = "qbdlx")
            },
            arcod = { _, _ -> expected }
        ).apply { timeoutMs = 100L }

        val result = registry.resolve(query, quality)
        assertEquals(expected, result)
    }

    @Test(expected = CancellationException::class)
    fun `CancellationException is propagated`() = runTest {
        val registry = FlacStreamRegistry(
            qbdlx = { _, _ -> throw CancellationException("test") },
            arcod = { _, _ -> null }
        )

        registry.resolve(query, quality)
    }
}
