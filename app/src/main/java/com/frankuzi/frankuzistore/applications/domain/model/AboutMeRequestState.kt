package com.frankuzi.frankuzistore.applications.domain.model

sealed class AboutMeRequestState {
    data class Success(val aboutMeInfo: AboutMeInfo): AboutMeRequestState()
    object Loading: AboutMeRequestState()
    data class Failed(val message: String?): AboutMeRequestState()
}