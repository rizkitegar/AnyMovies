package com.movies.anymovies.core.network.error

import com.movies.anymovies.core.common.error.DomainError
import kotlinx.serialization.SerializationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ThrowableToDomainErrorMapperTest {

    @ParameterizedTest
    @MethodSource("throwableToDomainErrorCases")
    fun `toDomainError maps every case`(throwable: Throwable, expected: DomainError) {
        assertEquals(expected, throwable.toDomainError())
    }

    companion object {
        private fun httpException(code: Int): HttpException {
            val body = "".toResponseBody("application/json".toMediaType())
            return HttpException(Response.error<Any>(code, body))
        }

        @JvmStatic
        fun throwableToDomainErrorCases() = listOf(
            Arguments.of(SocketTimeoutException(), DomainError.Timeout),
            Arguments.of(UnknownHostException(), DomainError.NoConnection),
            Arguments.of(ConnectException(), DomainError.NoConnection),
            Arguments.of(SerializationException(), DomainError.Parse),
            Arguments.of(httpException(401), DomainError.Unauthorized),
            Arguments.of(httpException(403), DomainError.Unauthorized),
            Arguments.of(httpException(404), DomainError.NotFound),
            Arguments.of(httpException(429), DomainError.RateLimited),
            Arguments.of(httpException(500), DomainError.Server),
            Arguments.of(httpException(503), DomainError.Server),
            Arguments.of(httpException(418), DomainError.Unknown),
            Arguments.of(IllegalStateException(), DomainError.Unknown),
        )
    }
}
