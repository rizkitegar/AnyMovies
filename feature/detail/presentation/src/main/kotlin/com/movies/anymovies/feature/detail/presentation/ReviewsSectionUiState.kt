package com.movies.anymovies.feature.detail.presentation

import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.feature.detail.presentation.model.ReviewUiModel
import kotlinx.collections.immutable.ImmutableList

public sealed interface ReviewsSectionUiState {
    public data object Loading : ReviewsSectionUiState

    public data class Success(
        val reviews: ImmutableList<ReviewUiModel>,
        val totalResults: Int,
    ) : ReviewsSectionUiState

    public data object Empty : ReviewsSectionUiState

    public data class Error(val error: DomainError) : ReviewsSectionUiState
}
