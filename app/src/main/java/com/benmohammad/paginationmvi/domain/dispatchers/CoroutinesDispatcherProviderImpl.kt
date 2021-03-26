package com.benmohammad.paginationmvi.domain.dispatchers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.rx2.asCoroutineDispatcher
import javax.inject.Inject

class CoroutinesDispatcherProviderImpl @Inject constructor(
    rxSchedulerProvider: RxSchedulerProvider
): CoroutinesDispatcherProvider {
    override val io: CoroutineDispatcher = rxSchedulerProvider.io.asCoroutineDispatcher()
    override val main: CoroutineDispatcher = rxSchedulerProvider.main.asCoroutineDispatcher()
}