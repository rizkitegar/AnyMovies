package com.movies.anymovies.feature.genre.presentation.di

import com.movies.anymovies.feature.genre.presentation.GenreListViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

public val genrePresentationModule: Module = module {
    viewModelOf(::GenreListViewModel)
}
