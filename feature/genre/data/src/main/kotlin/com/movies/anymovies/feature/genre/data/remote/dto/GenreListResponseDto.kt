package com.movies.anymovies.feature.genre.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class GenreListResponseDto(
    val genres: List<GenreDto> = emptyList(),
)
