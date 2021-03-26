package com.benmohammad.paginationmvi.data

import com.benmohammad.paginationmvi.data.remote.ApiService
import com.benmohammad.paginationmvi.di.ApplicationScope
import com.benmohammad.paginationmvi.domain.dispatchers.CoroutinesDispatcherProvider
import com.benmohammad.paginationmvi.domain.entity.Photo
import com.benmohammad.paginationmvi.domain.repo.PhotoRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ApplicationScope
class PhotoRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val dispatcherProvider: CoroutinesDispatcherProvider
): PhotoRepository {

    override suspend fun getPhotos(start: Int, limit: Int): List<Photo> {
        return withContext(dispatcherProvider.io) {
            apiService.getPhotos(start = start, limit = limit).map {
                Photo(
                    id = it.id,
                    title = it.title,
                    albumId = it.albumId,
                    thumbnailUrl = it.thumbnailUrl,
                    url = it.url
                )
            }
        }
    }
}