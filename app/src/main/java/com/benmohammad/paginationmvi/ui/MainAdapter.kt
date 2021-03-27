package com.benmohammad.paginationmvi.ui

import android.graphics.Rect
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.benmohammad.paginationmvi.R
import com.benmohammad.paginationmvi.asObservable
import com.benmohammad.paginationmvi.ui.MainContract.Item
import com.benmohammad.paginationmvi.ui.MainContract.Item.PlaceHolder
import com.jakewharton.rxbinding3.recyclerview.scrollEvents
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.util.HalfSerializer.onNext
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.recycler_item_horizontal_list.view.*
import kotlinx.android.synthetic.main.recycler_item_photo.view.*
import kotlinx.android.synthetic.main.recycler_item_placeholder.view.*

private object DiffUtilItemCallback: DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
        return when {
            oldItem is PlaceHolder && newItem is PlaceHolder -> true
            oldItem is Item.HorizontalList && newItem is Item.HorizontalList -> true
            oldItem is Item.Photo && newItem is Item.Photo -> oldItem.photo.id == newItem.photo.id
            else -> oldItem == newItem
        }
    }
    override fun areContentsTheSame(oldItem: Item, newItem: Item) = oldItem == newItem

    override fun getChangePayload(oldItem: Item, newItem: Item): Any? {
        return when {
            oldItem is PlaceHolder && newItem is PlaceHolder -> newItem.state
            oldItem is Item.HorizontalList && newItem is Item.HorizontalList -> newItem
            oldItem is Item.Photo && newItem is Item.Photo -> newItem.photo
            else -> null
        }
    }
}

