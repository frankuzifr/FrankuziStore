package com.frankuzi.frankuzistore.applications.domain.model

import com.frankuzi.frankuzistore.applications.presentation.ApplicationState

data class ApplicationInfo(
    val packageName: String,
    val applicationName: String,
    val ruApplicationName: String,
    val description: String,
    val ruDescription: String,
    val downloadUrl: String,
    val imageUrl: String,
    val githubUrl: String,
    var applicationState: ApplicationState = ApplicationState.NotDownloaded
)