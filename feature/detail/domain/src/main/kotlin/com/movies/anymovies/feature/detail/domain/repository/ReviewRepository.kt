package com.movies.anymovies.feature.detail.domain.repository

import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.detail.domain.model.PagedResult
import com.movies.anymovies.feature.detail.domain.model.Review
import kotlinx.coroutines.flow.Flow

public interface ReviewRepository {
    public fun observeReviews(movieId: Int): Flow<Result<PagedResult<Review>>>

    public suspend fun loadNextPage(movieId: Int): Result<Unit>
}
