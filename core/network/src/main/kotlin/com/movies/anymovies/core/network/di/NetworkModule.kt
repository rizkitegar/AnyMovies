package com.movies.anymovies.core.network.di

import com.movies.anymovies.core.network.createOkHttpClient
import com.movies.anymovies.core.network.createRetrofit
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.Retrofit

val networkModule: Module = module {
    single<OkHttpClient> { createOkHttpClient(androidContext()) }
    single<Retrofit> { createRetrofit(get()) }
}
