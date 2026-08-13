package com.movies.anymovies.feature.detail.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.movies.anymovies.core.common.dispatchers.CoroutineDispatchers
import com.movies.anymovies.core.network.error.toDomainError
import com.movies.anymovies.feature.detail.data.mapper.toDomain
import com.movies.anymovies.feature.detail.data.remote.ReviewApi
import com.movies.anymovies.feature.detail.domain.model.Review
import com.movies.anymovies.feature.detail.domain.model.ReviewLoadException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

internal class ReviewPagingSource(
    private val reviewApi: ReviewApi,
    private val dispatchers: CoroutineDispatchers,
    private val movieId: Int,
) : PagingSource<Int, Review>() {

    override fun getRefreshKey(state: PagingState<Int, Review>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Review> {
        val page = params.key ?: FIRST_PAGE
        return try {
            val response = withContext(dispatchers.io) {
                reviewApi.getReviews(movieId = movieId, page = page)
            }
            val isLastPage = page >= response.totalPages || page >= MAX_PAGE
            LoadResult.Page(
                data = response.results.map { it.toDomain() },
                prevKey = if (page == FIRST_PAGE) null else page - 1,
                nextKey = if (isLastPage) null else page + 1,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoadResult.Error(ReviewLoadException(e.toDomainError()))
        }
    }

    private companion object {
        const val FIRST_PAGE = 1

        // TMDB hard-caps the reviews endpoint at page 500 regardless of total_pages.
        const val MAX_PAGE = 500
    }
}
