package com.movies.anymovies.feature.movies.data.di

import com.movies.anymovies.core.database.AppDatabase
import com.movies.anymovies.feature.movies.data.remote.DiscoverMovieApi
import com.movies.anymovies.feature.movies.data.repository.MovieRepositoryImpl
import com.movies.anymovies.feature.movies.domain.repository.MovieRepository
import com.movies.anymovies.feature.movies.domain.usecase.DiscoverMoviesUseCase
import com.movies.anymovies.feature.movies.domain.usecase.LoadNextMoviesPageUseCase
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.Retrofit

val moviesDataModule: Module = module {
    single<DiscoverMovieApi> { get<Retrofit>().create(DiscoverMovieApi::class.java) }
    single { get<AppDatabase>().movieDao() }
    single<MovieRepository> {
        MovieRepositoryImpl(
            discoverMovieApi = get(),
            movieDao = get(),
            dispatchers = get(),
        )
    }
    factory { DiscoverMoviesUseCase(movieRepository = get()) }
    factory { LoadNextMoviesPageUseCase(movieRepository = get()) }
}
