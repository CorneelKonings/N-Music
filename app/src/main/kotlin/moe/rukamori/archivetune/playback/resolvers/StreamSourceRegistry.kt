package moe.rukamori.archivetune.playback.resolvers

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
        val kennyyResult = kennyyResolver.resolve(song, quality)
        if (kennyyResult != null) {
            return kennyyResult
        }
        
        val squidResult = squidResolver.resolve(song, quality)
        if (squidResult != null) {
            return squidResult
        }
        
        val arcodResult = arcodResolver.resolve(song, quality)
        if (arcodResult != null) {
            return arcodResult
        }
        
        return qbdlxResolver.resolve(song, quality)
    }
}
