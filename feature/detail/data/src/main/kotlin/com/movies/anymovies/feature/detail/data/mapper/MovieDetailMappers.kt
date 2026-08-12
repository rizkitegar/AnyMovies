package com.movies.anymovies.feature.detail.data.mapper

import com.movies.anymovies.core.database.moviedetail.MovieDetailEntity
import com.movies.anymovies.core.database.moviedetail.VideoEntity
import com.movies.anymovies.feature.detail.data.remote.dto.MovieDetailDto
import com.movies.anymovies.feature.detail.data.remote.dto.VideoDto
import com.movies.anymovies.feature.detail.domain.model.Genre
import com.movies.anymovies.feature.detail.domain.model.MovieDetail
import com.movies.anymovies.feature.detail.domain.model.Video
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException

private const val POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500"
private const val BACKDROP_BASE_URL = "https://image.tmdb.org/t/p/w780"
private const val GENRE_DELIMITER = "|"

internal fun MovieDetailDto.toEntity(fetchedAtMillis: Long): MovieDetailEntity = MovieDetailEntity(
    id = id,
    title = title,
    tagline = tagline?.takeIf { it.isNotBlank() },
    overview = overview.orEmpty(),
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    runtime = runtime,
    genreIds = genres.joinToString(GENRE_DELIMITER) { it.id.toString() },
    genreNames = genres.joinToString(GENRE_DELIMITER) { it.name },
    voteAverage = voteAverage,
    voteCount = voteCount,
    status = status,
    originalLanguage = originalLanguage,
    homepage = homepage?.takeIf { it.isNotBlank() },
    fetchedAtMillis = fetchedAtMillis,
)

internal fun VideoDto.toEntity(movieId: Int, orderIndex: Int): VideoEntity = VideoEntity(
    id = id,
    movieId = movieId,
    key = key,
    name = name,
    site = site,
    type = type,
    official = official,
    publishedAt = publishedAt,
    orderIndex = orderIndex,
)

internal fun MovieDetailEntity.toDomain(videos: List<VideoEntity>): MovieDetail = MovieDetail(
    id = id,
    title = title,
    tagline = tagline,
    overview = overview,
    posterUrl = posterPath?.let { "$POSTER_BASE_URL$it" },
    backdropUrl = resolveBackdropUrl(),
    releaseDate = releaseDate.toLocalDateOrNull(),
    runtimeMinutes = runtime?.takeIf { it > 0 },
    genres = toGenres(),
    voteAverage = voteAverage,
    voteCount = voteCount,
    status = status,
    originalLanguage = originalLanguage,
    homepage = homepage,
    videos = videos.sortedBy { it.orderIndex }.map { it.toDomain() },
)

internal fun VideoEntity.toDomain(): Video = Video(
    id = id,
    key = key,
    name = name,
    site = site,
    type = type,
    official = official,
    publishedAt = publishedAt.toInstantOrNull(),
)

private fun MovieDetailEntity.resolveBackdropUrl(): String? {
    val backdropUrl = backdropPath?.let { "$BACKDROP_BASE_URL$it" }
    val posterFallbackUrl = posterPath?.let { "$POSTER_BASE_URL$it" }
    return backdropUrl ?: posterFallbackUrl
}

private fun MovieDetailEntity.toGenres(): List<Genre> {
    if (genreIds.isBlank()) return emptyList()
    val ids = genreIds.split(GENRE_DELIMITER)
    val names = genreNames.split(GENRE_DELIMITER)
    return ids.indices.map { index ->
        Genre(id = ids[index].toIntOrNull() ?: 0, name = names.getOrElse(index) { "" })
    }
}

private fun String?.toLocalDateOrNull(): LocalDate? {
    if (this.isNullOrBlank()) return null
    return try {
        LocalDate.parse(this)
    } catch (e: DateTimeParseException) {
        null
    }
}

private fun String?.toInstantOrNull(): Instant? {
    if (this.isNullOrBlank()) return null
    return try {
        Instant.parse(this)
    } catch (e: DateTimeParseException) {
        null
    }
}
