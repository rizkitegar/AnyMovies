package com.movies.anymovies.feature.detail.presentation

import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.detail.domain.model.PagedResult
import com.movies.anymovies.feature.detail.domain.model.Review
import com.movies.anymovies.feature.detail.domain.repository.ReviewRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal class FakeReviewRepository : ReviewRepository {

    private val observeQueue = ArrayDeque<Flow<Result<PagedResult<Review>>>>()
    private val loadNextPageResults = ArrayDeque<Result<Unit>>()
    private val loadNextPageGate = Channel<Unit>(Channel.UNLIMITED)

    var suspendLoadNextPage: Boolean = false
    var loadNextPageCallCount: Int = 0
        private set

    fun enqueueObserveResult(flow: Flow<Result<PagedResult<Review>>>) {
        observeQueue.addLast(flow)
    }

    fun enqueueLoadNextPageResult(result: Result<Unit>) {
        loadNextPageResults.addLast(result)
    }

    fun releaseLoadNextPage() {
        loadNextPageGate.trySend(Unit)
    }

    override fun observeReviews(movieId: Int): Flow<Result<PagedResult<Review>>> {
        return observeQueue.removeFirstOrNull() ?: emptyFlow()
    }

    override suspend fun loadNextPage(movieId: Int): Result<Unit> {
        loadNextPageCallCount++
        if (suspendLoadNextPage) {
            loadNextPageGate.receive()
        }
        return loadNextPageResults.removeFirstOrNull() ?: Result.Success(Unit)
    }
}
