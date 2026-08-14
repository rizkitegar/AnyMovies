package com.movies.anymovies.feature.movies.presentation

import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.feature.movies.presentation.model.MovieUiModel

sealed interface MovieListUiState {
    data object Loading : MovieListUiState

    data class Success(
        val movies: List<MovieUiModel>,
        val isAppending: Boolean = false,
        val appendError: DomainError? = null,
        val endReached: Boolean = false,
    ) : MovieListUiState

    data object Empty : MovieListUiState

    data class Error(val error: DomainError) : MovieListUiState
}
