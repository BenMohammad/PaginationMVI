package com.benmohammad.paginationmvi.domain.repo

import com.benmohammad.paginationmvi.domain.entity.Post

interface PostRepository {
    suspend fun getPosts(
        start: Int,
        limit: Int
    ): List<Post>
}