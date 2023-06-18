package com.frankuzi.frankuzistore.applications.domain.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
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
import com.frankuzi.frankuzistore.applications.domain.model.ApplicationInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class DownloadHandlerImpl(
    val context: Context
): DownloadHandler {

    companion object {
        private const val FILE_BASE_PATH = "file://"
        private const val MIME_TYPE = "application/vnd.android.package-archive"
        private const val PROVIDER_PATH = ".provider"
        private const val APP_INSTALL_PATH = "\"application/vnd.android.package-archive\""
    }

    private val _downloadManager = context.getSystemService(DownloadManager::class.java)

    var isDownloading = mutableStateOf(false)
    var progress = mutableStateOf(0)

    @SuppressLint("Range")
    override fun enqueueDownload(applicationInfo: ApplicationInfo, onProgressChanged: (Int) -> Unit, onComplete: () -> Unit, onError: () -> Unit) {

//        if (isDownloading.value)
//            return
//
        isDownloading.value = true

        val destination = "${Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)}/${applicationInfo.applicationName}.apk"

        val uri = Uri.parse("$FILE_BASE_PATH$destination")
        val file = File(destination)

        if (file.exists())
            file.delete()

        val downloadUri = Uri.parse(applicationInfo.downloadUrl)
        val request = DownloadManager.Request(downloadUri)
            .setMimeType(MIME_TYPE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION)
            .setTitle("${applicationInfo.applicationName} downloading")
            .setDestinationUri(uri)

        val downloadId = _downloadManager.enqueue(request)

        showInstallOption(destination, uri, onComplete, onError)

        Toast.makeText(context, "${applicationInfo.applicationName} downloading", Toast.LENGTH_LONG)
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

                onProgressChanged.invoke((bytesDownloaded * 100L / bytesTotal).toInt())
                progress.value = (bytesDownloaded * 100L / bytesTotal).toInt()

                cursor.close()
            }
        }
    }

    private fun showInstallOption(destination: String, uri: Uri, onCompleteAction: () -> Unit, onErrorAction: () -> Unit) {
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(recieverContext: Context?, intent: Intent?) {

                val file = File(destination)

                if (!file.exists())
                    return

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

                onCompleteAction.invoke()
            }
        }

        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
}