package moe.rukamori.archivetune.flaccore.model

/*
 * Ported from Stash (GPL-3.0)
 * Original source: com.stash.data.download.lossless.LosslessSource.kt
 */

/**
 * Identification fields for a track lookup. ISRC is the most precise
 * identifier — when present, sources should match by ISRC first, falling
 * back to artist+title fuzzy match only when no ISRC hit is found. Duration
 * is used to disambiguate "studio vs live" type mismatches.
 */
data class TrackQuery(
    val artist: String,
    val title: String,
    val album: String? = null,
    val isrc: String? = null,
    val durationMs: Long? = null,
    /** Explicit-content identity when known; null means the source cannot verify it. */
    val explicit: Boolean? = null,
) {
    /**
     * The proxy search terms to try for this query, in priority order.
     *
     * - When an [isrc] is present it is the precise index key — used alone.
     * - Otherwise the FULL artist credit is tried first ("Tyler, The Creator Foo"),
     *   so single artists whose name contains a comma still match.
     * - Then, for multi-artist credits joined with commas (e.g.
     *   "¥$, Kanye West, Ty Dolla $ign"), a fallback using only the PRIMARY artist
     *   (text before the first comma → "¥$") is appended.
     */
    fun searchTerms(): List<String> {
        isrc?.takeIf { it.isNotBlank() }?.let { return listOf(it) }
        val full = "$artist $title".trim()
        val primary = artist.substringBefore(",").trim()
        return if (primary.isNotEmpty() && !primary.equals(artist.trim(), ignoreCase = true)) {
            listOf(full, "$primary $title".trim())
        } else {
            listOf(full)
        }
    }
}

/**
 * Audio format expected from the source.
 */
data class AudioFormat(
    val id: String,
    val codec: String,
    val bitrateKbps: Int? = null,
    val bitsPerSample: Int? = null,
    val sampleRateHz: Int? = null,
) {
    val isLossless: Boolean
        get() = codec.lowercase() in LOSSLESS_CODECS

    companion object {
        val LOSSLESS_CODECS = setOf("flac", "alac", "wav", "ape", "tta", "wv", "aiff")
    }
}

/**
 * Snapshot of a source's rate-limit state.
 */
data class RateLimitState(
    val tokensAvailable: Double,
    val msUntilNextToken: Long,
    val isCircuitBroken: Boolean,
    val msUntilUnblock: Long,
    val recentFailures: Int,
)
