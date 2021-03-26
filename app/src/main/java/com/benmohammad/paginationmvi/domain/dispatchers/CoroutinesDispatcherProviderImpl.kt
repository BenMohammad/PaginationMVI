package com.benmohammad.paginationmvi.domain.dispatchers

import com.benmohammad.paginationmvi.di.ApplicationScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.rx2.asCoroutineDispatcher
import javax.inject.Inject

@ApplicationScope
class CoroutinesDispatcherProviderImpl @Inject constructor(
    rxSchedulerProvider: RxSchedulerProvider
): CoroutinesDispatcherProvider {
    override val io: CoroutineDispatcher = rxSchedulerProvider.io.asCoroutineDispatcher()
    override val main: CoroutineDispatcher = rxSchedulerProvider.main.asCoroutineDispatcher()
}