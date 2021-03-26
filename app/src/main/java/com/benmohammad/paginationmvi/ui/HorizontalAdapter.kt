package com.benmohammad.paginationmvi.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.benmohammad.paginationmvi.R
import com.benmohammad.paginationmvi.ui.MainContract.Item.HorizontalList.HorizontalItem
import com.benmohammad.paginationmvi.ui.MainContract.PostVS
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.recycler_item_horizonta_post.view.*
import kotlinx.android.synthetic.main.recycler_item_placeholder.view.*

private object HorizontalItemCallback: DiffUtil.ItemCallback<HorizontalItem>() {
    override fun areItemsTheSame(
        oldItem: HorizontalItem,
        newItem: HorizontalItem
    ): Boolean {
        return when {
            oldItem is HorizontalItem.Post && newItem  is HorizontalItem.Post -> oldItem.post.id == newItem.post.id
            oldItem is HorizontalItem.PlaceHolder && newItem is HorizontalItem.PlaceHolder -> true
            else -> oldItem == newItem
        }
    }

    override fun areContentsTheSame(
        oldItem: HorizontalItem,
        newItem: HorizontalItem
    ) = oldItem == newItem


    override fun getChangePayload(
        oldItem: HorizontalItem,
        newItem: HorizontalItem
    ): Any? {
        return when {
            oldItem is HorizontalItem.Post && newItem is HorizontalItem.Post -> newItem.post
            oldItem is HorizontalItem.PlaceHolder && newItem is HorizontalItem.PlaceHolder -> newItem.state
            else -> null
        }
    }
}



class HorizontalAdapter(
    private val compositeDisposable: CompositeDisposable
): ListAdapter<HorizontalItem, HorizontalAdapter.VH>(HorizontalItemCallback) {

    private val retryNextPageS = PublishSubject.create<Unit>()
    val retryNextObservable get() =  retryNextPageS

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.recycler_item_horizonta_post -> PostVH(itemView)
            R.layout.recycler_item_horizontal_placeholder -> PlacerHolderVH(itemView)
            else -> error("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))


    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        if(payloads.isEmpty()) return holder.bind(getItem(position))
        payloads.forEach { payload ->
            when {
                payload is PostVS && holder is PostVH -> holder.update(payload)
                payload is MainContract.PlaceHolderState && holder is PlacerHolderVH -> holder.update(payload)
            }
        }
    }

    @LayoutRes
    override fun getItemViewType(position: Int) = getItem(position).viewType

    abstract class VH(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: HorizontalItem)
    }

    private class PostVH(itemView: View): VH(itemView) {
        private val textTitle = itemView.text_title!!
        private val textBody = itemView.text_body!!

        override fun bind(item: HorizontalItem) {
            if(item !is HorizontalItem.Post) return
            update(item.post)
        }

        fun update(post: PostVS) {
            textTitle.text = post.title
            textBody.text = post.body
        }
    }

    private inner class PlacerHolderVH(itemView: View): VH(itemView) {
        private val progressBar = itemView.progress_bar!!
        private val textError = itemView.text_error!!
        private val buttonRetry = itemView.button_retry!!

        init {
            buttonRetry
                .clicks()
                .filter {
                    val position = adapterPosition
                    if(position == RecyclerView.NO_POSITION) {
                        false
                    } else {
                        (getItem(position) as? HorizontalItem.PlaceHolder)?.state is MainContract.PlaceHolderState.Error
                    }
                }
                .subscribeBy { retryNextPageS.onNext(Unit) }
                .addTo(compositeDisposable)
        }

        override fun bind(item: HorizontalItem) {
            if(item !is HorizontalItem.PlaceHolder) return
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


}