package com.movies.anymovies.feature.genre.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.genre.domain.model.Genre
import com.movies.anymovies.feature.genre.domain.usecase.GetGenresUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalCoroutinesApi::class)
class GenreListViewModel(
    private val getGenresUseCase: GetGenresUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<GenreListUiState>(GenreListUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val reloadSignal = MutableSharedFlow<Unit>(replay = 1)

    init {
        reloadSignal.tryEmit(Unit)
        reloadSignal
            .flatMapLatest { getGenresUseCase() }
            .onEach { result -> _uiState.value = result.toUiState() }
            .launchIn(viewModelScope)
    }

    fun onRetry() {
        _uiState.value = GenreListUiState.Loading
        reloadSignal.tryEmit(Unit)
    }

    private fun Result<List<Genre>>.toUiState(): GenreListUiState {
        return when (this) {
            is Result.Success -> if (data.isEmpty()) {
                GenreListUiState.Empty
            } else {
                GenreListUiState.Success(genres = data)
            }
            is Result.Error -> GenreListUiState.Error(error = error)
        }
    }
}
