package moe.rukamori.archivetune.flaccore.arcod

import kotlinx.coroutines.test.runTest
import moe.rukamori.archivetune.flaccore.FlacConfig
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/*
 * Ported from Stash (GPL-3.0)
 * Original source: com.stash.data.download.lossless.arcod.ArcodClientTest.kt
 */

class ArcodClientTest {

    private lateinit var server: MockWebServer
    private lateinit var client: ArcodClient
    private lateinit var fakeConfig: FakeFlacConfig

    class FakeFlacConfig : FlacConfig {
        var stashKey = "test-stash-key"
        var bearerToken = ""
        var apiBase = "https://arcod.xyz/api"

        override suspend fun qbdlxEnabled(): Boolean = false
        override suspend fun qbdlxAppId(): String = ""
        override suspend fun qbdlxAppSecret(): String = ""
        override suspend fun qbdlxTokenPool(): String = ""
        override suspend fun arcodApiBase(): String = apiBase
        override suspend fun arcodStashKey(): String = stashKey
        override suspend fun arcodBearerToken(): String = bearerToken
    }

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        fakeConfig = FakeFlacConfig()
        client = ArcodClient(
            config = fakeConfig,
            sharedClient = OkHttpClient(),
        ).apply {
            baseUrl = server.url("/api").toString().trimEnd('/')
            // The /v2/stash routes issued to Stash: search + stream both live here.
            stashBaseUrl = server.url("/v2/stash").toString().trimEnd('/')
        }
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `search returns parsed items`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(SEARCH_BODY))

        val items = client.search("Ja Rule Murderers")

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path!!.startsWith("/v2/stash/search"))
        assertTrue(request.path!!.contains("q="))
        assertTrue(request.path!!.contains("offset=0"))

        assertEquals(1, items.size)
        assertEquals(8767428L, items[0].id)
        assertEquals("0093624804567", items[0].album?.id)
        assertEquals("USAT20300456", items[0].isrc)
    }

    @Test
    fun `search returns empty on non-2xx`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("boom"))

        assertTrue(client.search("x").isEmpty())
    }

    @Test
    fun `streamUrl appends trackId and quality to the stash stream route and parses plain-text url`() = runTest {
        val url = "https://dl.arcod.xyz/stream/abc.flac?token=xyz"
        server.enqueue(MockResponse().setResponseCode(200).setBody(url))

        val result = client.streamUrl(8767428L, 27)

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/v2/stash/stream/8767428?quality=27", request.path)
        assertNotNull(result)
        assertEquals(url, result!!.url)
        assertNull(result.expiresInSec)
    }

    @Test
    fun `stash requests carry the X-Stash-Key header when configured`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(SEARCH_BODY))
        client.search("Ja Rule Murderers")
        val sent = server.takeRequest().getHeader("X-Stash-Key")
        assertEquals("test-stash-key", sent)
    }

    @Test
    fun `streamUrl parses flat json url`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{"url":"https://dl.arcod.xyz/s/x.flac"}"""),
        )

        val result = client.streamUrl(1L, 6)

        assertEquals("https://dl.arcod.xyz/s/x.flac", result!!.url)
        assertNull(result.expiresInSec)
    }

    @Test
    fun `streamUrl parses enveloped json with expiresIn`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{"success":true,"data":{"url":"https://dl.arcod.xyz/s/y.flac","expiresIn":120}}"""),
        )

        val result = client.streamUrl(1L, 7)

        assertEquals("https://dl.arcod.xyz/s/y.flac", result!!.url)
        assertEquals(120, result.expiresInSec)
    }

    @Test
    fun `streamUrl parses enveloped json with string expiresIn`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{"success":true,"data":{"url":"https://dl.arcod.xyz/s/z.flac","expiresIn":"120"}}"""),
        )

        val result = client.streamUrl(1L, 27)

        assertEquals("https://dl.arcod.xyz/s/z.flac", result!!.url)
        assertEquals(120, result.expiresInSec)
    }

    @Test
    fun `streamUrl throws on 429`() = runTest {
        server.enqueue(MockResponse().setResponseCode(429).setBody("rate limited"))
        try {
            client.streamUrl(1L, 27)
            fail("expected ArcodRateLimitedException")
        } catch (_: ArcodRateLimitedException) {
            // expected
        }
    }

    @Test
    fun `streamUrl returns null on non-2xx`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("boom"))
        assertNull(client.streamUrl(1L, 27))
    }

    @Test
    fun `streamUrl returns null on unparseable body`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("not a url or json {"))
        assertNull(client.streamUrl(1L, 27))
    }

    private companion object {
        val SEARCH_BODY = """
            {
              "success": true,
              "data": {
                "tracks": {
                  "items": [
                    {
                      "id": 8767428,
                      "title": "Murderers (Album Version)",
                      "isrc": "USAT20300456",
                      "duration": 243,
                      "maximum_bit_depth": 24,
                      "performer": { "name": "Ja Rule", "id": 12345 },
                      "album": { "id": "0093624804567", "title": "Blood In My Eye" }
                    }
                  ]
                }
              }
            }
        """.trimIndent()
    }
}
