package com.frankuzi.frankuzistore.applications.data.remote

import retrofit2.http.GET

interface StoreApi {
    @GET("path")
    suspend fun getApplications()
}