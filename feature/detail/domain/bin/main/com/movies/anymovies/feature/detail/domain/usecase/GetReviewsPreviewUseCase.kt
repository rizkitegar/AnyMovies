package com.movies.anymovies.feature.detail.domain.usecase

import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.detail.domain.model.PagedResult
import com.movies.anymovies.feature.detail.domain.model.Review
import com.movies.anymovies.feature.detail.domain.repository.ReviewRepository

public class GetReviewsPreviewUseCase(
    private val reviewRepository: ReviewRepository,
) {
    public suspend operator fun invoke(movieId: Int): Result<PagedResult<Review>> =
        reviewRepository.getReviewsPreview(movieId)
}
