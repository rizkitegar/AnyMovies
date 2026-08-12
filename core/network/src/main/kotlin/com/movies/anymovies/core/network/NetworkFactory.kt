package com.movies.anymovies.core.network

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.movies.anymovies.core.network.auth.AuthInterceptor
import com.movies.anymovies.core.network.json.networkJson
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

public fun createOkHttpClient(context: Context): OkHttpClient {
    val chuckerCollector = ChuckerCollector(
        context = context,
        showNotification = true,
        retentionPeriod = RetentionManager.Period.ONE_WEEK,
    )
    val chuckerInterceptor = ChuckerInterceptor.Builder(context)
        .collector(chuckerCollector)
        .redactHeaders("Authorization", "api_key")
        .build()

    return OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .addInterceptor(chuckerInterceptor)
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
