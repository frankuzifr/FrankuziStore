package com.frankuzi.frankuzistore.applications.domain.utils

import com.frankuzi.frankuzistore.applications.domain.model.ApplicationInfo

interface DownloadHandler {
    fun enqueueDownload(applicationInfo: ApplicationInfo, onProgressChanged: (Int) -> Unit, onComplete: () -> Unit, onError: () -> Unit)
    fun cancel()
}