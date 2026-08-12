package com.movies.anymovies.feature.detail.data.remote

import com.movies.anymovies.feature.detail.data.remote.dto.MovieDetailDto
import com.movies.anymovies.feature.detail.data.remote.dto.VideoListDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface MovieDetailApi {

    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id") movieId: Int,
        @Query("append_to_response") appendToResponse: String = "videos",
        @Query("language") language: String = "en-US",
    ): MovieDetailDto

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String = "en-US",
    ): VideoListDto
}
