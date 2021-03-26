package com.benmohammad.paginationmvi.domain.dispatchers

import kotlinx.coroutines.CoroutineDispatcher

interface CoroutinesDispatcherProvider {
    val io: CoroutineDispatcher
    val main: CoroutineDispatcher
}