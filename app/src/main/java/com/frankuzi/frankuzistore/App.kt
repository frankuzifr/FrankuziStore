package com.frankuzi.frankuzistore

import android.app.Application
import com.frankuzi.frankuzistore.applications.domain.utils.DownloadHandler
import com.frankuzi.frankuzistore.applications.domain.utils.Downloader
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application() {
    companion object {
        var downloader: Downloader? = null
    }
}