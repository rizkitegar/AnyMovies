package com.movies.anymovies.core.network

import okhttp3.OkHttpClient

internal fun OkHttpClient.Builder.addDebugLoggingInterceptor(): OkHttpClient.Builder = this
