package com.movies.anymovies.core.database.moviedetail

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie_details")
public data class MovieDetailEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val tagline: String?,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val runtime: Int?,
    val genreIds: String,
    val genreNames: String,
    val voteAverage: Double,
    val voteCount: Int,
    val status: String,
    val originalLanguage: String,
    val homepage: String?,
    val fetchedAtMillis: Long,
)
