package moe.rukamori.archivetune.flaccore.arcod

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/*
 * Ported from Stash (GPL-3.0)
 * Original source: com.stash.data.download.lossless.arcod.ArcodApiModels.kt
 */

/**
 * Lenient Json instance for the ARCOD (Qobuz-DL proxy) API.
 *
 * ARCOD re-publishes Qobuz catalog JSON, which carries far more fields than
 * Stash models, so [Json.ignoreUnknownKeys] is mandatory. [Json.isLenient]
 * tolerates the proxy's occasional unquoted/relaxed values.
 */
internal val ArcodJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    // The job-create POST body must carry every field the proxy expects,
    // including ones left at their defaults (quality, format, …), so default
    // values are emitted rather than omitted.
    encodeDefaults = true
}

// ── get-music search ────────────────────────────────────────────────────────

@Serializable
data class ArcodSearchResponse(
    val success: Boolean = false,
    val data: ArcodSearchData? = null,
)

@Serializable
data class ArcodSearchData(
    val tracks: ArcodTrackList? = null,
)

@Serializable
data class ArcodTrackList(
    val items: List<ArcodTrackItem> = emptyList(),
)

// ── job lifecycle ─────────────────────────────────────────────────────────────

/**
 * Parsed result of the single (private) stream-URL GET.
 * [url] is the playable, open, Range-capable link; [expiresInSec] is the
 * server-stated lifetime in seconds when the response carried one (used to size
 * the resolver's cache TTL), else null.
 */
data class ArcodStreamResult(
    val url: String,
    val expiresInSec: Int? = null,
)
