package com.movies.anymovies.core.database.moviedetail

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
public interface MovieDetailDao {

    @Query("SELECT * FROM movie_details WHERE id = :movieId")
    public fun observeDetail(movieId: Int): Flow<MovieDetailEntity?>

    @Query("SELECT * FROM movie_videos WHERE movieId = :movieId ORDER BY orderIndex ASC")
    public fun observeVideos(movieId: Int): Flow<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertDetail(detail: MovieDetailEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertVideos(videos: List<VideoEntity>)

    @Query("DELETE FROM movie_videos WHERE movieId = :movieId")
    public suspend fun deleteVideosByMovie(movieId: Int)

    @Transaction
    public suspend fun replaceDetail(detail: MovieDetailEntity, videos: List<VideoEntity>) {
        deleteVideosByMovie(detail.id)
        insertDetail(detail)
        insertVideos(videos)
    }
}
