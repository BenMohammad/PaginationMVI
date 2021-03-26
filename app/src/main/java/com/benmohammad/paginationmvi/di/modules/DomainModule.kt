package com.benmohammad.paginationmvi.di.modules

import com.benmohammad.paginationmvi.data.PhotoRepositoryImpl
import com.benmohammad.paginationmvi.data.PostRepositoryImpl
import com.benmohammad.paginationmvi.domain.dispatchers.CoroutinesDispatcherProvider
import com.benmohammad.paginationmvi.domain.dispatchers.CoroutinesDispatcherProviderImpl
import com.benmohammad.paginationmvi.domain.dispatchers.RxSchedulerProvider
import com.benmohammad.paginationmvi.domain.dispatchers.RxSchedulerProviderImpl
import com.benmohammad.paginationmvi.domain.repo.PhotoRepository
import com.benmohammad.paginationmvi.domain.repo.PostRepository
import dagger.Binds
import dagger.Module

@Module
interface DomainModule {

    @Binds
    fun provideRxSchedulerProvider(rxSchedulerProviderImpl: RxSchedulerProviderImpl): RxSchedulerProvider

    @Binds
    fun provideCoroutineDispatchersProvider(coroutinesDispatcherProviderImpl: CoroutinesDispatcherProviderImpl): CoroutinesDispatcherProvider

    @Binds
    fun providePhotosRepository(photoRepositoryImpl: PhotoRepositoryImpl): PhotoRepository

    @Binds
    fun providePostRepository(postRepositoryImpl: PostRepositoryImpl): PostRepository

}