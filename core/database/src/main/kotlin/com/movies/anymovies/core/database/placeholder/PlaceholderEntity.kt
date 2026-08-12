package com.movies.anymovies.core.database.placeholder

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "placeholder")
internal data class PlaceholderEntity(
    @PrimaryKey val id: Int,
)
