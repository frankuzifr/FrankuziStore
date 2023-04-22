package com.frankuzi.frankuzistore.applications.domain.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.widget.Toast
import androidx.core.content.FileProvider
import com.frankuzi.frankuzistore.BuildConfig
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

    fun enqueueDownload(url: String) {

        val destination = "${Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)}/${FILE_NAME}"
//        destination += FILE_NAME

        val uri = Uri.parse("$FILE_BASE_PATH$destination")
        val file = File(destination)

        if (file.exists())
            file.delete()

        val downloadUri = Uri.parse(url)
        val request = DownloadManager.Request(downloadUri)
            .setMimeType(MIME_TYPE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle("Download")
            .setDestinationUri(uri)

        val downloadId = _downloadManager.enqueue(request)
        showInstallOption(destination, uri, downloadId)
        Toast.makeText(context, "Downloading...", Toast.LENGTH_LONG)
            .show()
    }

    private fun showInstallOption(destination: String, uri: Uri, downloadId: Long) {
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(recieverContext: Context?, intent: Intent?) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val contentUri = FileProvider.getUriForFile(
                        recieverContext!!,
                        BuildConfig.APPLICATION_ID + PROVIDER_PATH,
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
                    install.setDataAndType(uri, APP_INSTALL_PATH)
                    context.startActivity(install)
                    context.unregisterReceiver(this)
                }

                val onInstalled = object : BroadcastReceiver(){
                    override fun onReceive(installedContetxt: Context?, installedIntent: Intent?) {
                        File(destination).delete()
                        context.unregisterReceiver(this)
                    }
                }

                context.registerReceiver(onInstalled, IntentFilter("android.intent.action.PACKAGE_ADDED"))
            }
        }

        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
}