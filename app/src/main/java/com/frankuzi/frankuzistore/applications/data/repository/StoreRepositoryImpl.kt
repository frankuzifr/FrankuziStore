package com.frankuzi.frankuzistore.applications.data.repository

import com.frankuzi.frankuzistore.applications.data.remote.StoreApi
import com.frankuzi.frankuzistore.applications.domain.model.ApplicationInfo
import com.frankuzi.frankuzistore.applications.domain.model.ApplicationsRequestState
import com.frankuzi.frankuzistore.applications.domain.repository.StoreRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class StoreRepositoryImpl @Inject constructor(
    private val api: StoreApi,
    private val firestore: FirebaseFirestore
) : StoreRepository {

    private var _applications = MutableStateFlow<ApplicationsRequestState>(ApplicationsRequestState.Loading)

    override fun updateApplicationsInfo(): MutableStateFlow<ApplicationsRequestState> {
        _applications.value = ApplicationsRequestState.Loading

        firestore.collection("applications")
            .get()
            .addOnSuccessListener { documents ->

                val applicationsInfo = mutableListOf<ApplicationInfo>()
                for (document in documents) {
                    applicationsInfo.add(ApplicationInfo(
                        packageName = document.data["packageName"].toString(),
                        applicationName = document.data["applicationName"].toString(),
                        ruApplicationName = document.data["ruApplicationName"].toString(),
                        description = document.data["description"].toString(),
                        ruDescription = document.data["ruDescription"].toString(),
                        downloadUrl = document.data["downloadUrl"].toString(),
                        imageUrl = document.data["imageUrl"].toString(),
                        githubUrl = document.data["githubUrl"].toString(),
                    ))
                }

                _applications.value = ApplicationsRequestState.Success(MutableStateFlow(applicationsInfo))
            }
            .addOnFailureListener { error ->
                _applications.value = ApplicationsRequestState.Failed(error.message)
            }

        return _applications
    }
}