class MainAdapter(
    private val compositeDisposable: CompositeDisposable,
    private val viewPool: RecyclerView.RecycledViewPool
): ListAdapter<Item, MainAdapter.VH>(DiffUtilItemCallback) {
    private val scrollToFirst = PublishSubject.create<Unit>()
    private var layoutManagerSavedState: Parcelable? = null
    private val retryS = PublishSubject.create<Unit>()
    val retryObservable get() = retryS.asObservable()
    private val loadNextPagerHorizontalS = PublishSubject.create<Unit>()
    val loadingNextPageHorizontalObservable get() = loadNextPagerHorizontalS.asObservable()
    private val retryNextPagerHorizontalS = PublishSubject.create<Unit>()
    val retryNextPageHorizontalObservable get() = retryNextPagerHorizontalS.asObservable()
    private val retryHorizontalS = PublishSubject.create<Unit>()
    val retryHorizontalObservable get() = retryHorizontalS.asObservable()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.recycler_item_photo -> PhotoVH(itemView)
            R.layout.recycler_item_placeholder -> PlaceHolderVH(itemView, parent)
            R.layout.recycler_item_horizontal_list  -> HorizontalListVH(itemView, parent)
            else -> error("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        if(payloads.isEmpty()) return holder.bind(getItem(position))
        payloads.forEach { payload ->
            when  {
                payload is MainContract.PlaceHolderState && holder is PlaceHolderVH -> holder.update(payload)
                payload is Item.HorizontalList && holder is HorizontalListVH -> holder.update(payload)
                payload is Item.Photo && holder is PhotoVH -> holder.update(payload)
            }
        }
    }

    @LayoutRes
    override fun getItemViewType(position: Int) = getItem(position).viewType


    abstract class VH(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: Item)
    }

    private class PhotoVH(itemView: View): VH(itemView) {
        private val image = itemView.image!!

        override fun bind(item: Item) {
            if(item !is Item.Photo) return
            update(item)
        }

        fun update(item: Item.Photo) {
            image.load(item.photo.thumbnailUrl) {
                crossfade(true)
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }
        }
    }

    private inner class PlaceHolderVH(itemView: View, parent: ViewGroup): VH(itemView) {
        private val progressBar = itemView.progress_bar!!
        private val textError = itemView.text_error!!
        private val buttonRetry = itemView.button_retry

        init {
            buttonRetry
                .clicks()
                .takeUntil(parent.detaches())
                .filter {
                    val position = adapterPosition
                    if(position == RecyclerView.NO_POSITION) {
                            false
                    } else {
                        (getItem(position) as? Item.PlaceHolder)?.state is MainContract.PlaceHolderState.Error
                    }
                }
                .subscribeBy { retryS.onNext(Unit) }
                .addTo(compositeDisposable)
        }

        override fun bind(item: Item) {
            if(item !is Item.PlaceHolder) return
            update(item.state)
        }

        fun update(state: MainContract.PlaceHolderState) {
            when(state) {
                MainContract.PlaceHolderState.Loading -> {
                    progressBar.isInvisible = false
                    textError.isInvisible = true
                    buttonRetry.isInvisible = true
                }
                MainContract.PlaceHolderState.Idle -> {
                    progressBar.isInvisible = true
                    textError.isInvisible = true
                    buttonRetry.isInvisible = true
                }

                is MainContract.PlaceHolderState.Error -> {
                    progressBar.isInvisible = true
                    textError.isInvisible = false
                    buttonRetry.isInvisible = false
                    textError.text = state.error.message
                }
            }
        }
    }

    private inner class HorizontalListVH(itemView: View, parent: ViewGroup): VH(itemView) {
        private val recycler= itemView.recycler_horizontal!!.apply { setRecycledViewPool(viewPool) }
        private val progressBar = itemView.progress_bar_horizontal!!
        private val textError = itemView.text_error_horizontal!!
        private val buttonRetry = itemView.button_retry_horizontal!!

        private val adapter = HorizontalAdapter(compositeDisposable)
        private val visibleThreshold get() = 2
        internal val linearLayoutManager =
            LinearLayoutManager(itemView.context, RecyclerView.HORIZONTAL, false)

        init {
            recycler.run {
                setHasFixedSize(true)
                adapter = this@HorizontalListVH.adapter
                layoutManager = this@HorizontalListVH.linearLayoutManager

                addItemDecoration(object: RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(
                        outRect: Rect,
                        view: View,
                        parent: RecyclerView,
                        state: RecyclerView.State
                    ) {
                        outRect.run {
                            right = if(parent.getChildAdapterPosition(view) == parent.adapter!!.itemCount - 1) 0 else 8
                            left = 0
                            top = 0
                            bottom = 0
                        }
                    }
                })
            }

            adapter
                .retryNextObservable
                .subscribe(retryNextPagerHorizontalS::onNext)
                .addTo(compositeDisposable)

            buttonRetry.clicks()
                .takeUntil(parent.detaches())
                .filter {
                    val position = adapterPosition
                    if(position == RecyclerView.NO_POSITION) {
                        false
                    } else {
                        (getItem(position) as? Item.HorizontalList)?.shouldRetry() == true
                    }
                }
                .subscribeBy { retryHorizontalS.onNext(Unit) }
                .addTo(compositeDisposable)

            recycler
                .scrollEvents()
                .takeUntil(parent.detaches())
                .filter { (_, dx, _) ->
                    val layoutManager = recycler.layoutManager as LinearLayoutManager
                    dx > 0 && layoutManager.findLastVisibleItemPosition() + visibleThreshold >= layoutManager.itemCount
                }
                .subscribeBy { loadNextPagerHorizontalS.onNext(Unit) }
                .addTo(compositeDisposable)
        }

        override fun bind(item: Item) {
            if(item !is Item.HorizontalList) return
            update(item)
        }

        fun update(item: Item.HorizontalList) {
            progressBar.isInvisible = !item.isLoading
            textError.isInvisible = item.error  == null
            buttonRetry.isInvisible  = item.error == null
            textError.text = item.error?.message

            adapter.submitList(item.items)
            layoutManagerSavedState?.let {
                linearLayoutManager.onRestoreInstanceState(it)
                layoutManagerSavedState = null
            }
        }
    }

    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        if(holder is HorizontalListVH) {
            layoutManagerSavedState = holder.linearLayoutManager.onSaveInstanceState()
        }
    }

    fun scrollHorizontalListToFirst() = scrollToFirst.onNext(Unit)
}