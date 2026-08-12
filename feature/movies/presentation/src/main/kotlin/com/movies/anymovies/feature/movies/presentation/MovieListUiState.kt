package com.movies.anymovies.feature.movies.presentation

import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.feature.movies.presentation.model.MovieUiModel

public sealed interface MovieListUiState {
    public data object Loading : MovieListUiState

    public data class Success(
        val movies: List<MovieUiModel>,
        val isAppending: Boolean = false,
        val appendError: DomainError? = null,
        val endReached: Boolean = false,
    ) : MovieListUiState

    public data object Empty : MovieListUiState

    public data class Error(val error: DomainError) : MovieListUiState
}
