package com.benmohammad.paginationmvi.di.modules

import com.benmohammad.paginationmvi.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface MainActivityModule {

    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    fun contributeMainActivity(): MainActivity
}