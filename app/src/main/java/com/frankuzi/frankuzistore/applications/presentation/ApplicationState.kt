package com.frankuzi.frankuzistore.applications.presentation

sealed class ApplicationState {
    object NotDownloaded : ApplicationState()
    data class Downloading(val progress: Int) : ApplicationState()
    object Downloaded : ApplicationState()
}
