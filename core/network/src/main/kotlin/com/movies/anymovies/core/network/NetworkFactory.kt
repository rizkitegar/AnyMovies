package com.movies.anymovies.core.network

import android.content.Context
import com.movies.anymovies.core.network.auth.AuthInterceptor
import com.movies.anymovies.core.network.json.networkJson
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

public fun createOkHttpClient(context: Context): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .addInterceptor(ChuckerInterceptorHolder.get(context))
        .addDebugLoggingInterceptor()
        .build()
}

public fun createRetrofit(okHttpClient: OkHttpClient, json: Json = networkJson): Retrofit {
    return Retrofit.Builder()
        .baseUrl(TMDB_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
}
