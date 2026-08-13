package com.movies.anymovies.feature.detail.data.repository

import com.movies.anymovies.core.common.dispatchers.CoroutineDispatchers
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.core.network.error.toDomainError
import com.movies.anymovies.feature.detail.data.mapper.toDomain
import com.movies.anymovies.feature.detail.data.remote.ReviewApi
import com.movies.anymovies.feature.detail.domain.model.PagedResult
import com.movies.anymovies.feature.detail.domain.model.Review
import com.movies.anymovies.feature.detail.domain.repository.ReviewRepository
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext

internal class ReviewRepositoryImpl(
    private val reviewApi: ReviewApi,
    private val dispatchers: CoroutineDispatchers,
) : ReviewRepository {

    private data class Session(
        val items: List<Review> = emptyList(),
        val page: Int = 0,
        val totalPages: Int = 0,
        val totalResults: Int = 0,
    )

    private val sessions = ConcurrentHashMap<Int, MutableStateFlow<Session>>()
    private val movieMutexes = ConcurrentHashMap<Int, Mutex>()

    override fun observeReviews(movieId: Int): Flow<Result<PagedResult<Review>>> = flow {
        val session = sessionFor(movieId)
        if (session.value.page == 0) {
            val initialLoad = fetchPage(movieId = movieId, page = 1, replace = true)
            if (initialLoad is Result.Error) {
                emit(Result.Error(initialLoad.error))
                return@flow
            }
        }
        emitAll(session.map { Result.Success(it.toPagedResult()) })
    }

    override suspend fun loadNextPage(movieId: Int): Result<Unit> {
        val mutex = mutexFor(movieId)
        if (!mutex.tryLock()) {
            return Result.Success(Unit)
        }
        return try {
            val session = sessionFor(movieId).value
            val currentPage = session.page
            val isLastPage = currentPage != 0 && currentPage >= session.totalPages
            when {
                currentPage == 0 -> Result.Success(Unit)
                isLastPage || currentPage >= MAX_PAGE -> Result.Success(Unit)
                else -> fetchPage(movieId = movieId, page = currentPage + 1, replace = false)
            }
        } finally {
            mutex.unlock()
        }
    }

    override fun clear(movieId: Int) {
        sessions.remove(movieId)
        movieMutexes.remove(movieId)
    }

    private fun sessionFor(movieId: Int): MutableStateFlow<Session> =
        sessions.getOrPut(movieId) { MutableStateFlow(Session()) }

    private fun mutexFor(movieId: Int): Mutex = movieMutexes.getOrPut(movieId) { Mutex() }

    private suspend fun fetchPage(movieId: Int, page: Int, replace: Boolean): Result<Unit> {
        return try {
            val response = withContext(dispatchers.io) {
                reviewApi.getReviews(movieId = movieId, page = page)
            }
            val fetchedReviews = response.results.map { it.toDomain() }
            sessionFor(movieId).update { current ->
                val merged = if (replace) fetchedReviews else current.items + fetchedReviews
                Session(
                    items = merged.distinctBy { it.id },
                    page = page,
                    totalPages = response.totalPages,
                    totalResults = response.totalResults,
                )
            }
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.toDomainError())
        }
    }

    private fun Session.toPagedResult(): PagedResult<Review> = PagedResult(
        items = items,
        page = page,
        totalPages = totalPages,
        totalResults = totalResults,
    )

    private companion object {
        const val MAX_PAGE = 500
    }
}
