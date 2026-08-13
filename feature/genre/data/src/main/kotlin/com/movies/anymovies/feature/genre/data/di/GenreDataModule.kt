package com.movies.anymovies.feature.genre.data.di

import com.movies.anymovies.core.database.AppDatabase
import com.movies.anymovies.feature.genre.data.remote.GenreApi
import com.movies.anymovies.feature.genre.data.repository.GenreRepositoryImpl
import com.movies.anymovies.feature.genre.domain.repository.GenreRepository
import com.movies.anymovies.feature.genre.domain.usecase.GetGenresUseCase
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.Retrofit

public val genreDataModule: Module = module {
    single<GenreApi> { get<Retrofit>().create(GenreApi::class.java) }
    single { get<AppDatabase>().genreDao() }
    single<GenreRepository> {
        GenreRepositoryImpl(
            genreApi = get(),
            genreDao = get(),
            dispatchers = get(),
        )
    }
    factory { GetGenresUseCase(genreRepository = get()) }
}
