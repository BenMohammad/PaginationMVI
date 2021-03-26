package com.benmohammad.paginationmvi.ui

import com.benmohammad.paginationmvi.domain.dispatchers.CoroutinesDispatcherProvider
import com.benmohammad.paginationmvi.domain.usecase.GetPhotosUseCase
import com.benmohammad.paginationmvi.domain.usecase.GetPostsUseCase
import io.reactivex.Observable
import javax.inject.Inject

class MainInteractorImpl @Inject constructor(
        private val getPhotosUseCase: GetPhotosUseCase,
        private val getPostsUseCase: GetPostsUseCase,
        private val dispatchers: CoroutinesDispatcherProvider
): MainContract.Interactor {

    override fun photoNextPageChange(start: Int, limit: Int): Observable<MainContract.PartialStateChange.PhotoNextPage> {
        TODO("Not yet implemented")
    }

    override fun photoFirstPageChange(limit: Int): Observable<MainContract.PartialStateChange.PostFirstPage> {
        TODO("Not yet implemented")
    }

    override fun postFirstPageChanges(limit: Int): Observable<MainContract.PartialStateChange.PostFirstPage> {
        TODO("Not yet implemented")
    }

    override fun postNextPageChanges(start: Int, limit: Int): Observable<MainContract.PartialStateChange.PostNextPage> {
        TODO("Not yet implemented")
    }

    override fun refreshAll(limitPost: Int, limitPhoto: Int): Observable<MainContract.PartialStateChange.Refresh> {
        TODO("Not yet implemented")
    }
}