package com.movies.anymovies.feature.detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.movies.anymovies.feature.detail.domain.usecase.GetReviewsUseCase
import com.movies.anymovies.feature.detail.presentation.model.ReviewUiModel
import com.movies.anymovies.feature.detail.presentation.model.toUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class MovieReviewsViewModel(
    movieId: Int,
    getReviewsUseCase: GetReviewsUseCase,
) : ViewModel() {

    public val reviewsPagingData: Flow<PagingData<ReviewUiModel>> =
        getReviewsUseCase(movieId)
            .map { pagingData -> pagingData.map { review -> review.toUiModel() } }
            .cachedIn(viewModelScope)
}
