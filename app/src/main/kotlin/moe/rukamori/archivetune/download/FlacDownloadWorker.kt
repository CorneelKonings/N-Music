package moe.rukamori.archivetune.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import moe.rukamori.archivetune.R
import moe.rukamori.archivetune.constants.DownloadLocationUriKey
import moe.rukamori.archivetune.constants.FlacQuality
import moe.rukamori.archivetune.constants.FlacQualityKey
import moe.rukamori.archivetune.db.entities.ArtistEntity
import moe.rukamori.archivetune.db.entities.Song
import moe.rukamori.archivetune.db.entities.SongEntity
import moe.rukamori.archivetune.extensions.toEnum
import moe.rukamori.archivetune.utils.dataStore
import moe.rukamori.archivetune.utils.getAsync
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class FlacDownloadWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val safDirectoryManager = SafDirectoryManager(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val songId = inputData.getString("songId") ?: return@withContext Result.failure()
        val title = inputData.getString("title") ?: return@withContext Result.failure()
        val artist = inputData.getString("artist") ?: return@withContext Result.failure()
        val album = inputData.getString("album") ?: return@withContext Result.failure()

        createNotificationChannel()
        setForeground(createForegroundInfo(title))

        val treeUriString = context.dataStore.getAsync(DownloadLocationUriKey, "")
        if (treeUriString.isEmpty()) {
            return@withContext Result.failure()
        }

        val entryPoint =
            EntryPointAccessors.fromApplication(context.applicationContext, FlacDownloaderEntryPoint::class.java)

        val song =
            entryPoint.database().song(songId).first()
                ?: Song(
                    song = SongEntity(id = songId, title = title, albumName = album),
                    artists = listOf(ArtistEntity(id = artist, name = artist)),
                )

        val quality =
            context.dataStore.getAsync(FlacQualityKey, FlacQuality.HI_RES.name).toEnum(FlacQuality.HI_RES)

        val streamUrl = entryPoint.losslessStreamResolver().resolve(song, quality)
        if (streamUrl == null) {
            showDownloadError(title)
            return@withContext Result.failure()
        }

        val treeUri = Uri.parse(treeUriString)
        val albumDir = safDirectoryManager.getOrCreateAlbumDirectory(treeUri, artist, album)
            ?: return@withContext Result.failure()

        val fileName = "${sanitizeFileName(title)}.flac"
        val existingFile = albumDir.findFile(fileName)
        if (existingFile != null && existingFile.exists()) {
            return@withContext Result.success()
        }

        val file = albumDir.createFile("audio/flac", fileName) ?: return@withContext Result.failure()

        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        try {
            val url = URL(streamUrl.url)
            connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36")
            connection.setRequestProperty("Referer", "https://music.youtube.com/")
            connection.connectTimeout = 10_000
            connection.readTimeout = 30_000
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                file.delete()
                return@withContext Result.failure()
            }

            inputStream = connection.inputStream
            context.contentResolver.openOutputStream(file.uri)?.use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            Result.success()
        } catch (e: Exception) {
            file.delete()
            showDownloadError(title)
            return@withContext Result.failure()
        } finally {
            inputStream?.close()
            connection?.disconnect()
            safDirectoryManager.clearCache()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(
                NotificationChannel(
                    "download",
                    context.getString(R.string.downloading),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
            )
        }
    }

    private fun showDownloadError(title: String) {
        createNotificationChannel()
        val notification =
            NotificationCompat.Builder(context, "download")
                .setContentTitle(context.getString(R.string.flac_download_failed))
                .setContentText(title)
                .setSmallIcon(R.drawable.error)
                .build()
        NotificationManagerCompat.from(context).notify(title.hashCode(), notification)
    }

    private fun createForegroundInfo(title: String): ForegroundInfo {
        val notification = NotificationCompat.Builder(context, "download")
            .setContentTitle(context.getString(R.string.downloading))
            .setContentText(title)
            .setSmallIcon(R.drawable.download)
            .setOngoing(true)
            .build()

        return if (Build.VERSION.SDK_INT >= 34) {
            ForegroundInfo(
                title.hashCode(),
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                title.hashCode(),
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(title.hashCode(), notification)
        }
    }
}
