package com.movies.anymovies.feature.movies.presentation.di

import com.movies.anymovies.feature.movies.presentation.MovieListViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val moviesPresentationModule: Module = module {
    viewModel { params ->
        MovieListViewModel(
            genreId = params.get(),
            discoverMoviesUseCase = get(),
            loadNextMoviesPageUseCase = get(),
        )
    }
}
