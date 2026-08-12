package com.movies.anymovies.di

import com.movies.anymovies.core.common.di.commonModule
import com.movies.anymovies.core.database.di.databaseModule
import com.movies.anymovies.core.network.di.networkModule
import com.movies.anymovies.feature.genre.data.di.genreDataModule
import com.movies.anymovies.feature.genre.presentation.di.genrePresentationModule
import org.koin.core.module.Module

val appModules: List<Module> = listOf(
    commonModule,
    networkModule,
    databaseModule,
    genreDataModule,
    genrePresentationModule,
)
