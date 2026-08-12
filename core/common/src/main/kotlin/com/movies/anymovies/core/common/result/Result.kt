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

public inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> this
    }
}

public inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

public inline fun <T> Result<T>.onError(action: (DomainError) -> Unit): Result<T> {
    if (this is Result.Error) action(error)
    return this
}

public fun <T> Result<T>.getOrNull(): T? {
    return (this as? Result.Success)?.data
}
