package com.benmohammad.paginationmvi.domain.repo

import com.benmohammad.paginationmvi.domain.entity.Photo

interface PhotoRepository {
    suspend fun getPhotos(
        start: Int,
        limit: Int
    ): List<Photo>
}
