package com.movies.anymovies.feature.detail.data.di

import com.movies.anymovies.core.database.AppDatabase
import com.movies.anymovies.feature.detail.data.remote.MovieDetailApi
import com.movies.anymovies.feature.detail.data.repository.MovieDetailRepositoryImpl
import com.movies.anymovies.feature.detail.domain.repository.MovieDetailRepository
import com.movies.anymovies.feature.detail.domain.usecase.GetMovieDetailUseCase
import com.movies.anymovies.feature.detail.domain.usecase.SelectTrailerUseCase
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.Retrofit

public val detailDataModule: Module = module {
    single<MovieDetailApi> { get<Retrofit>().create(MovieDetailApi::class.java) }
    single { get<AppDatabase>().movieDetailDao() }
    single<MovieDetailRepository> {
        MovieDetailRepositoryImpl(
            movieDetailApi = get(),
            movieDetailDao = get(),
            dispatchers = get(),
        )
    }
    factory { GetMovieDetailUseCase(movieDetailRepository = get()) }
    factory { SelectTrailerUseCase() }
}
