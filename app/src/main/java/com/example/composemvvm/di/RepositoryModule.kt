package com.example.composemvvm.di

import com.example.composemvvm.data.IMapRepository
import com.example.composemvvm.data.api.ApiService
import com.example.composemvvm.data.repository.MapRepository
import com.example.composemvvm.domain.GetCoordinatesUseCase
import com.example.composemvvm.domain.IMapUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMapRepo(apiService: ApiService): IMapRepository =
        MapRepository(apiService = apiService)

    @Provides
    fun provideGetCoordinatesUseCase(mapRepository: IMapRepository): IMapUseCase = GetCoordinatesUseCase(mapRepository)
}