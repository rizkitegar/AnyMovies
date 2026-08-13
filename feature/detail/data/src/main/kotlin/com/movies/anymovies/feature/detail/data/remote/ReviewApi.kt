package com.movies.anymovies.feature.detail.data.remote

import com.movies.anymovies.feature.detail.data.remote.dto.ReviewListDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface ReviewApi {

    @GET("movie/{movie_id}/reviews")
    suspend fun getReviews(
        @Path("movie_id") movieId: Int,
        @Query("page") page: Int,
        @Query("language") language: String = "en-US",
    ): ReviewListDto
}
