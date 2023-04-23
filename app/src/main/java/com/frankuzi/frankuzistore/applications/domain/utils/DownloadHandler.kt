package com.frankuzi.frankuzistore.applications.domain.utils

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import com.frankuzi.frankuzistore.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class DownloadHandler(
    private val context: Context
){
    companion object {
        private const val FILE_NAME = "SampleApplication.apk"
        private const val FILE_BASE_PATH = "file://"
        private const val MIME_TYPE = "application/vnd.android.package-archive"
        private const val PROVIDER_PATH = ".provider"
        private const val APP_INSTALL_PATH = "\"application/vnd.android.package-archive\""
    }

    private val _downloadManager = context.getSystemService(DownloadManager::class.java)

    var isDownloading = mutableStateOf(false)
    var progress = mutableStateOf(0)

    @SuppressLint("Range")
    fun enqueueDownload(url: String) {

        if (isDownloading.value)
            return

        isDownloading.value = true

        val destination = "${Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)}/${FILE_NAME}"

        val uri = Uri.parse("$FILE_BASE_PATH$destination")
        val file = File(destination)

        if (file.exists())
            file.delete()

        val downloadUri = Uri.parse(url)
        val request = DownloadManager.Request(downloadUri)
            .setMimeType(MIME_TYPE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setTitle("Download")
            .setDestinationUri(uri)

        val downloadId = _downloadManager.enqueue(request)

        showInstallOption(destination, uri)
        Toast.makeText(context, "Downloading...", Toast.LENGTH_LONG)
            .show()

        GlobalScope.launch(Dispatchers.IO) {

            while (isDownloading.value) {
                val query = DownloadManager.Query()
                query.setFilterById(downloadId)

                val cursor = _downloadManager.query(query)
                cursor.moveToFirst()

                val bytesDownloaded =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val bytesTotal = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    isDownloading.value = false
                }

                progress.value = (bytesDownloaded * 100L / bytesTotal).toInt()

                cursor.close()
            }
        }
    }

    private fun showInstallOption(destination: String, uri: Uri) {
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(recieverContext: Context?, intent: Intent?) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val contentUri = FileProvider.getUriForFile(
                        recieverContext!!,
                        BuildConfig.APPLICATION_ID + PROVIDER_PATH,
                        File(destination)
                    )

                    val install = Intent(Intent.ACTION_VIEW)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)

                    install.data = contentUri
                    context.startActivity(install)
                    context.unregisterReceiver(this)

                } else {
                    val install = Intent(Intent.ACTION_VIEW)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .setDataAndType(uri, APP_INSTALL_PATH)

                    context.startActivity(install)
                    context.unregisterReceiver(this)
                }
            }
        }

        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
}