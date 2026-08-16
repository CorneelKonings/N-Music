package moe.rukamori.archivetune.download

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

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
}
