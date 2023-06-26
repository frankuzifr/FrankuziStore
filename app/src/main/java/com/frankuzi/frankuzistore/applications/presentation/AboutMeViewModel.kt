package com.frankuzi.frankuzistore.applications.presentation

import androidx.lifecycle.ViewModel
import com.frankuzi.frankuzistore.applications.domain.repository.AboutMeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AboutMeViewModel @Inject constructor(
    private val aboutMeRepository: AboutMeRepository
): ViewModel() {

    val aboutMeInfo = aboutMeRepository.aboutMeInfo

    fun updateInfo() {
        aboutMeRepository.updateInfo()
    }
}