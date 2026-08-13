package com.movies.anymovies.feature.detail.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ReviewListDto(
    val page: Int = 1,
    val results: List<ReviewDto> = emptyList(),
    @SerialName("total_pages") val totalPages: Int = 0,
    @SerialName("total_results") val totalResults: Int = 0,
)

@Serializable
internal data class ReviewDto(
    val id: String = "",
    val author: String = "",
    @SerialName("author_details") val authorDetails: AuthorDetailsDto = AuthorDetailsDto(),
    val content: String = "",
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
internal data class AuthorDetailsDto(
    val name: String = "",
    val username: String = "",
    @SerialName("avatar_path") val avatarPath: String? = null,
    val rating: Double? = null,
)
