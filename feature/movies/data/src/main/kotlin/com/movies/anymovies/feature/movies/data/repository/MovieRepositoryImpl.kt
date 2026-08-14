package com.movies.anymovies.feature.movies.data.repository

import com.movies.anymovies.core.common.dispatchers.CoroutineDispatchers
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.core.database.movie.MovieDao
import com.movies.anymovies.core.database.movie.MovieEntity
import com.movies.anymovies.core.database.movie.MovieRemoteKeyEntity
import com.movies.anymovies.core.database.ttl.TtlPolicy
import com.movies.anymovies.core.network.error.toDomainError
import com.movies.anymovies.feature.movies.data.mapper.toDomain
import com.movies.anymovies.feature.movies.data.mapper.toEntity
import com.movies.anymovies.feature.movies.data.remote.DiscoverMovieApi
import com.movies.anymovies.feature.movies.domain.model.Movie
import com.movies.anymovies.feature.movies.domain.model.PagedResult
import com.movies.anymovies.feature.movies.domain.repository.MovieRepository
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal class MovieRepositoryImpl(
    private val discoverMovieApi: DiscoverMovieApi,
    private val movieDao: MovieDao,
    private val dispatchers: CoroutineDispatchers,
) : MovieRepository {

    private val genreMutexes = ConcurrentHashMap<Int, Mutex>()

    override fun observeMoviesByGenre(genreId: Int): Flow<Result<PagedResult<Movie>>> = flow {
        val cachedMovies = movieDao.observeByGenre(genreId).first()
        val cachedKey = movieDao.getLatestKey(genreId)
        val hasCache = cachedMovies.isNotEmpty() && cachedKey != null

        if (hasCache) {
            emit(Result.Success(buildPagedResult(cachedMovies, cachedKey)))
        }

        val isStale = cachedKey == null || TtlPolicy(cachedKey.fetchedAtMillis, CACHE_TTL).isStale()
        if (isStale) {
            val refreshResult = fetchPage(genreId = genreId, page = 1, clearExisting = true)
            if (refreshResult is Result.Error && !hasCache) {
                emit(Result.Error(refreshResult.error))
                return@flow
            }
        }

        emitAll(
            movieDao.observeByGenre(genreId).map { entities ->
                val latestKey = movieDao.getLatestKey(genreId)
                Result.Success(buildPagedResult(entities, latestKey))
            },
        )
    }.catch { e ->
        if (e is CancellationException && currentCoroutineContext()[Job]?.isActive != true) throw e
        emit(Result.Error(e.toDomainError()))
    }

    override suspend fun loadNextPage(genreId: Int): Result<Unit> {
        val mutex = mutexFor(genreId)
        if (!mutex.tryLock()) {
            return Result.Success(Unit)
        }
        return try {
            val latestKey = movieDao.getLatestKey(genreId)
            val currentPage = latestKey?.page ?: 0
            val isLastPage = latestKey?.isLastPage ?: false
            if (isLastPage || currentPage >= MAX_PAGE) {
                Result.Success(Unit)
            } else {
                fetchPage(genreId = genreId, page = currentPage + 1, clearExisting = false)
            }
        } finally {
            mutex.unlock()
        }
    }

    override suspend fun refresh(genreId: Int): Result<Unit> {
        return mutexFor(genreId).withLock {
            fetchPage(genreId = genreId, page = 1, clearExisting = true)
        }
    }

    private fun mutexFor(genreId: Int): Mutex = genreMutexes.getOrPut(genreId) { Mutex() }

    private suspend fun fetchPage(genreId: Int, page: Int, clearExisting: Boolean): Result<Unit> {
        return try {
            val response = withContext(dispatchers.io) {
                discoverMovieApi.discoverMovies(genreId = genreId, page = page)
            }
            val fetchedAt = System.currentTimeMillis()
            val isLastPage = page >= response.totalPages || page >= MAX_PAGE
            val movieEntities = response.results.mapIndexed { index, dto ->
                dto.toEntity(genreId = genreId, page = page, orderIndex = (page - 1) * PAGE_SIZE + index)
            }
            val keyEntities = movieEntities.map { movie ->
                MovieRemoteKeyEntity(
                    movieId = movie.id,
                    genreId = genreId,
                    page = page,
                    isLastPage = isLastPage,
                    totalPages = response.totalPages,
                    totalResults = response.totalResults,
                    fetchedAtMillis = fetchedAt,
                )
            }
            if (clearExisting) {
                movieDao.replaceGenre(genreId = genreId, movies = movieEntities, keys = keyEntities)
            } else {
                movieDao.insertPage(movieEntities, keyEntities)
            }
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.toDomainError())
        }
    }

    private fun buildPagedResult(entities: List<MovieEntity>, key: MovieRemoteKeyEntity?): PagedResult<Movie> {
        return PagedResult(
            items = entities.map { it.toDomain() },
            page = key?.page ?: 0,
            totalPages = key?.totalPages ?: 0,
            totalResults = key?.totalResults ?: 0,
        )
    }

    private companion object {
        const val MAX_PAGE = 500
        const val PAGE_SIZE = 20
        val CACHE_TTL = 1.hours
    }
}
