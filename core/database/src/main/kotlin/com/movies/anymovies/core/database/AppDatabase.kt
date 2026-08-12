package com.movies.anymovies.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.movies.anymovies.core.database.placeholder.PlaceholderDao
import com.movies.anymovies.core.database.placeholder.PlaceholderEntity

@Database(
    entities = [PlaceholderEntity::class],
    version = 1,
    exportSchema = true,
)
public abstract class AppDatabase : RoomDatabase() {
    internal abstract fun placeholderDao(): PlaceholderDao
}
