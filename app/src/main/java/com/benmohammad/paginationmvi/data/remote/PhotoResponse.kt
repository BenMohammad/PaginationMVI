package com.benmohammad.paginationmvi.data.remote

import com.squareup.moshi.Json

data class PhotoResponse(
    @Json(name = "albumId")
    val albumId: Int,
    @Json(name = "id")
    val id: Int,
    @Json(name = "thumbnailUrl")
    val thumbnailUrl: String,
    @Json(name = "title")
    val title: String,
    @Json(name = "url")
    val url: String
)
