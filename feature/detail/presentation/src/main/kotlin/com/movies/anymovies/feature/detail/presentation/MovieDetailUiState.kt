package com.movies.anymovies.feature.detail.presentation

import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.feature.detail.presentation.model.MovieDetailUiModel

sealed interface MovieDetailUiState {

    data object Loading : MovieDetailUiState

    data class Success(
        val movie: MovieDetailUiModel,
    ) : MovieDetailUiState

    data class Error(
        val error: DomainError,
        val isRetryable: Boolean,
    ) : MovieDetailUiState
}
