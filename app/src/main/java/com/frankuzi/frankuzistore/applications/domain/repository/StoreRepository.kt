package com.frankuzi.frankuzistore.applications.domain.repository

import com.frankuzi.frankuzistore.applications.domain.model.ApplicationsRequestState
import kotlinx.coroutines.flow.MutableStateFlow

interface StoreRepository {
    fun getApplications(): MutableStateFlow<ApplicationsRequestState>
}