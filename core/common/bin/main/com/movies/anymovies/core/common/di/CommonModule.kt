package com.movies.anymovies.core.common.di

import com.movies.anymovies.core.common.dispatchers.CoroutineDispatchers
import com.movies.anymovies.core.common.dispatchers.DefaultCoroutineDispatchers
import org.koin.core.module.Module
import org.koin.dsl.module

public val commonModule: Module = module {
    single<CoroutineDispatchers> { DefaultCoroutineDispatchers() }
}
