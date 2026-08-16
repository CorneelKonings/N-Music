package moe.rukamori.archivetune.playback.resolvers.qobuz

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

class SquidApiClient(
    private val httpClient: OkHttpClient,
    private val captchaCookie: String
) {
    private val baseUrl = "https://qobuz.squid.wtf/api"
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    suspend fun search(query: String, offset: Int = 0): KennyySearchData? = withContext(Dispatchers.IO) {
        val url = "$baseUrl/get-music".toHttpUrl().newBuilder()
            .addQueryParameter("q", query)
            .addQueryParameter("offset", offset.toString())
            .build()

        val req = Request.Builder()
            .url(url)
            .header("User-Agent", "Stash-Android/1.0")
            .header("Cookie", "captcha_verified_at=$captchaCookie")
            .get()
            .build()

        httpClient.newCall(req).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) return@withContext null
            runCatching { json.decodeFromString<SquidWtfEnvelope<KennyySearchData>>(body).data }.getOrNull()
        }
    }

    suspend fun getFileUrl(trackId: Long, quality: Int = 27): KennyyDownloadData? = withContext(Dispatchers.IO) {
        val url = "$baseUrl/download-music".toHttpUrl().newBuilder()
            .addQueryParameter("track_id", trackId.toString())
            .addQueryParameter("quality", quality.toString())
            .build()

        val req = Request.Builder()
            .url(url)
            .header("User-Agent", "Stash-Android/1.0")
            .header("Cookie", "captcha_verified_at=$captchaCookie")
            .get()
            .build()

        httpClient.newCall(req).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) return@withContext null
            runCatching { json.decodeFromString<SquidWtfEnvelope<KennyyDownloadData>>(body).data }.getOrNull()
        }
    }
}
