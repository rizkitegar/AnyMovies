package com.movies.anymovies.feature.detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.detail.domain.model.PagedResult
import com.movies.anymovies.feature.detail.domain.model.Review
import com.movies.anymovies.feature.detail.domain.usecase.GetReviewsUseCase
import com.movies.anymovies.feature.detail.domain.usecase.LoadNextReviewsPageUseCase
import com.movies.anymovies.feature.detail.presentation.model.toUiModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
public class MovieReviewsViewModel(
    private val movieId: Int,
    private val getReviewsUseCase: GetReviewsUseCase,
    private val loadNextReviewsPageUseCase: LoadNextReviewsPageUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MovieReviewsUiState>(MovieReviewsUiState.Loading)
    public val uiState = _uiState.asStateFlow()

    private val reloadSignal = MutableSharedFlow<Unit>(replay = 1)

    init {
        reloadSignal.tryEmit(Unit)
        reloadSignal
            .flatMapLatest { getReviewsUseCase(movieId) }
            .onEach(::handleObserveResult)
            .launchIn(viewModelScope)
    }

    public fun onRetry() {
        _uiState.value = MovieReviewsUiState.Loading
        reloadSignal.tryEmit(Unit)
    }

    public fun loadNextPage() {
        val current = _uiState.value
        if (current !is MovieReviewsUiState.Success) return
        if (current.isAppending || current.endReached) return
        _uiState.value = current.copy(isAppending = true, appendError = null)
        viewModelScope.launch {
            val result = loadNextReviewsPageUseCase(movieId)
            if (result is Result.Error) {
                val latest = _uiState.value
                if (latest is MovieReviewsUiState.Success) {
                    _uiState.value = latest.copy(isAppending = false, appendError = result.error)
                }
            }
        }
    }

    public fun onRetryAppend() {
        val current = _uiState.value
        if (current !is MovieReviewsUiState.Success || current.appendError == null) return
        loadNextPage()
    }

    private fun handleObserveResult(result: Result<PagedResult<Review>>) {
        _uiState.value = when (result) {
            is Result.Success -> {
                val paged = result.data
                if (paged.items.isEmpty()) {
                    MovieReviewsUiState.Empty
                } else {
                    MovieReviewsUiState.Success(
                        reviews = paged.items.map { it.toUiModel() }.toImmutableList(),
                        isAppending = false,
                        appendError = null,
                        endReached = paged.totalPages > 0 && paged.page >= paged.totalPages,
                    )
                }
            }
            is Result.Error -> {
                val current = _uiState.value
                if (current is MovieReviewsUiState.Success) {
                    current.copy(isAppending = false, appendError = result.error)
                } else {
                    MovieReviewsUiState.Error(result.error)
                }
            }
        }
    }
}
