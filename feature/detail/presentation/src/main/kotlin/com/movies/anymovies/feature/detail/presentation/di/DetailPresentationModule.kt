package com.movies.anymovies.feature.detail.presentation.di

import com.movies.anymovies.feature.detail.presentation.MovieDetailViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

public val detailPresentationModule: Module = module {
    viewModel { params ->
        MovieDetailViewModel(
            movieId = params.get(),
            getMovieDetailUseCase = get(),
        )
    }
}
