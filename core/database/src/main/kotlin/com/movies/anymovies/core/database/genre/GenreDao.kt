package com.movies.anymovies.core.database.genre

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
public interface GenreDao {

    @Query("SELECT * FROM genres ORDER BY name ASC")
    public fun observeAll(): Flow<List<GenreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertAll(genres: List<GenreEntity>)

    @Query("DELETE FROM genres")
    public suspend fun deleteAll()

    @Transaction
    public suspend fun replaceAll(genres: List<GenreEntity>) {
        deleteAll()
        insertAll(genres)
    }
}
