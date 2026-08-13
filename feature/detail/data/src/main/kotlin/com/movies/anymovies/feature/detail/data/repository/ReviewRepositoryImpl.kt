package com.movies.anymovies.feature.detail.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.filter
import com.movies.anymovies.core.common.dispatchers.CoroutineDispatchers
import com.movies.anymovies.feature.detail.data.paging.ReviewPagingSource
import com.movies.anymovies.feature.detail.data.remote.ReviewApi
import com.movies.anymovies.feature.detail.domain.model.Review
import com.movies.anymovies.feature.detail.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class ReviewRepositoryImpl(
    private val reviewApi: ReviewApi,
    private val dispatchers: CoroutineDispatchers,
) : ReviewRepository {

    override fun getReviewsPagingData(movieId: Int): Flow<PagingData<Review>> =
        Pager(
            config = PagingConfig(
                pageSize = REVIEWS_PAGE_SIZE,
                initialLoadSize = REVIEWS_PAGE_SIZE,
                prefetchDistance = APPEND_PREFETCH_DISTANCE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                ReviewPagingSource(reviewApi = reviewApi, dispatchers = dispatchers, movieId = movieId)
            },
        ).flow.map { pagingData ->
            // PagingSource pages are independent network calls; TMDB can return the
            // same review on adjacent pages, so re-dedupe by id per paging session.
            val seenIds = HashSet<String>()
            pagingData.filter { review -> seenIds.add(review.id) }
        }

    private companion object {
        const val REVIEWS_PAGE_SIZE = 20
        const val APPEND_PREFETCH_DISTANCE = 4
    }
}
