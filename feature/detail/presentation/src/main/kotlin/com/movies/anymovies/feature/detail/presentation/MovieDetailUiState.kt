package com.movies.anymovies.feature.detail.presentation

import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.feature.detail.presentation.model.MovieDetailUiModel

public sealed interface MovieDetailUiState {

    public data object Loading : MovieDetailUiState

    public data class Success(
        val movie: MovieDetailUiModel,
    ) : MovieDetailUiState

    public data class Error(
        val error: DomainError,
        val isRetryable: Boolean,
    ) : MovieDetailUiState
}
