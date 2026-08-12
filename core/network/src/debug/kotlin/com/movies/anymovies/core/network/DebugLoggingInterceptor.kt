package com.movies.anymovies.core.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

internal fun OkHttpClient.Builder.addDebugLoggingInterceptor(): OkHttpClient.Builder {
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    return addInterceptor(loggingInterceptor)
}
