package com.movies.anymovies.core.uilegacy.error

import android.content.Context
import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.uilegacy.R

public fun DomainError.toUserMessage(context: Context): String {
    val resId = when (this) {
        DomainError.NoConnection -> R.string.core_ui_legacy_error_no_connection
        DomainError.Timeout -> R.string.core_ui_legacy_error_timeout
        DomainError.Unauthorized -> R.string.core_ui_legacy_error_unauthorized
        DomainError.NotFound -> R.string.core_ui_legacy_error_not_found
        DomainError.RateLimited -> R.string.core_ui_legacy_error_rate_limited
        DomainError.Server -> R.string.core_ui_legacy_error_server
        DomainError.Parse -> R.string.core_ui_legacy_error_parse
        DomainError.LocalStorage -> R.string.core_ui_legacy_error_local_storage
        DomainError.Unknown -> R.string.core_ui_legacy_error_unknown
    }
    return context.getString(resId)
}
