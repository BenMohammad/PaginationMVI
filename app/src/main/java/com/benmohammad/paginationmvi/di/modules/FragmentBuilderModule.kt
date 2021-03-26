package com.benmohammad.paginationmvi.di.modules

import com.benmohammad.paginationmvi.ui.MainFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuilderModule {

    @ContributesAndroidInjector
    abstract fun contributeMainFragment(): MainFragment
}