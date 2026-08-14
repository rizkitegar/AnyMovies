package com.movies.anymovies.feature.genre.presentation

import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.feature.genre.domain.model.Genre

sealed interface GenreListUiState {

    data object Loading : GenreListUiState

    data class Success(
        val genres: List<Genre>,
    ) : GenreListUiState

    data object Empty : GenreListUiState

    data class Error(
        val error: DomainError,
    ) : GenreListUiState
}
