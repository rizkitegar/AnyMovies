package com.movies.anymovies.feature.detail.domain.model

import com.movies.anymovies.core.common.error.DomainError

/**
 * Carries a [DomainError] as a [Throwable] so it can travel through
 * `PagingSource.LoadResult.Error`/`LoadState.Error`, which require a `Throwable`.
 */
public class ReviewLoadException(public val domainError: DomainError) : Exception()
