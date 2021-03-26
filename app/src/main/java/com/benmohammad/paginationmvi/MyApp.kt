package com.benmohammad.paginationmvi

import android.app.Application
import com.benmohammad.paginationmvi.di.DaggerApplicationComponent
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class MyApp: DaggerApplication() {

    private val applicationComponent by lazy {
        DaggerApplicationComponent.factory().create(this)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return applicationComponent
    }
}