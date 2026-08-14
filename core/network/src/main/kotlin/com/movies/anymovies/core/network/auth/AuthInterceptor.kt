package com.movies.anymovies.core.network.auth

import com.movies.anymovies.core.network.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val authenticatedRequest = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${BuildConfig.TMDB_API_KEY}")
            .build()
        return chain.proceed(authenticatedRequest)
    }
}
