package com.benmohammad.paginationmvi.domain.dispatchers

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class RxSchedulerProviderImpl @Inject constructor(): RxSchedulerProvider{
    override val io =  Schedulers.io()
    override val main = AndroidSchedulers.mainThread()
}