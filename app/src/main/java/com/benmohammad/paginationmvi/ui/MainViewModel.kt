package com.benmohammad.paginationmvi.ui

import androidx.lifecycle.ViewModel
import com.benmohammad.paginationmvi.domain.dispatchers.RxSchedulerProvider
import javax.inject.Inject

class MainViewModel @Inject constructor(
        private val interactor: MainContract.Interactor,
        private val rxSchedulerProvider: RxSchedulerProvider
): ViewModel() {
}