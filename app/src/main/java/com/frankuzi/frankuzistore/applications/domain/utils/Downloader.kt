package com.frankuzi.frankuzistore.applications.domain.utils

import android.content.Context
import com.frankuzi.frankuzistore.applications.domain.model.ApplicationInfo
import com.frankuzi.frankuzistore.utils.myLog

class Downloader(
    private val context: Context
) {
    private val _downloaderHandlers = mutableListOf<DownloadHandlerWithApplicationInfo>()

    fun addedDownload(applicationInfo: ApplicationInfo, onProgressChanged: (Int) -> Unit, onComplete: () -> Unit, onError: () -> Unit) {
        val downloadHandler = DownloadHandlerImpl(context)
        downloadHandler.enqueueDownload(applicationInfo, onProgressChanged, onComplete, onError)

        val index = _downloaderHandlers.indexOfFirst { downloadHandlerWithApplicationInfo ->
            downloadHandlerWithApplicationInfo.applicationInfo.packageName == applicationInfo.packageName
        }

        val downloadHandlerWithApplicationInfo = DownloadHandlerWithApplicationInfo(
            applicationInfo = applicationInfo,
            downloadHandler = downloadHandler
        )

        if (index != -1) {
            _downloaderHandlers[index] = downloadHandlerWithApplicationInfo
        }
        _downloaderHandlers.add(downloadHandlerWithApplicationInfo)
    }

    fun cancelDownload(applicationInfo: ApplicationInfo) {
        val downloadHandlerWithApplicationInfo = _downloaderHandlers.firstOrNull { downloadHandler ->
            downloadHandler.applicationInfo == applicationInfo
        }

        myLog(downloadHandlerWithApplicationInfo.toString())
        downloadHandlerWithApplicationInfo?.downloadHandler?.cancel()
    }
}

data class DownloadHandlerWithApplicationInfo(
    val applicationInfo: ApplicationInfo,
    val downloadHandler: DownloadHandler
)