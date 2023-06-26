package com.frankuzi.frankuzistore.di

import com.frankuzi.frankuzistore.applications.data.repository.AboutMeRepositoryImpl
import com.frankuzi.frankuzistore.applications.data.repository.StoreRepositoryImpl
import com.frankuzi.frankuzistore.applications.domain.repository.AboutMeRepository
import com.frankuzi.frankuzistore.applications.domain.repository.StoreRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindStoreRepository(
        storeRepositoryImpl: StoreRepositoryImpl
    ): StoreRepository

    @Binds
    @Singleton
    abstract fun bindAboutMeRepository(
        aboutMeRepositoryImpl: AboutMeRepositoryImpl
    ): AboutMeRepository
}