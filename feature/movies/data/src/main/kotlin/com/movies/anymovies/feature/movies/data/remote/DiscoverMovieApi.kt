package com.movies.anymovies.feature.movies.data.remote

import com.movies.anymovies.feature.movies.data.remote.dto.DiscoverMovieResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

internal interface DiscoverMovieApi {

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("with_genres") genreId: Int,
        @Query("page") page: Int,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("language") language: String = "en-US",
        @Query("include_adult") includeAdult: Boolean = false,
    ): DiscoverMovieResponseDto
}
