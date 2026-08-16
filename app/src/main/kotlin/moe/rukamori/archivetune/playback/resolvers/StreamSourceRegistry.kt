package moe.rukamori.archivetune.playback.resolvers

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import moe.rukamori.archivetune.constants.FlacQuality
import moe.rukamori.archivetune.db.entities.Song
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamSourceRegistry @Inject constructor(
    private val kennyyResolver: KennyyStreamResolver,
    private val squidResolver: SquidStreamResolver,
    private val arcodResolver: ArcodStreamResolver,
    private val qbdlxResolver: QbdlxStreamResolver
) : LosslessStreamResolver {
    
    override suspend fun resolve(song: Song, quality: FlacQuality): StreamUrl? {
        val qbdlxResult = try {
            withTimeout(35_000L) {
                qbdlxResolver.resolve(song, quality)
            }
        } catch (e: TimeoutCancellationException) {
            null
        }
        if (qbdlxResult != null) {
            return qbdlxResult
        }

        val arcodResult = try {
            withTimeout(35_000L) {
                arcodResolver.resolve(song, quality)
            }
        } catch (e: TimeoutCancellationException) {
            null
        }
        if (arcodResult != null) {
            return arcodResult
        }

        val kennyyResult = try {
            withTimeout(4_000L) {
                kennyyResolver.resolve(song, quality)
            }
        } catch (e: TimeoutCancellationException) {
            null
        }
        if (kennyyResult != null) {
            return kennyyResult
        }

        return try {
            withTimeout(4_000L) {
                squidResolver.resolve(song, quality)
            }
        } catch (e: TimeoutCancellationException) {
            null
        }
    }
}
