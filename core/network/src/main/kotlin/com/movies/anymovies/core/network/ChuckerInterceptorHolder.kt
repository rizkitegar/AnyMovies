package com.movies.anymovies.core.network

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Interceptor

internal object ChuckerInterceptorHolder {

    @Volatile
    private var cached: Interceptor? = null

    @OptIn(DelicateCoroutinesApi::class)
    private val warmUpScope: CoroutineScope = GlobalScope

    fun warmUp(context: Context) {
        if (cached != null) return
        val appContext = context.applicationContext
        warmUpScope.launch(Dispatchers.Default) {
            cached = build(appContext)
        }
    }

    fun get(context: Context): Interceptor {
        return cached ?: build(context.applicationContext).also { cached = it }
    }

    private fun build(context: Context): Interceptor {
        val chuckerCollector = ChuckerCollector(
            context = context,
            showNotification = true,
            retentionPeriod = RetentionManager.Period.ONE_WEEK,
        )
        return ChuckerInterceptor.Builder(context)
            .collector(chuckerCollector)
            .redactHeaders("Authorization", "api_key")
            .build()
    }
}
