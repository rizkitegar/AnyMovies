package com.movies.anymovies.core.common.error

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class DomainErrorTest {

    @ParameterizedTest
    @MethodSource("errorToMessageCases")
    fun `toUserMessage returns exact copy`(error: DomainError, expected: String) {
        assertEquals(expected, error.toUserMessage())
    }

    @Test
    fun `every DomainError subtype is covered by the mapping test cases`() {
        val coveredCount = errorToMessageCases().size
        val declaredCount = listOf(
            DomainError.NoConnection,
            DomainError.Timeout,
            DomainError.Unauthorized,
            DomainError.NotFound,
            DomainError.RateLimited,
            DomainError.Server,
            DomainError.Parse,
            DomainError.LocalStorage,
            DomainError.Unknown,
        ).size
        assertEquals(declaredCount, coveredCount)
    }

    companion object {
        @JvmStatic
        fun errorToMessageCases() = listOf(
            org.junit.jupiter.params.provider.Arguments.of(
                DomainError.NoConnection,
                "No internet connection. Check your network and try again.",
            ),
            org.junit.jupiter.params.provider.Arguments.of(
                DomainError.Timeout,
                "The request took too long. Please try again.",
            ),
            org.junit.jupiter.params.provider.Arguments.of(
                DomainError.Unauthorized,
                "We can't reach the movie service right now.",
            ),
            org.junit.jupiter.params.provider.Arguments.of(
                DomainError.NotFound,
                "This movie isn't available anymore.",
            ),
            org.junit.jupiter.params.provider.Arguments.of(
                DomainError.RateLimited,
                "Too many requests. Please wait a moment.",
            ),
            org.junit.jupiter.params.provider.Arguments.of(
                DomainError.Server,
                "The movie service is having trouble. Try again shortly.",
            ),
            org.junit.jupiter.params.provider.Arguments.of(
                DomainError.Parse,
                "Something went wrong loading this content.",
            ),
            org.junit.jupiter.params.provider.Arguments.of(
                DomainError.LocalStorage,
                "Couldn't load saved data.",
            ),
            org.junit.jupiter.params.provider.Arguments.of(
                DomainError.Unknown,
                "Something went wrong. Please try again.",
            ),
        )
    }
}
