package moe.rukamori.archivetune.download

import android.content.Context
import android.net.Uri
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.rukamori.archivetune.constants.DownloadLocationUriKey
import moe.rukamori.archivetune.utils.dataStore
import moe.rukamori.archivetune.utils.getAsync

object FlacDownloader {
    fun downloadFlac(
        context: Context,
        songId: String,
        title: String,
        artist: String,
        album: String,
    ) {
        val data =
            Data.Builder()
                .putString("songId", songId)
                .putString("title", title)
                .putString("artist", artist)
                .putString("album", album)
                .build()

        val constraints =
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val request =
            OneTimeWorkRequestBuilder<FlacDownloadWorker>()
                .setConstraints(constraints)
                .setInputData(data)
                .build()

        WorkManager.getInstance(context).enqueueUniqueWork("flac_download_$songId", ExistingWorkPolicy.KEEP, request)
    }

    fun deleteFlac(
        context: Context,
        songId: String,
        title: String,
        artist: String,
        album: String,
    ) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork("flac_download_$songId")
        workManager.pruneWork()

        CoroutineScope(Dispatchers.IO).launch {
            val treeUriString = context.dataStore.getAsync(DownloadLocationUriKey, "")
            if (treeUriString.isNotEmpty()) {
                val treeUri = Uri.parse(treeUriString)
                val safDirectoryManager = SafDirectoryManager(context)
                val albumDir = safDirectoryManager.getOrCreateAlbumDirectory(treeUri, artist, album)
                if (albumDir != null) {
                    val fileName = "${sanitizeFileName(title)}.flac"
                    val existingFile = albumDir.findFile(fileName)
                    if (existingFile != null && existingFile.exists()) {
                        existingFile.delete()
                    }
                }
                safDirectoryManager.clearCache()
            }
        }
    }
}
