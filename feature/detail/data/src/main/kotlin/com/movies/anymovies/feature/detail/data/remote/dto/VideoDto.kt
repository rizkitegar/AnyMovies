package com.movies.anymovies.feature.detail.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class VideoDto(
    val id: String,
    val key: String = "",
    val name: String = "",
    val site: String = "",
    val type: String = "",
    val official: Boolean = false,
    @SerialName("published_at") val publishedAt: String? = null,
)

@Serializable
internal data class VideoListDto(
    val results: List<VideoDto> = emptyList(),
)
