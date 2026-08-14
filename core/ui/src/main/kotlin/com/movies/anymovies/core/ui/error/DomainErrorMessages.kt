package com.movies.anymovies.core.ui.error

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.ui.R

@Composable
fun DomainError.toUserMessage(): String {
    val resId = when (this) {
        DomainError.NoConnection -> R.string.core_ui_error_no_connection
        DomainError.Timeout -> R.string.core_ui_error_timeout
        DomainError.Unauthorized -> R.string.core_ui_error_unauthorized
        DomainError.NotFound -> R.string.core_ui_error_not_found
        DomainError.RateLimited -> R.string.core_ui_error_rate_limited
        DomainError.Server -> R.string.core_ui_error_server
        DomainError.Parse -> R.string.core_ui_error_parse
        DomainError.LocalStorage -> R.string.core_ui_error_local_storage
        DomainError.Unknown -> R.string.core_ui_error_unknown
    }
    return stringResource(resId)
}
