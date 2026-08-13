package com.movies.anymovies.feature.detail.domain.usecase

import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.detail.domain.model.PagedResult
import com.movies.anymovies.feature.detail.domain.model.Review
import com.movies.anymovies.feature.detail.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow

public class GetReviewsUseCase(
    private val reviewRepository: ReviewRepository,
) {
    public operator fun invoke(movieId: Int): Flow<Result<PagedResult<Review>>> =
        reviewRepository.observeReviews(movieId)
}
