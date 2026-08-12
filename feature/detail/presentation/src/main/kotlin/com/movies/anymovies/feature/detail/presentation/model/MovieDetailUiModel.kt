package com.movies.anymovies.feature.detail.presentation.model

import androidx.compose.runtime.Immutable
import com.movies.anymovies.feature.detail.domain.model.MovieDetail
import com.movies.anymovies.feature.detail.domain.model.Video
import com.movies.anymovies.feature.detail.domain.usecase.SelectTrailerUseCase
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable
public data class GenreUiModel(
    val id: Int,
    val name: String,
)

@Immutable
public data class VideoUiModel(
    val id: String,
    val key: String,
    val name: String,
    val thumbnailUrl: String,
)

@Immutable
public data class MovieDetailUiModel(
    val id: Int,
    val title: String,
    val tagline: String?,
    val overviewLabel: String,
    val posterUrl: String?,
    val headerImageUrl: String?,
    val releaseDateLabel: String?,
    val runtimeLabel: String?,
    val genres: ImmutableList<GenreUiModel>,
    val ratingLabel: String,
    val status: String,
    val originalLanguageLabel: String,
    val homepage: String?,
    val videos: ImmutableList<VideoUiModel>,
    val selectedTrailerKey: String?,
)

private val releaseDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM uuuu", Locale.US)

private const val NO_SYNOPSIS_LABEL = "No synopsis available"

private val selectTrailerUseCase = SelectTrailerUseCase()

public fun MovieDetail.toUiModel(): MovieDetailUiModel {
    val youtubeVideos = videos.filter { it.site == "YouTube" }
    return MovieDetailUiModel(
        id = id,
        title = title,
        tagline = tagline?.takeIf { it.isNotBlank() },
        overviewLabel = overview.takeIf { it.isNotBlank() } ?: NO_SYNOPSIS_LABEL,
        posterUrl = posterUrl,
        headerImageUrl = backdropUrl ?: posterUrl,
        releaseDateLabel = releaseDate?.format(releaseDateFormatter),
        runtimeLabel = runtimeMinutes?.takeIf { it > 0 }?.let(::formatRuntime),
        genres = genres.map { GenreUiModel(id = it.id, name = it.name) }.toImmutableList(),
        ratingLabel = formatRating(voteAverage, voteCount),
        status = status,
        originalLanguageLabel = originalLanguage.uppercase(Locale.US),
        homepage = homepage?.takeIf { it.isNotBlank() },
        videos = youtubeVideos.map { it.toUiModel() }.toImmutableList(),
        selectedTrailerKey = selectTrailerUseCase(videos)?.key,
    )
}

private fun Video.toUiModel(): VideoUiModel = VideoUiModel(
    id = id,
    key = key,
    name = name,
    thumbnailUrl = "https://img.youtube.com/vi/$key/mqdefault.jpg",
)

private fun formatRuntime(minutes: Int): String {
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return "${hours}h ${remainingMinutes}m"
}

private fun formatRating(voteAverage: Double, voteCount: Int): String {
    val averageLabel = String.format(Locale.US, "%.1f", voteAverage)
    val countLabel = NumberFormat.getNumberInstance(Locale.US).format(voteCount)
    return "$averageLabel · $countLabel votes"
}
