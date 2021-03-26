package com.benmohammad.paginationmvi.di

import com.benmohammad.paginationmvi.MyApp
import com.benmohammad.paginationmvi.di.modules.DataModule
import com.benmohammad.paginationmvi.di.modules.DomainModule
import com.benmohammad.paginationmvi.di.modules.MainActivityModule
import com.benmohammad.paginationmvi.di.modules.ViewModelModule
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule

@ApplicationScope
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        MainActivityModule::class,
        DataModule::class,
        DomainModule::class,
        ViewModelModule::class
    ]
)
interface ApplicationComponent: AndroidInjector<MyApp> {
    @Component.Factory
    interface Factory : AndroidInjector.Factory<MyApp>
}