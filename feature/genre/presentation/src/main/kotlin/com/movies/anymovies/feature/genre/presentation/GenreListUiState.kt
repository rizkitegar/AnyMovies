package com.movies.anymovies.feature.genre.presentation

import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.feature.genre.domain.model.Genre

public sealed interface GenreListUiState {

    public data object Loading : GenreListUiState

    public data class Success(
        val genres: List<Genre>,
    ) : GenreListUiState

    public data object Empty : GenreListUiState

    public data class Error(
        val error: DomainError,
    ) : GenreListUiState
}
