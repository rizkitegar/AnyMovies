package com.movies.anymovies.feature.detail.data.di

import com.movies.anymovies.core.database.AppDatabase
import com.movies.anymovies.feature.detail.data.remote.MovieDetailApi
import com.movies.anymovies.feature.detail.data.remote.ReviewApi
import com.movies.anymovies.feature.detail.data.repository.MovieDetailRepositoryImpl
import com.movies.anymovies.feature.detail.data.repository.ReviewRepositoryImpl
import com.movies.anymovies.feature.detail.domain.repository.MovieDetailRepository
import com.movies.anymovies.feature.detail.domain.repository.ReviewRepository
import com.movies.anymovies.feature.detail.domain.usecase.GetMovieDetailUseCase
import com.movies.anymovies.feature.detail.domain.usecase.GetReviewsUseCase
import com.movies.anymovies.feature.detail.domain.usecase.LoadNextReviewsPageUseCase
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

    single<ReviewApi> { get<Retrofit>().create(ReviewApi::class.java) }
    single<ReviewRepository> {
        ReviewRepositoryImpl(
            reviewApi = get(),
            dispatchers = get(),
        )
    }
    factory { GetReviewsUseCase(reviewRepository = get()) }
    factory { LoadNextReviewsPageUseCase(reviewRepository = get()) }
}
