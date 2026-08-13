package com.movies.anymovies.feature.detail.domain.usecase

import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.detail.domain.repository.ReviewRepository

public class LoadNextReviewsPageUseCase(
    private val reviewRepository: ReviewRepository,
) {
    public suspend operator fun invoke(movieId: Int): Result<Unit> = reviewRepository.loadNextPage(movieId)
}
