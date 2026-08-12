package com.movies.anymovies.core.database.movie

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "movie_remote_keys",
    primaryKeys = ["movieId", "genreId"],
    indices = [
        Index(value = ["movieId"]),
        Index(value = ["genreId", "page"]),
    ],
)
public data class MovieRemoteKeyEntity(
    val movieId: Int,
    val genreId: Int,
    val page: Int,
    val isLastPage: Boolean,
    val totalPages: Int,
    val totalResults: Int,
    val fetchedAtMillis: Long,
)
