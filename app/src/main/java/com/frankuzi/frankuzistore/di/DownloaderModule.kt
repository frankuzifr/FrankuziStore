package com.frankuzi.frankuzistore.di

import android.app.Activity
import android.app.Application
import com.frankuzi.frankuzistore.applications.domain.utils.DownloadHandler
import com.frankuzi.frankuzistore.applications.domain.utils.DownloadHandlerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Singleton

//@Module
//@InstallIn(ActivityRetainedComponent::class)
//abstract class DownloaderModule {
//    @Binds
//    @Singleton
//    abstract fun bindDownloadHandler(
//        downloadHandlerImpl: DownloadHandlerImpl
//    ): DownloadHandler
//}

//@Module
//@InstallIn(ActivityRetainedComponent::class)
//object DownloaderModule {
//    @Provides
//    @Singleton
//    fun provideDownloadHandler(context: Application): DownloadHandler {
//        return DownloadHandlerImpl(context)
//    }
//}