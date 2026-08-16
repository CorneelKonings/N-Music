package moe.rukamori.archivetune.download

import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.rukamori.archivetune.R
import moe.rukamori.archivetune.constants.DownloadLocationUriKey
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
        val urlString = inputData.getString("url") ?: return@withContext Result.failure()

        setForeground(createForegroundInfo(title))

        val treeUriString = context.dataStore.getAsync(DownloadLocationUriKey, "")
        if (treeUriString.isEmpty()) {
            return@withContext Result.failure()
        }

        val treeUri = Uri.parse(treeUriString)
        val albumDir = safDirectoryManager.getOrCreateAlbumDirectory(treeUri, artist, album)
            ?: return@withContext Result.failure()

        val fileName = "$title.flac"
        val existingFile = albumDir.findFile(fileName)
        if (existingFile != null && existingFile.exists()) {
            return@withContext Result.success()
        }

        val file = albumDir.createFile("audio/flac", fileName) ?: return@withContext Result.failure()

        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
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
            Result.failure()
        } finally {
            inputStream?.close()
            connection?.disconnect()
            safDirectoryManager.clearCache()
        }
    }

    private fun createForegroundInfo(title: String): ForegroundInfo {
        val notification = NotificationCompat.Builder(context, "download")
            .setContentTitle(context.getString(R.string.downloading))
            .setContentText(title)
            .setSmallIcon(R.drawable.download)
            .setOngoing(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
