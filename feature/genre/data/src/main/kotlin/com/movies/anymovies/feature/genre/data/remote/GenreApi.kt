package com.movies.anymovies.feature.genre.data.remote

import com.movies.anymovies.feature.genre.data.remote.dto.GenreListResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

internal interface GenreApi {

    @GET("genre/movie/list")
    suspend fun getGenres(@Query("language") language: String = "en-US"): GenreListResponseDto
}
