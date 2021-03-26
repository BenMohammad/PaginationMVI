package com.benmohammad.paginationmvi.ui

import com.benmohammad.paginationmvi.domain.dispatchers.CoroutinesDispatcherProvider
import com.benmohammad.paginationmvi.domain.usecase.GetPhotosUseCase
import com.benmohammad.paginationmvi.domain.usecase.GetPostsUseCase
import com.benmohammad.paginationmvi.ui.MainContract.PhotoVS
import com.benmohammad.paginationmvi.ui.MainContract.PostVS
import io.reactivex.Observable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.rx2.rxObservable
import java.lang.Exception
import javax.inject.Inject

class MainInteractorImpl @Inject constructor(
        private val getPhotosUseCase: GetPhotosUseCase,
        private val getPostsUseCase: GetPostsUseCase,
        private val dispatchers: CoroutinesDispatcherProvider
): MainContract.Interactor {

    override fun photoNextPageChange(
        start: Int,
        limit: Int
    ): Observable<MainContract.PartialStateChange.PhotoNextPage> {
        return rxObservable(dispatchers.main) {
            send(MainContract.PartialStateChange.PhotoNextPage.Loading)
            try {
                getPhotosUseCase(start = start, limit = limit)
                    .map ( MainContract::PhotoVS )
                    .let {
                        MainContract.PartialStateChange.PhotoNextPage.Data(it)
                    }
                    .let {
                        send(it)
                    }
            } catch (e: Exception) {
                delayError()
                send(MainContract.PartialStateChange.PhotoNextPage.Error(e))
            }
        }
    }

    override fun photoFirstPageChange(limit: Int): Observable<MainContract.PartialStateChange.PhotoFirstPage> {
        return rxObservable(dispatchers.main) {
            send(MainContract.PartialStateChange.PhotoFirstPage.Loading)
            try {
                getPhotosUseCase(start = 0, limit)
                    .map(MainContract::PhotoVS)
                    .let { MainContract.PartialStateChange.PhotoFirstPage.Data(it) }
                    .let { send(it) }
            } catch (e: Exception) {
                delayError()
                send(MainContract.PartialStateChange.PhotoFirstPage.Error(e))
            }
        }
    }

    override fun postFirstPageChanges(limit: Int): Observable<MainContract.PartialStateChange.PostFirstPage> {
        return rxObservable(dispatchers.main) {
            send(MainContract.PartialStateChange.PostFirstPage.Loading)
            try {
                getPostsUseCase(start = 0, limit = limit)
                    .map(::PostVS)
                    .let { MainContract.PartialStateChange.PostFirstPage.Data(it) }
                    .let { send(it) }
            } catch (e: Exception) {
                delayError()
                send(MainContract.PartialStateChange.PostFirstPage.Error(e))
            }
        }
    }

    override fun postNextPageChanges(start: Int, limit: Int): Observable<MainContract.PartialStateChange.PostNextPage> {
        return rxObservable(dispatchers.main) {
            send(MainContract.PartialStateChange.PostNextPage.Loading)
            try {
                getPostsUseCase(start = start, limit = limit)
                    .map(::PostVS)
                    .let { MainContract.PartialStateChange.PostNextPage.Data(it) }
                    .let { send(it) }
            } catch (e: Exception) {
                delayError()
                send(MainContract.PartialStateChange.PostNextPage.Error(e))
            }
        }
    }

    override fun refreshAll(limitPost: Int, limitPhoto: Int): Observable<MainContract.PartialStateChange.Refresh> {
        return rxObservable(dispatchers.main) {
            send(MainContract.PartialStateChange.Refresh.Refreshing)
            try {
                coroutineScope {
                    val async1 = async { getPostsUseCase(limit = limitPost, start = 0) }
                    val async2 = async { getPhotosUseCase(limit = limitPhoto, start = 0) }

                    send(
                        MainContract.PartialStateChange.Refresh.Success(
                            posts = async1.await().map(::PostVS),
                            photos = async2.await().map(::PhotoVS)
                        )
                    )
                }
            } catch (e: Exception) {
                delayError()
                send(MainContract.PartialStateChange.Refresh.Error(e))
            }
        }
    }

    private suspend fun delayError() = delay(0)
}