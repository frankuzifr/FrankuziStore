package com.frankuzi.frankuzistore.applications.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.frankuzi.frankuzistore.App
import com.frankuzi.frankuzistore.applications.domain.model.ApplicationInfo
import com.frankuzi.frankuzistore.applications.domain.model.ApplicationsRequestState
import com.frankuzi.frankuzistore.applications.domain.repository.StoreRepository
import com.frankuzi.frankuzistore.applications.domain.utils.DownloadHandlerImpl
import com.frankuzi.frankuzistore.utils.myLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val storeRepository: StoreRepository
): ViewModel() {

    private var _getApplicationState = storeRepository.getApplications()
    val getApplicationState = _getApplicationState.asStateFlow()

    fun getApplications() {
        _getApplicationState = storeRepository.getApplications()
    }

    fun downloadApplication(application: ApplicationInfo) {

        var applications = listOf<ApplicationInfo>()
        when (val applicationsRequestState = getApplicationState.value) {
            is ApplicationsRequestState.Error -> {

            }
            ApplicationsRequestState.Loading -> {

            }
            is ApplicationsRequestState.Success -> {
                applications = applicationsRequestState.applications.value
            }
        }

        val index = applications.indexOf(application)
        myLog(index.toString())
        applications[index].applicationState = ApplicationState.Downloading(0)
        _getApplicationState.update {
            ApplicationsRequestState.Success(MutableStateFlow(applications))
        }

        App.downloadHandler?.enqueueDownload(
            applicationInfo = application,
            onProgressChanged = {progress ->
                myLog(progress.toString())
                applications[index].applicationState = ApplicationState.Downloading(progress)
                _getApplicationState.update {
                    ApplicationsRequestState.Success(MutableStateFlow(applications))
                }
            },
            onComplete = {
                applications[index].applicationState = ApplicationState.Downloaded
                _getApplicationState.update {
                    ApplicationsRequestState.Success(MutableStateFlow(applications))
                }
            },
            onError = {

            }
        )
    }
}