package com.movies.anymovies.feature.detail.presentation

import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.feature.detail.presentation.model.ReviewUiModel
import kotlinx.collections.immutable.ImmutableList

public sealed interface MovieReviewsUiState {
    public data object Loading : MovieReviewsUiState

    public data class Success(
        val reviews: ImmutableList<ReviewUiModel>,
        val isAppending: Boolean = false,
        val appendError: DomainError? = null,
        val endReached: Boolean = false,
    ) : MovieReviewsUiState

    public data object Empty : MovieReviewsUiState

    public data class Error(val error: DomainError) : MovieReviewsUiState
}
