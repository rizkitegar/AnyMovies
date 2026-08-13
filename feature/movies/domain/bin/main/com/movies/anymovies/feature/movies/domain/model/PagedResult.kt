package com.movies.anymovies.feature.movies.domain.model

public data class PagedResult<T>(
    val items: List<T>,
    val page: Int,
    val totalPages: Int,
    val totalResults: Int,
)
