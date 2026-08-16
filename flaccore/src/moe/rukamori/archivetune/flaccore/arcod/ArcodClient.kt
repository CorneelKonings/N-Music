package moe.rukamori.archivetune.flaccore.arcod

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import moe.rukamori.archivetune.flaccore.FlacConfig
import moe.rukamori.archivetune.flaccore.FlacLogger
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder

/*
 * Ported from Stash (GPL-3.0)
 * Original source: com.stash.data.download.lossless.arcod.ArcodClient.kt
 */

/**
 * HTTP client for the ARCOD (arcod.xyz) Qobuz-DL proxy.
 */
class ArcodClient(
    private val config: FlacConfig,
    sharedClient: OkHttpClient,
) {
    private val httpClient: OkHttpClient = sharedClient
    private val logger = FlacLogger(TAG)

    /** Test seam: tests point this at a MockWebServer URL. */
    internal var baseUrl = "https://arcod.xyz/api"

    /**
     * Base for the operator's `/v2/stash/…` routes.
     * Test seam like the others.
     */
    internal var stashBaseUrl: String = ""

    /** True when this build carries the operator's private integration key. */
    suspend fun isConfigured(): Boolean = config.arcodStashKey().isNotBlank()

    /**
     * Search the proxied Qobuz catalog. Non-2xx (other than 429) or a parse
     * failure yields an empty list so the caller cleanly fails over.
     */
    suspend fun search(query: String): List<ArcodTrackItem> = withContext(Dispatchers.IO) {
        if (stashBaseUrl.isEmpty()) {
            stashBaseUrl = config.arcodApiBase().trimEnd('/') + "/v2/stash"
        }
        val encoded = URLEncoder.encode(query, "UTF-8")
        val request = arcodRequest("$stashBaseUrl/search?q=$encoded&limit=$SEARCH_LIMIT&offset=0").get().build()
        try {
            httpClient.newCall(request).execute().use { response ->
                if (response.code == 429) throw ArcodRateLimitedException()
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body.string()
                val parsed = ArcodJson.decodeFromString<ArcodSearchResponse>(body)
                parsed.data?.tracks?.items ?: emptyList()
            }
        } catch (e: ArcodRateLimitedException) {
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Resolve a single playable stream URL for a Qobuz [trackId] at the given
     * Qobuz [quality] format_id (6=CD, 7=hi-res, 27=max). One GET — no job
     * render/poll. 429 throws [ArcodRateLimitedException]; any other failure
     * (non-2xx or unparseable body) returns null so the caller fails over.
     */
    suspend fun streamUrl(trackId: Long, quality: Int): ArcodStreamResult? =
        withContext(Dispatchers.IO) {
            if (!isConfigured()) {
                logger.d("stash key not configured — skipping arcod stream")
                return@withContext null
            }
            if (stashBaseUrl.isEmpty()) {
                stashBaseUrl = config.arcodApiBase().trimEnd('/') + "/v2/stash"
            }
            val request = arcodRequest("$stashBaseUrl/stream/$trackId?quality=$quality")
                .get().build()
            try {
                httpClient.newCall(request).execute().use { response ->
                    if (response.code == 429) throw ArcodRateLimitedException()
                    if (!response.isSuccessful) return@withContext null
                    val body = response.body.string()
                    parseStreamResult(body) ?: run {
                        logger.d("stream parse miss trackId=$trackId body='${body.take(200)}'")
                        null
                    }
                }
            } catch (e: ArcodRateLimitedException) {
                throw e
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
        }

    /**
     * Tolerant parse of the stream GET body. Accepts (a) a bare URL as the whole
     * body, or (b) JSON with the URL at the top level OR one level under `data`
     * (arcod's usual `{success,data:{…}}` envelope), in any of the `url` /
     * `streamUrl` / `downloadUrl` keys, plus an optional `expiresIn`.
     */
    internal fun parseStreamResult(body: String): ArcodStreamResult? {
        val trimmed = body.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return ArcodStreamResult(url = trimmed)
        }
        return try {
            val root = ArcodJson.parseToJsonElement(trimmed).jsonObject
            val obj = (root["data"] as? JsonObject) ?: root
            val url = (obj["url"] ?: obj["streamUrl"] ?: obj["downloadUrl"])
                ?.jsonPrimitive?.contentOrNull ?: return null
            val expiresIn = (obj["expiresIn"] ?: root["expiresIn"])
                ?.jsonPrimitive?.let { it.intOrNull ?: it.contentOrNull?.toIntOrNull() }
            ArcodStreamResult(url = url, expiresInSec = expiresIn)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Browser-y headers the arcod web app sends.
     *
     * `X-Stash-Key` is the operator's per-build integration key for the `/v2/stash`
     * routes. Without it those routes answer 403; with it (and no Bearer) 401.
     */
    private suspend fun arcodRequest(url: String): Request.Builder =
        Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Origin", "https://arcod.xyz")
            .header("Referer", "https://arcod.xyz/")
            .apply {
                val stashKey = config.arcodStashKey()
                if (stashKey.isNotBlank()) {
                    header("X-Stash-Key", stashKey)
                }
                val bearer = config.arcodBearerToken()
                if (bearer.isNotBlank()) {
                    header("Authorization", "Bearer $bearer")
                }
            }

    private companion object {
        const val TAG = "ArcodClient"
        /** Catalog page size for search — enough candidates for the matcher to score. */
        const val SEARCH_LIMIT = 12
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
    }
}

/**
 * Thrown when an ARCOD endpoint returns HTTP 429. Kept distinct so the
 * lossless rate limiter can back off ARCOD specifically rather than treating
 * it as a generic source failure.
 */
class ArcodRateLimitedException : RuntimeException("arcod rate limited (HTTP 429)")
