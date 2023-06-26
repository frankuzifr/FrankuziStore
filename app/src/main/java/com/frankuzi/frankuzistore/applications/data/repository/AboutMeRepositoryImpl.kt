package com.frankuzi.frankuzistore.applications.data.repository

import com.frankuzi.frankuzistore.applications.data.remote.StoreApi
import com.frankuzi.frankuzistore.applications.domain.model.AboutMeInfo
import com.frankuzi.frankuzistore.applications.domain.model.AboutMeRequestState
import com.frankuzi.frankuzistore.applications.domain.repository.AboutMeRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class AboutMeRepositoryImpl @Inject constructor(
    private val api: StoreApi,
    private val firestore: FirebaseFirestore
) : AboutMeRepository {

    private var _aboutMeInfo = MutableStateFlow<AboutMeRequestState>(AboutMeRequestState.Loading)
    override val aboutMeInfo: StateFlow<AboutMeRequestState> = _aboutMeInfo.asStateFlow()

    override fun updateInfo() {
        _aboutMeInfo.value = AboutMeRequestState.Loading

        firestore.collection("aboutMe")
            .get()
            .addOnSuccessListener { documents ->

                var aboutMeInfo: AboutMeInfo? = null

                for (document in documents) {
                    aboutMeInfo = AboutMeInfo(
                        name = document.data["name"].toString(),
                        imageUrl = document.data["imageUrl"].toString(),
                        email = document.data["email"].toString(),
                        githubLink = document.data["githubLink"].toString(),
                        description = document.data["description"].toString()
                    )
                }

                aboutMeInfo?.let {
                    _aboutMeInfo.value = AboutMeRequestState.Success(it)
                    return@addOnSuccessListener
                }

                _aboutMeInfo.value = AboutMeRequestState.Failed("Collection not found")
            }
            .addOnFailureListener { error ->
                _aboutMeInfo.value = AboutMeRequestState.Failed(error.message)
            }
    }

}