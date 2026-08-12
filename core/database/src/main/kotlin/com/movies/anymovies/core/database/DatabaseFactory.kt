package com.movies.anymovies.core.database

import android.content.Context
import androidx.room.Room

public fun createAppDatabase(context: Context): AppDatabase {
    return Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        DATABASE_NAME,
    ).build()
}
