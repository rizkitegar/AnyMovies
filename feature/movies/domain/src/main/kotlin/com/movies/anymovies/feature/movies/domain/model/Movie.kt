package com.movies.anymovies.feature.movies.domain.model

public data class Movie(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseYear: String?,
    val voteAverage: Double,
    val voteCount: Int,
    val overview: String,
)
