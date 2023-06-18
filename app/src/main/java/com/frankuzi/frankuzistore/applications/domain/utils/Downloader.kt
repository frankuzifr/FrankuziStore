package com.frankuzi.frankuzistore.applications.domain.utils

import android.content.Context
import com.frankuzi.frankuzistore.applications.domain.model.ApplicationInfo

class Downloader(
    private val context: Context
) {
    fun addedDownload(applicationInfo: ApplicationInfo, onProgressChanged: (Int) -> Unit, onComplete: () -> Unit, onError: () -> Unit) {
        val downloadHandler = DownloadHandlerImpl(context)
        downloadHandler.enqueueDownload(applicationInfo, onProgressChanged, onComplete, onError)
    }
}