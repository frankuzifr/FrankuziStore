package com.frankuzi.frankuzistore.applications.domain.repository

import com.frankuzi.frankuzistore.applications.domain.model.AboutMeRequestState
import kotlinx.coroutines.flow.StateFlow

interface AboutMeRepository {
    val aboutMeInfo: StateFlow<AboutMeRequestState>

    fun updateInfo()
}