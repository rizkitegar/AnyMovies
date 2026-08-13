package com.movies.anymovies.feature.detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.detail.domain.model.PagedResult
import com.movies.anymovies.feature.detail.domain.model.Review
import com.movies.anymovies.feature.detail.domain.usecase.GetReviewsUseCase
import com.movies.anymovies.feature.detail.presentation.model.toUiModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val INLINE_PREVIEW_COUNT = 3

@OptIn(ExperimentalCoroutinesApi::class)
public class ReviewsSectionViewModel(
    private val movieId: Int,
    private val getReviewsUseCase: GetReviewsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReviewsSectionUiState>(ReviewsSectionUiState.Loading)
    public val uiState = _uiState.asStateFlow()

    private val reloadSignal = MutableSharedFlow<Unit>(replay = 1)

    init {
        reloadSignal.tryEmit(Unit)
        reloadSignal
            .flatMapLatest { getReviewsUseCase(movieId) }
            .onEach(::handleResult)
            .launchIn(viewModelScope)
    }

    public fun onRetry() {
        _uiState.value = ReviewsSectionUiState.Loading
        reloadSignal.tryEmit(Unit)
    }

    private fun handleResult(result: Result<PagedResult<Review>>) {
        _uiState.value = when (result) {
            is Result.Success -> {
                val paged = result.data
                if (paged.items.isEmpty()) {
                    ReviewsSectionUiState.Empty
                } else {
                    ReviewsSectionUiState.Success(
                        reviews = paged.items.take(INLINE_PREVIEW_COUNT).map { it.toUiModel() }.toImmutableList(),
                        totalResults = paged.totalResults,
                    )
                }
            }
            is Result.Error -> ReviewsSectionUiState.Error(result.error)
        }
    }
}
