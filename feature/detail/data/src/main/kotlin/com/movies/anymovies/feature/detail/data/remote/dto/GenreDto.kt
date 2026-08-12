package com.movies.anymovies.feature.detail.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class GenreDto(
    val id: Int,
    val name: String = "",
)
