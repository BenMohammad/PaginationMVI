package com.benmohammad.paginationmvi.domain.dispatchers

import io.reactivex.Scheduler

interface RxSchedulerProvider {
    val io: Scheduler
    val main: Scheduler
}