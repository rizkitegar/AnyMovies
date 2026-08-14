package com.movies.anymovies.core.common.result

import com.movies.anymovies.core.common.error.DomainError

/**
 * Boundary-crossing result type. Every repository/use-case call returns this
 * instead of throwing, so exceptions never escape the `data` layer.
 */
public sealed interface Result<out T> {
    public data class Success<out T>(val data: T) : Result<T>
    public data class Error(val error: DomainError) : Result<Nothing>
}
