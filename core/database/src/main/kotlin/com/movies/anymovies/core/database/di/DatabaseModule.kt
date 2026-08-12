package com.movies.anymovies.core.database.di

import com.movies.anymovies.core.database.AppDatabase
import com.movies.anymovies.core.database.createAppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

public val databaseModule: Module = module {
    single<AppDatabase> { createAppDatabase(androidContext()) }
}
