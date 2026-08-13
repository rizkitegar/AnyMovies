package com.movies.anymovies.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object GenreList : Route

    @Serializable
    data class MovieList(val genreId: Int, val genreName: String) : Route

    @Serializable
    data class MovieDetail(val movieId: Int) : Route
}
