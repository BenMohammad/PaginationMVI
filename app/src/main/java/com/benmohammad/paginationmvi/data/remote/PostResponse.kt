package com.benmohammad.paginationmvi.data.remote

import com.squareup.moshi.Json

data class PostResponse(
    @Json(name = "body")
    val body: String,
    @Json(name = "id")
    val id: Int,
    @Json(name = "title")
    val title: String,
    @Json(name = "userId")
    val userId: Int
)
