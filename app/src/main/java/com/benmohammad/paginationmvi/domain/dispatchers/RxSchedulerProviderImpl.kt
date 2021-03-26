package com.benmohammad.paginationmvi.domain.dispatchers

import com.benmohammad.paginationmvi.di.ApplicationScope
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@ApplicationScope
class RxSchedulerProviderImpl @Inject constructor(): RxSchedulerProvider{
    override val io =  Schedulers.io()
    override val main = AndroidSchedulers.mainThread()
}