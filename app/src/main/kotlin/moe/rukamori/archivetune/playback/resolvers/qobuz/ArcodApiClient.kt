package moe.rukamori.archivetune.playback.resolvers.qobuz

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

@Serializable
data class ArcodStreamResult(
    val url: String,
    val expiresInSec: Int? = null
)

class ArcodApiClient(
    private val httpClient: OkHttpClient,
    private val stashKey: String,
    private val bearerToken: String
) {
    private val stashBaseUrl = "https://arcod.xyz/api/v2/stash"
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    suspend fun search(query: String): KennyySearchData? = withContext(Dispatchers.IO) {
        val url = "$stashBaseUrl/search".toHttpUrl().newBuilder()
            .addQueryParameter("q", query)
            .addQueryParameter("limit", "12")
            .addQueryParameter("offset", "0")
            .build()

        val req = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
            .header("Origin", "https://arcod.xyz")
            .header("Referer", "https://arcod.xyz/")
            .header("X-Stash-Key", stashKey)
            .header("Authorization", "Bearer $bearerToken")
            .get()
            .build()

        httpClient.newCall(req).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) return@withContext null
            runCatching { json.decodeFromString<SquidWtfEnvelope<KennyySearchData>>(body).data }.getOrNull()
        }
    }

    suspend fun streamUrl(trackId: Long, quality: Int = 27): ArcodStreamResult? = withContext(Dispatchers.IO) {
        val url = "$stashBaseUrl/stream/$trackId".toHttpUrl().newBuilder()
            .addQueryParameter("quality", quality.toString())
            .build()

        val req = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
            .header("Origin", "https://arcod.xyz")
            .header("Referer", "https://arcod.xyz/")
            .header("X-Stash-Key", stashKey)
            .header("Authorization", "Bearer $bearerToken")
            .get()
            .build()

        httpClient.newCall(req).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) return@withContext null
            
            val trimmed = body.trim()
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                return@withContext ArcodStreamResult(url = trimmed)
            }
            
            runCatching { 
                val root = json.parseToJsonElement(trimmed) as kotlinx.serialization.json.JsonObject
                val obj = (root["data"] as? kotlinx.serialization.json.JsonObject) ?: root
                val urlStr = (obj["url"] ?: obj["streamUrl"] ?: obj["downloadUrl"])?.let { 
                    (it as kotlinx.serialization.json.JsonPrimitive).content 
                } ?: return@withContext null
                
                val expiresIn = (obj["expiresIn"] ?: root["expiresIn"])?.let {
                    val prim = it as kotlinx.serialization.json.JsonPrimitive
                    prim.content.toIntOrNull()
                }
                
                ArcodStreamResult(url = urlStr, expiresInSec = expiresIn)
            }.getOrNull()
        }
    }
}
