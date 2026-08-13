package com.movies.anymovies.feature.detail.domain.usecase

import androidx.paging.PagingData
import com.movies.anymovies.feature.detail.domain.model.Review
import com.movies.anymovies.feature.detail.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow

public class GetReviewsUseCase(
    private val reviewRepository: ReviewRepository,
) {
    public operator fun invoke(movieId: Int): Flow<PagingData<Review>> =
        reviewRepository.getReviewsPagingData(movieId)
}
