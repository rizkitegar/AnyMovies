package com.movies.anymovies

import android.app.Application
import android.content.pm.ApplicationInfo
import com.movies.anymovies.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AnyMoviesApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        startKoin {
            if (isDebuggable) androidLogger()
            androidContext(this@AnyMoviesApplication)
            modules(appModules)
        }
    }
}
