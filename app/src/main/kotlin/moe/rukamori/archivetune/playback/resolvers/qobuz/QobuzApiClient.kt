package moe.rukamori.archivetune.playback.resolvers.qobuz

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.MessageDigest

@Serializable
data class QobuzSearchResponse(val tracks: QobuzTrackList)

@Serializable
data class QobuzTrackList(val items: List<QobuzTrack>)

@Serializable
data class QobuzTrack(
    val id: Long,
    val title: String,
    val performer: QobuzPerformer? = null,
    val duration: Int,
    val streamable: Boolean,
    val maximum_sampling_rate: Float = 44.1f,
    val maximum_bit_depth: Int = 16,
    val album: QobuzAlbum? = null
)

@Serializable
data class QobuzPerformer(val name: String)

@Serializable
data class QobuzAlbum(val image: QobuzImage? = null)

@Serializable
data class QobuzImage(val large: String? = null, val thumbnail: String? = null, val small: String? = null)

@Serializable
data class QobuzFileUrl(
    val url: String? = null,
    val format_id: Int = 0,
    val sample: Boolean = false,
    val sampling_rate: Float = 44.1f,
    val bit_depth: Int = 16
)

class QobuzApiClient(
    private val httpClient: OkHttpClient,
    private val appId: String,
    private val appSecret: String,
    private val userAuthToken: String
) {
    private val baseUrl = "https://www.qobuz.com"
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    suspend fun search(query: String, limit: Int = 10): List<QobuzTrack> = withContext(Dispatchers.IO) {
        val url = "$baseUrl/api.json/0.2/catalog/search".toHttpUrl().newBuilder()
            .addQueryParameter("query", query)
            .addQueryParameter("type", "tracks")
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("app_id", appId)
            .build()
        
        val req = Request.Builder()
            .url(url)
            .header("X-App-Id", appId)
            .header("X-User-Auth-Token", userAuthToken)
            .get()
            .build()
            
        httpClient.newCall(req).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) return@withContext emptyList()
            runCatching { json.decodeFromString<QobuzSearchResponse>(body).tracks.items }.getOrDefault(emptyList())
        }
    }

    suspend fun getFileUrl(trackId: Long, formatId: Int = 27): QobuzFileUrl? = withContext(Dispatchers.IO) {
        val ts = System.currentTimeMillis() / 1000L
        val sig = md5("trackgetFileUrl" + "format_id$formatId" + "intentstream" + "track_id$trackId" + ts + appSecret)
        
        val url = "$baseUrl/api.json/0.2/track/getFileUrl".toHttpUrl().newBuilder()
            .addQueryParameter("track_id", trackId.toString())
            .addQueryParameter("format_id", formatId.toString())
            .addQueryParameter("app_id", appId)
            .addQueryParameter("request_ts", ts.toString())
            .addQueryParameter("request_sig", sig)
            .addQueryParameter("intent", "stream")
            .build()
            
        val req = Request.Builder()
            .url(url)
            .header("X-App-Id", appId)
            .header("X-User-Auth-Token", userAuthToken)
            .get()
            .build()
            
        httpClient.newCall(req).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) return@withContext null
            runCatching { json.decodeFromString<QobuzFileUrl>(body) }.getOrNull()
        }
    }

    private fun md5(s: String): String =
        MessageDigest.getInstance("MD5").digest(s.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
