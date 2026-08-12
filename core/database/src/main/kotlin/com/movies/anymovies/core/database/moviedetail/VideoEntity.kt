package com.movies.anymovies.core.database.moviedetail

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "movie_videos",
    indices = [Index(value = ["movieId"])],
)
public data class VideoEntity(
    @PrimaryKey val id: String,
    val movieId: Int,
    val key: String,
    val name: String,
    val site: String,
    val type: String,
    val official: Boolean,
    val publishedAt: String?,
    val orderIndex: Int,
)
