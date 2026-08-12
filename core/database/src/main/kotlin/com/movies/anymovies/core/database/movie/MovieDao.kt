package com.movies.anymovies.core.database.movie

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
public interface MovieDao {

    @Query("SELECT * FROM movies WHERE genreId = :genreId ORDER BY page ASC, orderIndex ASC")
    public fun observeByGenre(genreId: Int): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movie_remote_keys WHERE genreId = :genreId ORDER BY page DESC LIMIT 1")
    public suspend fun getLatestKey(genreId: Int): MovieRemoteKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertMovies(movies: List<MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertKeys(keys: List<MovieRemoteKeyEntity>)

    @Query("DELETE FROM movies WHERE genreId = :genreId")
    public suspend fun deleteMoviesByGenre(genreId: Int)

    @Query("DELETE FROM movie_remote_keys WHERE genreId = :genreId")
    public suspend fun deleteKeysByGenre(genreId: Int)

    @Transaction
    public suspend fun insertPage(movies: List<MovieEntity>, keys: List<MovieRemoteKeyEntity>) {
        insertMovies(movies)
        insertKeys(keys)
    }

    @Transaction
    public suspend fun replaceGenre(genreId: Int, movies: List<MovieEntity>, keys: List<MovieRemoteKeyEntity>) {
        deleteMoviesByGenre(genreId)
        deleteKeysByGenre(genreId)
        insertMovies(movies)
        insertKeys(keys)
    }
}
