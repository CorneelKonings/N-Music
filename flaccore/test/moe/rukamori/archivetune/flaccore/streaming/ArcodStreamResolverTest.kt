package moe.rukamori.archivetune.flaccore.streaming

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import moe.rukamori.archivetune.flaccore.arcod.ArcodClient
import moe.rukamori.archivetune.flaccore.arcod.ArcodStreamResult
import moe.rukamori.archivetune.flaccore.arcod.ArcodTrackItem
import moe.rukamori.archivetune.flaccore.model.TrackQuery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArcodStreamResolverTest {

    private val client = mockk<ArcodClient>()
    private val resolver = ArcodStreamResolver(client)
    private val query = TrackQuery(artist = "Artist", title = "Title", durationMs = 1000)
    private val quality = 27

    @Test
    fun `returns null if search returns no match`() = runTest {
        coEvery { client.search("Artist Title") } returns emptyList()

        val result = resolver.resolve(query, quality)
        assertNull(result)
    }

    @Test
    fun `returns null if streamUrl returns null`() = runTest {
        val item = ArcodTrackItem(
            id = 123,
            title = "Title",
            performer = moe.rukamori.archivetune.flaccore.arcod.ArcodNamed("Artist"),
            duration = 1,
            maxBitDepth = 16,
            maxSamplingRate = 44.1,
            album = null
        )
        coEvery { client.search("Artist Title") } returns listOf(item)
        coEvery { client.streamUrl(123, quality) } returns null

        val result = resolver.resolve(query, quality)
        assertNull(result)
    }

    @Test
    fun `maps fields correctly with default TTL when expiresInSec is null`() = runTest {
        val item = ArcodTrackItem(
            id = 123,
            title = "Title",
            performer = moe.rukamori.archivetune.flaccore.arcod.ArcodNamed("Artist"),
            duration = 1,
            maxBitDepth = 24,
            maxSamplingRate = 96.0,
            album = null
        )
        coEvery { client.search("Artist Title") } returns listOf(item)
        coEvery { client.streamUrl(123, quality) } returns ArcodStreamResult(
            url = "https://example.com/stream.flac",
            expiresInSec = null
        )

        val before = System.currentTimeMillis()
        val result = resolver.resolve(query, quality)
        val after = System.currentTimeMillis()

        assertEquals("https://example.com/stream.flac", result?.url)
        assertEquals("flac", result?.codec)
        assertEquals(24, result?.bitsPerSample)
        assertEquals(96000, result?.sampleRateHz)
        assertEquals("arcod", result?.origin)
        assertNull(result?.coverArtUrl)
        
        val expectedTtl = 280_000L
        assertTrue(result!!.expiresAtMs >= before + expectedTtl)
        assertTrue(result.expiresAtMs <= after + expectedTtl)
    }

    @Test
    fun `maps fields correctly with calculated TTL when expiresInSec is provided`() = runTest {
        val item = ArcodTrackItem(
            id = 123,
            title = "Title",
            performer = moe.rukamori.archivetune.flaccore.arcod.ArcodNamed("Artist"),
            duration = 1,
            maxBitDepth = 16,
            maxSamplingRate = 44.1,
            album = null
        )
        coEvery { client.search("Artist Title") } returns listOf(item)
        coEvery { client.streamUrl(123, quality) } returns ArcodStreamResult(
            url = "https://example.com/stream.flac",
            expiresInSec = 300 // 5 minutes
        )

        val before = System.currentTimeMillis()
        val result = resolver.resolve(query, quality)
        val after = System.currentTimeMillis()

        val expectedTtl = (300L * 1000L - 20_000L).coerceAtLeast(5_000L)
        assertTrue(result!!.expiresAtMs >= before + expectedTtl)
        assertTrue(result.expiresAtMs <= after + expectedTtl)
    }
}
