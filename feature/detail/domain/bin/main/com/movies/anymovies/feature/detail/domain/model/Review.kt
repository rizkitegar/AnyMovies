package com.movies.anymovies.feature.detail.domain.model

import java.time.Instant

public data class Review(
    val id: String,
    val author: String,
    val username: String,
    val avatarUrl: String?,
    val rating: Double?,
    val content: String,
    val createdAt: Instant,
)
