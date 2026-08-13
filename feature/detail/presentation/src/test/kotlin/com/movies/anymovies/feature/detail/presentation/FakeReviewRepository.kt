package com.movies.anymovies.feature.detail.presentation

import androidx.paging.PagingData
import com.movies.anymovies.feature.detail.domain.model.Review
import com.movies.anymovies.feature.detail.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class FakeReviewRepository : ReviewRepository {

    var pagingDataFlow: Flow<PagingData<Review>> = flowOf(PagingData.empty())

    override fun getReviewsPagingData(movieId: Int): Flow<PagingData<Review>> = pagingDataFlow
}
