package com.benmohammad.paginationmvi.data

import com.benmohammad.paginationmvi.data.remote.ApiService
import com.benmohammad.paginationmvi.domain.dispatchers.CoroutinesDispatcherProvider
import com.benmohammad.paginationmvi.domain.entity.Post
import com.benmohammad.paginationmvi.domain.repo.PostRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val dispatcherProvider: CoroutinesDispatcherProvider
): PostRepository {

    override suspend fun getPosts(start: Int, limit: Int): List<Post> {
        return withContext(dispatcherProvider.io) {
            apiService.getPosts(start = start, limit = limit).map {
                Post(
                    body = it.body,
                    title  = it.title,
                    id = it.id,
                    userId = it.userId
                )
            }
        }
    }
}