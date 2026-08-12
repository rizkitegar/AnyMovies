package com.movies.anymovies.feature.movies.data.mapper

import com.movies.anymovies.core.database.movie.MovieEntity
import com.movies.anymovies.feature.movies.data.remote.dto.MovieDto
import com.movies.anymovies.feature.movies.domain.model.Movie

private const val POSTER_BASE_URL = "https://image.tmdb.org/t/p/w342"
private const val BACKDROP_BASE_URL = "https://image.tmdb.org/t/p/w780"

internal fun MovieDto.toEntity(genreId: Int, page: Int, orderIndex: Int): MovieEntity = MovieEntity(
    id = id,
    genreId = genreId,
    page = page,
    orderIndex = orderIndex,
    title = title,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
    overview = overview,
)

internal fun MovieEntity.toDomain(): Movie = Movie(
    id = id,
    title = title,
    posterUrl = posterPath?.let { "$POSTER_BASE_URL$it" },
    backdropUrl = backdropPath?.let { "$BACKDROP_BASE_URL$it" },
    releaseYear = releaseDate?.takeIf { it.isNotBlank() }?.take(4),
    voteAverage = voteAverage,
    voteCount = voteCount,
    overview = overview,
)
