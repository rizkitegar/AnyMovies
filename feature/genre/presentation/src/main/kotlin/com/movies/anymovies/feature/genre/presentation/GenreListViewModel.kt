package com.movies.anymovies.feature.genre.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.genre.domain.model.Genre
import com.movies.anymovies.feature.genre.domain.usecase.GetGenresUseCase
import com.movies.anymovies.feature.genre.domain.usecase.RefreshGenresUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
public class GenreListViewModel(
    private val getGenresUseCase: GetGenresUseCase,
    private val refreshGenresUseCase: RefreshGenresUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<GenreListUiState>(GenreListUiState.Loading)
    public val uiState = _uiState.asStateFlow()

    private val reloadSignal = MutableSharedFlow<Unit>(replay = 1)

    init {
        reloadSignal.tryEmit(Unit)
        reloadSignal
            .flatMapLatest { getGenresUseCase() }
            .onEach { result -> _uiState.value = result.toUiState() }
            .launchIn(viewModelScope)
    }

    public fun onRetry() {
        _uiState.value = GenreListUiState.Loading
        reloadSignal.tryEmit(Unit)
    }

    public fun onRefresh() {
        val current = _uiState.value
        if (current !is GenreListUiState.Success) return
        _uiState.value = current.copy(isRefreshing = true)
        viewModelScope.launch {
            refreshGenresUseCase()
            val latest = _uiState.value
            if (latest is GenreListUiState.Success) {
                _uiState.value = latest.copy(isRefreshing = false)
            }
        }
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
