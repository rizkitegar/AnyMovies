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
