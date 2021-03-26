package com.benmohammad.paginationmvi.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.benmohammad.paginationmvi.di.AppViewModelFactory
import com.benmohammad.paginationmvi.di.ViewModelKey
import com.benmohammad.paginationmvi.ui.MainContract
import com.benmohammad.paginationmvi.ui.MainInteractorImpl
import com.benmohammad.paginationmvi.ui.MainViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    abstract fun provideMainInteractor(mainInteractorImpl: MainInteractorImpl): MainContract.Interactor

    @Binds
    abstract fun provideViewModelFactory(appViewModelFactory: AppViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(mainViewModel: MainViewModel): ViewModel
}