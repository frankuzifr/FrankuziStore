package com.frankuzi.frankuzistore

import android.app.Application
import com.frankuzi.frankuzistore.applications.domain.utils.DownloadHandler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application() {
    companion object {
        var downloadHandler: DownloadHandler? = null
    }
}