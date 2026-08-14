package com.movies.anymovies.feature.movies.presentation.model

import com.movies.anymovies.feature.movies.domain.model.Movie
import java.util.Locale

data class MovieUiModel(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val releaseYearLabel: String?,
    val voteAverageLabel: String?,
)

internal fun Movie.toUiModel(): MovieUiModel {
    return MovieUiModel(
        id = id,
        title = title,
        posterUrl = posterUrl,
        releaseYearLabel = releaseYear?.takeIf { it.isNotBlank() },
        voteAverageLabel = voteAverage.takeIf { it > 0.0 }?.let {
            String.format(Locale.US, "%.1f", it)
        },
    )
}
