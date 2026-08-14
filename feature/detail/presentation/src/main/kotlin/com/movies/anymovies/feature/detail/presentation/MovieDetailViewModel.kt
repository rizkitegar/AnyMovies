package com.movies.anymovies.feature.detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.detail.domain.model.MovieDetail
import com.movies.anymovies.feature.detail.domain.usecase.GetMovieDetailUseCase
import com.movies.anymovies.feature.detail.presentation.model.toUiModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailViewModel(
    private val movieId: Int,
    private val getMovieDetailUseCase: GetMovieDetailUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MovieDetailUiState>(MovieDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val reloadSignal = MutableSharedFlow<Unit>(replay = 1)

    init {
        reloadSignal.tryEmit(Unit)
        reloadSignal
            .flatMapLatest { getMovieDetailUseCase(movieId) }
            .onEach(::handleResult)
            .launchIn(viewModelScope)
    }

    fun onRetry() {
        _uiState.value = MovieDetailUiState.Loading
        reloadSignal.tryEmit(Unit)
    }

    private fun handleResult(result: Result<MovieDetail>) {
        _uiState.value = when (result) {
            is Result.Success -> MovieDetailUiState.Success(movie = result.data.toUiModel())
            is Result.Error -> MovieDetailUiState.Error(
                error = result.error,
                isRetryable = result.error != DomainError.NotFound,
            )
        }
    }
}
