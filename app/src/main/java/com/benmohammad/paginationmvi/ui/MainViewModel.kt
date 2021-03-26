package com.benmohammad.paginationmvi.ui


import io.reactivex.rxkotlin.withLatestFrom
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import com.benmohammad.paginationmvi.asObservable
import com.benmohammad.paginationmvi.domain.dispatchers.RxSchedulerProvider
import com.benmohammad.paginationmvi.exhaustMap
import com.benmohammad.paginationmvi.ui.MainContract.*
import com.benmohammad.paginationmvi.ui.MainContract.ViewState.Factory.initial
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class MainViewModel @Inject constructor(
        private val interactor: MainContract.Interactor,
        private val rxSchedulerProvider: RxSchedulerProvider
): ViewModel() {
        private val initial = initial()
        private val _stateD = MutableLiveData<ViewState>().apply { value = initial }
        private val stateS = BehaviorSubject.createDefault(initial)
        private val stateObservable get() = stateS.asObservable()

        private val intentS = PublishSubject.create<ViewIntent>()
        private val singleEvents = PublishSubject.create<SingleEvent>()
        private val compositeDisposable = CompositeDisposable()

        val stateD get() = _stateD.distinctUntilChanged()
        val singleEventObservable get()= singleEvents.asObservable()

        fun processIntents(intents: Observable<ViewIntent>) = intents.subscribe(intentS::onNext)

        private val initialProcessor =
                ObservableTransformer<ViewIntent.Initial, MainContract.PartialStateChange> {
                        intents ->
                        intents.withLatestFrom(stateObservable)
                                .filter{(_, vs) -> vs.photoItems.isEmpty()}
                                .flatMap {
                                        Observable.mergeArray(
                                                interactor.photoFirstPageChange(limit = Companion.PHOTO_PAGE_SIZE),
                                                interactor.postFirstPageChanges(limit = POST_PAGE_SIZE)
                                        )
                                }

                }

        private val nextProcessor =
                ObservableTransformer<ViewIntent.LoadNextPage, PartialStateChange> {
                        intents ->
                        intents.
                                withLatestFrom(stateObservable)
                                .filter {(_, vs) -> vs.canLoadNextPage()}
                                .map { (_, vs) -> vs.photoItems.size }
                                .exhaustMap { interactor.photoNextPageChange(start = it, limit = PHOTO_PAGE_SIZE) }
                }

        private val retryLoadProcessor =
                ObservableTransformer<ViewIntent.RetryLoadPage, PartialStateChange> { intents ->
                        intents
                                .withLatestFrom(stateObservable)
                                .filter {(_, vs) -> vs.shouldRetry()}
                                .map { (_, vs) -> vs.photoItems.size }
                                .exhaustMap { interactor.photoNextPageChange(start = it, limit = PHOTO_PAGE_SIZE) }
                }

        private val loadNextPageHorizontal =
                ObservableTransformer<ViewIntent.LoadNextPageHorizontal, PartialStateChange> { intents ->
                        intents
                                .withLatestFrom(stateObservable)
                                .filter{(_, vs) -> vs.canLoadNextPageHorizontal()}
                                .map{(_, vs) -> vs.getHorizontalListCount()}
                                .exhaustMap { interactor.postNextPageChanges(start = it, limit = POST_PAGE_SIZE) }
                }

        private val retryLoadPageHorizontalProcessor =
                ObservableTransformer<ViewIntent.RetryLoadPageHorizontal, PartialStateChange> {
                        intents ->
                        intents.withLatestFrom(stateObservable)
                                .filter{(_, vs) -> vs.shouldRetryHorizontal()}
                                .map { (_, vs) -> vs.getHorizontalListCount() }
                                .exhaustMap { interactor.postNextPageChanges(start = it, limit = POST_PAGE_SIZE) }
                }

        private val retryHorizontalProcessor =
                ObservableTransformer<ViewIntent.RetryHorizontal, PartialStateChange> { intents ->
                        intents
                                .withLatestFrom(stateObservable)
                                .filter{(_, vs) -> vs.shouldRetryHorizontal()}
                                .exhaustMap { interactor.postFirstPageChanges(limit = POST_PAGE_SIZE) }
                }

        private val refreshProcessor =
                ObservableTransformer<ViewIntent.Refresh, PartialStateChange> { intents ->
                        intents
                                .withLatestFrom(stateObservable)
                                .filter{(_, vs) -> vs.enableRefresh}
                                .exhaustMap {
                                        interactor.refreshAll(
                                                limitPhoto = PHOTO_PAGE_SIZE,
                                                limitPost = POST_PAGE_SIZE
                                        )
                                }
                }


        private val toPartialStateChange =
                ObservableTransformer<ViewIntent, PartialStateChange> { intents ->
                        intents
                                .publish { shared ->
                                        Observable.mergeArray(
                                                shared.ofType<ViewIntent.Initial>().compose(initialProcessor),
                                                shared.ofType<ViewIntent.LoadNextPage>().compose(nextProcessor),
                                                shared.ofType<ViewIntent.RetryLoadPage>().compose(retryLoadProcessor),
                                                shared.ofType<ViewIntent.LoadNextPageHorizontal>().compose(loadNextPageHorizontal),
                                                shared.ofType<ViewIntent.RetryLoadPageHorizontal>().compose(retryLoadPageHorizontalProcessor),
                                                shared.ofType<ViewIntent.RetryHorizontal>().compose(retryHorizontalProcessor),
                                                shared.ofType<ViewIntent.Refresh>().compose(refreshProcessor)
                                        )
                                }
                                .compose(sendingSingleEvent)
                }

        private val sendingSingleEvent =
                ObservableTransformer<PartialStateChange, PartialStateChange> { changes ->
                        changes
                                .observeOn(rxSchedulerProvider.main)
                                .doOnNext{
                                        change ->
                                        when(change) {
                                                is PartialStateChange.PhotoFirstPage.Data -> if(change.photos.isEmpty()) singleEvents.onNext(SingleEvent.HasReachedMax)
                                                is PartialStateChange.PhotoFirstPage.Error -> singleEvents.onNext(SingleEvent.GetPhotosFailure(change.error))
                                                PartialStateChange.PhotoFirstPage.Loading -> Unit

                                                is PartialStateChange.PhotoNextPage.Data -> if(change.photos.isEmpty()) singleEvents.onNext(SingleEvent.HasReachedMax)
                                                is PartialStateChange.PhotoNextPage.Error -> singleEvents.onNext(SingleEvent.GetPhotosFailure(change.error))
                                                PartialStateChange.PhotoNextPage.Loading -> Unit

                                                is PartialStateChange.PostFirstPage.Data -> if(change.posts.isEmpty()) singleEvents.onNext(SingleEvent.HasReachedMaxHorizontal)
                                                is PartialStateChange.PostFirstPage.Error -> singleEvents.onNext(SingleEvent.GetPostsFailure(change.error))
                                                PartialStateChange.PostFirstPage.Loading -> Unit

                                                is PartialStateChange.PostNextPage.Data -> if(change.posts.isEmpty()) singleEvents.onNext(SingleEvent.HasReachedMaxHorizontal)
                                                is PartialStateChange.PostNextPage.Error -> singleEvents.onNext(SingleEvent.GetPostsFailure(change.error))
                                                PartialStateChange.PostNextPage.Loading -> Unit

                                                is PartialStateChange.Refresh.Success -> singleEvents.onNext(SingleEvent.RefreshSuccess)
                                                is PartialStateChange.Refresh.Error -> singleEvents.onNext(SingleEvent.RefreshFailure(change.error))
                                                PartialStateChange.Refresh.Refreshing -> Unit

                                        }
                                }
                }


        init {
            stateS
                    .subscribeBy {_stateD.value = it}
                        .addTo(compositeDisposable)

                intentS
                        .compose(intentFilter)
                        .compose(toPartialStateChange)
                        .observeOn(rxSchedulerProvider.main)
                        .scan(initial) {vs, change -> change.reduce(vs)}
                        .subscribe(stateS::onNext)
                        .addTo(compositeDisposable)
        }

        private companion object {
                val intentFilter = ObservableTransformer<ViewIntent, ViewIntent> { intents ->
                        intents.publish{ shared ->
                                Observable.mergeArray(
                                        shared.ofType<ViewIntent.Initial>().take(1),
                                        shared.filter { it !is ViewIntent.Initial }
                                )
                        }
                }
                const val PHOTO_PAGE_SIZE = 20
                const val POST_PAGE_SIZE = 10
        }
}