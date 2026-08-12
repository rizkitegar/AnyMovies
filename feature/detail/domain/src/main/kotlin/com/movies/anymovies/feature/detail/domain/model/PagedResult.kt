package com.movies.anymovies.feature.detail.domain.model

public data class PagedResult<T>(
    val items: List<T>,
    val page: Int,
    val totalPages: Int,
    val totalResults: Int,
)
