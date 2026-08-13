package com.movies.anymovies.feature.detail.domain.model

import java.time.Instant

public data class Video(
    val id: String,
    val key: String,
    val name: String,
    val site: String,
    val type: String,
    val official: Boolean,
    val publishedAt: Instant?,
)
