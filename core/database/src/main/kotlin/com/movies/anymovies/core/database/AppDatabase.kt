package com.movies.anymovies.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.movies.anymovies.core.database.genre.GenreDao
import com.movies.anymovies.core.database.genre.GenreEntity
import com.movies.anymovies.core.database.movie.MovieDao
import com.movies.anymovies.core.database.movie.MovieEntity
import com.movies.anymovies.core.database.movie.MovieRemoteKeyEntity
import com.movies.anymovies.core.database.placeholder.PlaceholderDao
import com.movies.anymovies.core.database.placeholder.PlaceholderEntity

@Database(
    entities = [
        PlaceholderEntity::class,
        GenreEntity::class,
        MovieEntity::class,
        MovieRemoteKeyEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
public abstract class AppDatabase : RoomDatabase() {
    internal abstract fun placeholderDao(): PlaceholderDao

    public abstract fun genreDao(): GenreDao

    public abstract fun movieDao(): MovieDao
}
