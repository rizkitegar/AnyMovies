package com.movies.anymovies.feature.movies.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.movies.domain.model.Movie
import com.movies.anymovies.feature.movies.domain.model.PagedResult
import com.movies.anymovies.feature.movies.domain.usecase.DiscoverMoviesUseCase
import com.movies.anymovies.feature.movies.domain.usecase.LoadNextMoviesPageUseCase
import com.movies.anymovies.feature.movies.presentation.model.toUiModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
public class MovieListViewModel(
    private val genreId: Int,
    private val discoverMoviesUseCase: DiscoverMoviesUseCase,
    private val loadNextMoviesPageUseCase: LoadNextMoviesPageUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MovieListUiState>(MovieListUiState.Loading)
    public val uiState = _uiState.asStateFlow()

    private val reloadSignal = MutableSharedFlow<Unit>(replay = 1)

    init {
        reloadSignal.tryEmit(Unit)
        reloadSignal
            .flatMapLatest { discoverMoviesUseCase(genreId) }
            .onEach { result -> handleObserveResult(result) }
            .launchIn(viewModelScope)
    }

    public fun onRetry() {
        _uiState.value = MovieListUiState.Loading
        reloadSignal.tryEmit(Unit)
    }

    public fun loadNextPage() {
        val current = _uiState.value
        if (current !is MovieListUiState.Success) return
        if (current.isAppending || current.endReached) return
        _uiState.value = current.copy(isAppending = true, appendError = null)
        viewModelScope.launch {
            val result = loadNextMoviesPageUseCase(genreId)
            if (result is Result.Error) {
                val latest = _uiState.value
                if (latest is MovieListUiState.Success) {
                    _uiState.value = latest.copy(isAppending = false, appendError = result.error)
                }
            }
        }
    }

    public fun onRetryAppend() {
        val current = _uiState.value
        if (current !is MovieListUiState.Success || current.appendError == null) return
        loadNextPage()
    }

    private fun handleObserveResult(result: Result<PagedResult<Movie>>) {
        _uiState.value = when (result) {
            is Result.Success -> {
                val paged = result.data
                if (paged.items.isEmpty()) {
                    MovieListUiState.Empty
                } else {
                    MovieListUiState.Success(
                        movies = paged.items.map { it.toUiModel() },
                        isAppending = false,
                        appendError = null,
                        endReached = paged.totalPages > 0 && paged.page >= paged.totalPages,
                    )
                }
            }
            is Result.Error -> {
                val current = _uiState.value
                if (current is MovieListUiState.Success) {
                    current.copy(isAppending = false, appendError = result.error)
                } else {
                    MovieListUiState.Error(result.error)
                }
            }
        }
    }
}
