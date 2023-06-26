package com.frankuzi.frankuzistore.applications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frankuzi.frankuzistore.App
import com.frankuzi.frankuzistore.applications.domain.model.ApplicationInfo
import com.frankuzi.frankuzistore.applications.domain.model.ApplicationsRequestState
import com.frankuzi.frankuzistore.applications.domain.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val storeRepository: StoreRepository
): ViewModel() {

    private var _applicationsInfo = storeRepository.updateApplicationsInfo()
    val applicationsInfo = _applicationsInfo.asStateFlow()

    private var _job = Job()
        get() {
            if (field.isCancelled)
                field = Job()
            return field
        }

    fun updateApplicationsInfo() {
        _applicationsInfo = storeRepository.updateApplicationsInfo()
    }

    fun downloadApplication(application: ApplicationInfo) {
        var applications = listOf<ApplicationInfo>()
        when (val applicationsRequestState = applicationsInfo.value) {
            is ApplicationsRequestState.Failed -> {

            }
            ApplicationsRequestState.Loading -> {

            }
            is ApplicationsRequestState.Success -> {
                applications = applicationsRequestState.applications.value
            }
        }

        val index = applications.indexOf(application)
        applications[index].applicationState = ApplicationState.Downloading(0)

        _applicationsInfo.update {
            ApplicationsRequestState.Success(MutableStateFlow(applications))
        }

        App.downloader?.addedDownload(
            applicationInfo = application,
            onProgressChanged = { progress ->
                applications[index].applicationState = ApplicationState.Downloading(progress)
                _applicationsInfo.update {
                    ApplicationsRequestState.Success(MutableStateFlow(applications))
                }
            },
            onComplete = {
                applications[index].applicationState = ApplicationState.Downloaded
                _applicationsInfo.update {
                    ApplicationsRequestState.Success(MutableStateFlow(applications))
                }
            },
            onError = {
                _applicationsInfo.update {
                    ApplicationsRequestState.Failed("Error")
                }
            }
        )
    }

    fun startActualizeApplications() {
        viewModelScope.launch(Dispatchers.IO + _job) {
            while (true) {
                withContext(Dispatchers.IO) {
                    when (val value = applicationsInfo.value) {
                        is ApplicationsRequestState.Success -> {
                            App.installedApplicationsChecker?.let { checker ->
                                val installedApplications =
                                    checker.actualizeInstalledApplications(
                                        value.applications.value
                                    )
                                _applicationsInfo.update {
                                    ApplicationsRequestState.Success(MutableStateFlow(installedApplications))
                                }
                            }
                        }
                        else -> {}
                    }
                    Thread.sleep(1000)
                }
            }
        }
    }

    fun stopActualizeApplications() {
        _job.cancel()
    }
}