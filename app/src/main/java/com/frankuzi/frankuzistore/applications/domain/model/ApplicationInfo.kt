package com.frankuzi.frankuzistore.applications.domain.model

import com.frankuzi.frankuzistore.applications.presentation.ApplicationState

data class ApplicationInfo(
    val packageName: String,
    val applicationName: String,
    val description: String,
    val downloadUrl: String,
    val imageUrl: String,
    var applicationState: ApplicationState = ApplicationState.NotDownloaded
)