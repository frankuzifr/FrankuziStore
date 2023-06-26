package com.frankuzi.frankuzistore.applications.domain.model

import kotlinx.coroutines.flow.StateFlow

sealed class ApplicationsRequestState {
    object Loading: ApplicationsRequestState()
    data class Success(val applications: StateFlow<List<ApplicationInfo>>): ApplicationsRequestState()
    data class Failed(val message: String?): ApplicationsRequestState()
}