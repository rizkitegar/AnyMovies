package com.movies.anymovies.core.database.movie

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "movies",
    primaryKeys = ["id", "genreId"],
    indices = [
        Index(value = ["genreId", "page"]),
        Index(value = ["id"]),
    ],
)
public data class MovieEntity(
    val id: Int,
    val genreId: Int,
    val page: Int,
    val orderIndex: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val voteCount: Int,
    val overview: String,
)
