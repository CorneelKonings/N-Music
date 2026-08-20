package moe.rukamori.archivetune.flaccore.streaming

import kotlinx.coroutines.CancellationException
import moe.rukamori.archivetune.flaccore.FlacLogger
import moe.rukamori.archivetune.flaccore.arcod.ArcodClient
import moe.rukamori.archivetune.flaccore.arcod.ArcodDeliveredQuality
import moe.rukamori.archivetune.flaccore.arcod.ArcodMatcher
import moe.rukamori.archivetune.flaccore.model.TrackQuery

/**
 * Stream-URL resolver backed by ARCOD's single stream-URL GET via [ArcodClient].
 *
 * Ported from Stash core/media/streaming/ArcodStreamResolver.kt
 */
class ArcodStreamResolver(
    private val client: ArcodClient,
) {
    private val logger = FlacLogger(TAG)

    suspend fun resolve(query: TrackQuery, requestedQuality: Int): FlacStreamUrl? {
        logger.d("resolve attempt title='${query.title}'")
        return try {
            for (term in query.searchTerms()) {
                val items = client.search(term)
                val match = ArcodMatcher.best(query, items) ?: continue
                val item = match.item
                val stream = client.streamUrl(item.id, requestedQuality) ?: run {
                    logger.d("no_stream_url trackId=${item.id} quality=$requestedQuality")
                    return null
                }
                val ttlMs = stream.expiresInSec
                    ?.let { (it.toLong() * 1000L - EXPIRY_SAFETY_MS).coerceAtLeast(MIN_TTL_MS) }
                    ?: DEFAULT_TTL_MS
                val delivered = ArcodDeliveredQuality.of(requestedQuality, item.maxBitDepth, item.maxSamplingRate)
                logger.d("resolved origin=$ORIGIN quality=$requestedQuality term='$term'")
                return FlacStreamUrl(
                    url = stream.url,
                    expiresAtMs = System.currentTimeMillis() + ttlMs,
                    codec = "flac",
                    bitsPerSample = delivered.bitsPerSample,
                    sampleRateHz = delivered.sampleRateHz,
                    origin = ORIGIN,
                    coverArtUrl = item.album?.image?.large?.takeIf { it.isNotBlank() },
                )
            }
            logger.d("no_match")
            null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.w("resolve failed: ${e.message}")
            null
        }
    }

    companion object {
        const val ORIGIN = "arcod"
        private const val TAG = "ArcodStreamResolver"
        private const val DEFAULT_TTL_MS = 280_000L
        private const val EXPIRY_SAFETY_MS = 20_000L
        private const val MIN_TTL_MS = 5_000L
    }
}
