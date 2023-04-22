package com.frankuzi.frankuzistore.applications.domain.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.frankuzi.frankuzistore.BuildConfig
import java.io.File

class AndroidDownloader(
    private val context: Context
) {
    private val _downloadManager = context.getSystemService(DownloadManager::class.java)

    fun downloadFile(url: String): Long {
        val request = DownloadManager.Request(url.toUri())
            .setMimeType("application/vnd.android.package-archive")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle("application.apk")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "app.apk")

//        showInstallOption(destination, uri)
        _downloadManager.enqueue(request)
        Toast.makeText(context, "Downloading...", Toast.LENGTH_LONG)
            .show()

        return _downloadManager.enqueue(request)
    }

    private fun showInstallOption(destination: String, uri: Uri) {
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val contentUri = FileProvider.getUriForFile(
                        context,
                        BuildConfig.APPLICATION_ID + ".provider",
                        File(destination)
                    )
                    val install = Intent(Intent.ACTION_VIEW)
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                    install.data = contentUri
                    context.startActivity(install)
                    context.unregisterReceiver(this)
                } else {
                    val install = Intent(Intent.ACTION_VIEW)
                    install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    install.setDataAndType(uri, "\"application/vnd.android.package-archive\"")
                    context.startActivity(install)
                    context.unregisterReceiver(this)
                }
            }
        }

        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
}