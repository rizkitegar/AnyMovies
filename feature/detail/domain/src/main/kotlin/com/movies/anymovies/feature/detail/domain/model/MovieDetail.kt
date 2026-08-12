package com.movies.anymovies.feature.detail.domain.model

import java.time.LocalDate

public data class MovieDetail(
    val id: Int,
    val title: String,
    val tagline: String?,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: LocalDate?,
    val runtimeMinutes: Int?,
    val genres: List<Genre>,
    val voteAverage: Double,
    val voteCount: Int,
    val status: String,
    val originalLanguage: String,
    val homepage: String?,
    val videos: List<Video>,
)
