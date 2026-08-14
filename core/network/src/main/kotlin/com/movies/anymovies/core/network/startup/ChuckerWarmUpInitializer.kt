package com.movies.anymovies.core.network.startup

import android.content.Context
import androidx.startup.Initializer
import com.movies.anymovies.core.network.ChuckerInterceptorHolder

class ChuckerWarmUpInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        ChuckerInterceptorHolder.warmUp(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
