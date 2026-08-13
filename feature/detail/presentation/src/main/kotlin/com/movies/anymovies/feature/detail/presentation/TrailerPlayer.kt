package com.movies.anymovies.feature.detail.presentation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

internal const val COULD_NOT_OPEN_YOUTUBE_MESSAGE = "Couldn't open YouTube. No app found."

/** Fires the YouTube deep link; returns false if no app can handle it. */
internal fun launchYoutube(context: Context, videoKey: String): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, "https://www.youtube.com/watch?v=$videoKey".toUri())
    return try {
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}
