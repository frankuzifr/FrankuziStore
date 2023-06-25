package com.frankuzi.frankuzistore.applications.domain.utils

import android.content.Context
import android.content.pm.PackageManager
import com.frankuzi.frankuzistore.applications.domain.model.ApplicationInfo
import com.frankuzi.frankuzistore.applications.presentation.ApplicationState

class InstalledApplicationsChecker (private val context: Context) {
    fun actualizeInstalledApplications(applications: List<ApplicationInfo>): List<ApplicationInfo> {
        val installedApplications =
            context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        for (application in applications) {
            var isInstalled = false
            for (installedApplication in installedApplications) {
                if (installedApplication.packageName != application.packageName)
                    continue

                isInstalled = true
                break
            }

            application.applicationState = if (isInstalled) {
                ApplicationState.Installed
            } else {
                when (val applicationState = application.applicationState) {
                    ApplicationState.Installed -> {
                        ApplicationState.NotDownloaded
                    }
                    else -> {
                        applicationState
                    }
                }
            }
        }

        return applications
    }
}