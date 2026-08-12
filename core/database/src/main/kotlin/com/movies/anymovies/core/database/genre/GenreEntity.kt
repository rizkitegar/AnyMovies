package com.movies.anymovies.core.database.genre

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "genres")
public data class GenreEntity(
    @PrimaryKey val id: Int,
    val name: String,
)
