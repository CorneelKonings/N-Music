package moe.rukamori.archivetune.playback.resolvers

import moe.rukamori.archivetune.db.entities.Song
import moe.rukamori.archivetune.playback.resolvers.qobuz.ArcodApiClient
import moe.rukamori.archivetune.playback.resolvers.qobuz.KennyyApiClient
import moe.rukamori.archivetune.playback.resolvers.qobuz.QobuzApiClient
import moe.rukamori.archivetune.playback.resolvers.qobuz.SquidApiClient
import javax.inject.Inject
import javax.inject.Singleton

data class StreamUrl(
    val url: String,
    val expiresAtMs: Long,
    val codec: String? = null,
    val bitsPerSample: Int? = null,
    val sampleRateHz: Int? = null,
    val bitrateKbps: Int? = null,
    val coverArtUrl: String? = null,
    val origin: String
)

interface LosslessStreamResolver {
    suspend fun resolve(song: Song): StreamUrl?
}

@Singleton
class KennyyStreamResolver @Inject constructor(
    private val apiClient: KennyyApiClient
) : LosslessStreamResolver {
    override suspend fun resolve(song: Song): StreamUrl? {
        val query = "${song.artists.firstOrNull()?.name ?: ""} ${song.title}".trim()
        val searchResult = apiClient.search(query)
        val track = searchResult?.tracks?.items?.firstOrNull { it.streamable } ?: return null
        
        val downloadResult = apiClient.getFileUrl(track.id, 27) // 27 = FLAC Hi-Res
        val url = downloadResult?.url ?: return null
        
        val etspMs = parseEtspMs(url) ?: (System.currentTimeMillis() + 3600_000L)
        
        return StreamUrl(
            url = url,
            expiresAtMs = etspMs,
            codec = "flac",
            bitsPerSample = track.maximum_bit_depth,
            sampleRateHz = (track.maximum_sampling_rate * 1000).toInt(),
            coverArtUrl = track.album?.image?.large ?: track.album?.image?.thumbnail,
            origin = "kennyy"
        )
    }
    
    private fun parseEtspMs(url: String): Long? {
        val match = Regex("""[?&]etsp=(\d+)""").find(url) ?: return null
        val secs = match.groupValues[1].toLongOrNull() ?: return null
        return secs * 1000L
    }
}

@Singleton
class QbdlxStreamResolver @Inject constructor(
    private val apiClient: QobuzApiClient
) : LosslessStreamResolver {
    override suspend fun resolve(song: Song): StreamUrl? {
        val query = "${song.artists.firstOrNull()?.name ?: ""} ${song.title}".trim()
        val searchResult = apiClient.search(query)
        val track = searchResult.firstOrNull { it.streamable } ?: return null
        
        val downloadResult = apiClient.getFileUrl(track.id, 27) // 27 = FLAC Hi-Res
        val url = downloadResult?.url ?: return null
        
        val etspMs = parseEtspMs(url) ?: (System.currentTimeMillis() + 3600_000L)
        
        return StreamUrl(
            url = url,
            expiresAtMs = etspMs,
            codec = "flac",
            bitsPerSample = downloadResult.bit_depth,
            sampleRateHz = (downloadResult.sampling_rate * 1000).toInt(),
            coverArtUrl = track.album?.image?.large ?: track.album?.image?.thumbnail,
            origin = "qbdlx"
        )
    }
    
    private fun parseEtspMs(url: String): Long? {
        val match = Regex("""[?&]etsp=(\d+)""").find(url) ?: return null
        val secs = match.groupValues[1].toLongOrNull() ?: return null
        return secs * 1000L
    }
}

@Singleton
class SquidStreamResolver @Inject constructor(
    private val apiClient: SquidApiClient
) : LosslessStreamResolver {
    override suspend fun resolve(song: Song): StreamUrl? {
        val query = "${song.artists.firstOrNull()?.name ?: ""} ${song.title}".trim()
        val searchResult = apiClient.search(query)
        val track = searchResult?.tracks?.items?.firstOrNull { it.streamable } ?: return null
        
        val downloadResult = apiClient.getFileUrl(track.id, 27) // 27 = FLAC Hi-Res
        val url = downloadResult?.url ?: return null
        
        val etspMs = parseEtspMs(url) ?: (System.currentTimeMillis() + 3600_000L)
        
        return StreamUrl(
            url = url,
            expiresAtMs = etspMs,
            codec = "flac",
            bitsPerSample = track.maximum_bit_depth,
            sampleRateHz = (track.maximum_sampling_rate * 1000).toInt(),
            coverArtUrl = track.album?.image?.large ?: track.album?.image?.thumbnail,
            origin = "squid"
        )
    }
    
    private fun parseEtspMs(url: String): Long? {
        val match = Regex("""[?&]etsp=(\d+)""").find(url) ?: return null
        val secs = match.groupValues[1].toLongOrNull() ?: return null
        return secs * 1000L
    }
}


@Singleton
class ArcodStreamResolver @Inject constructor(
    private val apiClient: ArcodApiClient
) : LosslessStreamResolver {
    override suspend fun resolve(song: Song): StreamUrl? {
        val query = "${song.artists.firstOrNull()?.name ?: ""} ${song.title}".trim()
        val searchResult = apiClient.search(query)
        val track = searchResult?.tracks?.items?.firstOrNull { it.streamable } ?: return null
        
        val streamResult = apiClient.streamUrl(track.id, 27) // 27 = FLAC Hi-Res
        val url = streamResult?.url ?: return null
        
        val etspMs = streamResult.expiresInSec?.let { System.currentTimeMillis() + (it * 1000L) } 
            ?: (System.currentTimeMillis() + 3600_000L)
        
        return StreamUrl(
            url = url,
            expiresAtMs = etspMs,
            codec = "flac",
            bitsPerSample = track.maximum_bit_depth,
            sampleRateHz = (track.maximum_sampling_rate * 1000).toInt(),
            coverArtUrl = track.album?.image?.large ?: track.album?.image?.thumbnail,
            origin = "arcod"
        )
    }
}
