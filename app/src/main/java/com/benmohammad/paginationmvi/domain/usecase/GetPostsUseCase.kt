package com.benmohammad.paginationmvi.domain.usecase

import com.benmohammad.paginationmvi.di.ApplicationScope
import com.benmohammad.paginationmvi.domain.entity.Post
import com.benmohammad.paginationmvi.domain.repo.PostRepository
import javax.inject.Inject

@ApplicationScope
class GetPostsUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(start: Int, limit: Int): List<Post> {
        return postRepository.getPosts(start = start, limit = limit)
    }
}