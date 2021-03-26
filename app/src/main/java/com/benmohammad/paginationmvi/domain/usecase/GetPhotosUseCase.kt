package com.benmohammad.paginationmvi.domain.usecase

import com.benmohammad.paginationmvi.domain.entity.Photo
import com.benmohammad.paginationmvi.domain.repo.PhotoRepository
import javax.inject.Inject

class GetPhotosUseCase @Inject constructor(
    private val photoRepository: PhotoRepository
) {
    suspend operator fun invoke(start: Int, limit: Int): List<Photo> {
        return photoRepository.getPhotos(start = start, limit = limit)
    }
}