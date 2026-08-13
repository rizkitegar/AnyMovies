package com.movies.anymovies.core.network.error

import android.database.sqlite.SQLiteException
import com.movies.anymovies.core.common.error.DomainError
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

public fun Throwable.toDomainError(): DomainError {
    return when (this) {
        is SocketTimeoutException -> DomainError.Timeout
        is UnknownHostException, is ConnectException -> DomainError.NoConnection
        is SerializationException -> DomainError.Parse
        is HttpException -> code().toHttpDomainError()
        is SQLiteException -> DomainError.LocalStorage
        else -> DomainError.Unknown
    }
}

private fun Int.toHttpDomainError(): DomainError {
    return when (this) {
        401, 403 -> DomainError.Unauthorized
        404 -> DomainError.NotFound
        429 -> DomainError.RateLimited
        in 500..599 -> DomainError.Server
        else -> DomainError.Unknown
    }
}
