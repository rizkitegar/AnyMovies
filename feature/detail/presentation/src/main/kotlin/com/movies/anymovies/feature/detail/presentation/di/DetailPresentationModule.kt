package com.movies.anymovies.feature.detail.presentation.di

import com.movies.anymovies.feature.detail.presentation.MovieDetailViewModel
import com.movies.anymovies.feature.detail.presentation.MovieReviewsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val detailPresentationModule: Module = module {
    viewModel { params ->
        MovieDetailViewModel(
            movieId = params.get(),
            getMovieDetailUseCase = get(),
        )
    }
    viewModel { params ->
        MovieReviewsViewModel(
            movieId = params.get(),
            getReviewsUseCase = get(),
        )
    }
}
