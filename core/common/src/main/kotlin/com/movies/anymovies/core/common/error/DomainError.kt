package com.movies.anymovies.core.common.error

/**
 * Error taxonomy for the app. Data-layer failures (exceptions, HTTP codes)
 * are mapped to exactly one of these before crossing into `domain`/`presentation`.
 */
public sealed class DomainError {
    public data object NoConnection : DomainError()
    public data object Timeout : DomainError()
    public data object Unauthorized : DomainError()
    public data object NotFound : DomainError()
    public data object RateLimited : DomainError()
    public data object Server : DomainError()
    public data object Parse : DomainError()
    public data object LocalStorage : DomainError()
    public data object Unknown : DomainError()
}

/**
 * Exact user-facing strings — never expose HTTP codes, stack traces, or raw
 * payloads to the UI.
 */
public fun DomainError.toUserMessage(): String {
    return when (this) {
        is DomainError.NoConnection -> "No internet connection. Check your network and try again."
        is DomainError.Timeout -> "The request took too long. Please try again."
        is DomainError.Unauthorized -> "We can't reach the movie service right now."
        is DomainError.NotFound -> "This movie isn't available anymore."
        is DomainError.RateLimited -> "Too many requests. Please wait a moment."
        is DomainError.Server -> "The movie service is having trouble. Try again shortly."
        is DomainError.Parse -> "Something went wrong loading this content."
        is DomainError.LocalStorage -> "Couldn't load saved data."
        is DomainError.Unknown -> "Something went wrong. Please try again."
    }
}
