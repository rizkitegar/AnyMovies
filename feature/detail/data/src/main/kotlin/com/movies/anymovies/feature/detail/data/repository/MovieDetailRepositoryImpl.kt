package com.movies.anymovies.feature.detail.data.repository

import com.movies.anymovies.core.common.dispatchers.CoroutineDispatchers
import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.core.database.moviedetail.MovieDetailDao
import com.movies.anymovies.core.database.ttl.TtlPolicy
import com.movies.anymovies.core.network.error.toDomainError
import com.movies.anymovies.feature.detail.data.mapper.toDomain
import com.movies.anymovies.feature.detail.data.mapper.toEntity
import com.movies.anymovies.feature.detail.data.remote.MovieDetailApi
import com.movies.anymovies.feature.detail.domain.model.MovieDetail
import com.movies.anymovies.feature.detail.domain.repository.MovieDetailRepository
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

internal class MovieDetailRepositoryImpl(
    private val movieDetailApi: MovieDetailApi,
    private val movieDetailDao: MovieDetailDao,
    private val dispatchers: CoroutineDispatchers,
) : MovieDetailRepository {

    override fun observeMovieDetail(movieId: Int): Flow<Result<MovieDetail>> = flow {
        val cachedDetail = movieDetailDao.observeDetail(movieId).first()
        val hasCache = cachedDetail != null

        if (cachedDetail != null) {
            val cachedVideos = movieDetailDao.observeVideos(movieId).first()
            emit(Result.Success(cachedDetail.toDomain(cachedVideos)))
        }

        val isStale = cachedDetail == null || TtlPolicy(cachedDetail.fetchedAtMillis, CACHE_TTL).isStale()
        if (isStale) {
            val refreshResult = fetchDetail(movieId)
            if (refreshResult is Result.Error && !hasCache) {
                emit(Result.Error(refreshResult.error))
                return@flow
            }
        }

        emitAll(
            combine(
                movieDetailDao.observeDetail(movieId),
                movieDetailDao.observeVideos(movieId),
            ) { detail, videos ->
                if (detail == null) {
                    Result.Error(DomainError.NotFound)
                } else {
                    Result.Success(detail.toDomain(videos))
                }
            },
        )
    }.catch { e ->
        if (e is CancellationException && currentCoroutineContext()[Job]?.isActive != true) throw e
        emit(Result.Error(e.toDomainError()))
    }

    override suspend fun refresh(movieId: Int): Result<Unit> = fetchDetail(movieId)

    private suspend fun fetchDetail(movieId: Int): Result<Unit> {
        return try {
            val detailDto = withContext(dispatchers.io) { movieDetailApi.getMovieDetail(movieId = movieId) }
            var videoDtos = detailDto.videos?.results.orEmpty()
            if (videoDtos.isEmpty()) {
                videoDtos = fetchVideosFallback(movieId)
            }

            val fetchedAtMillis = System.currentTimeMillis()
            val detailEntity = detailDto.toEntity(fetchedAtMillis = fetchedAtMillis)
            val videoEntities = videoDtos.mapIndexed { index, dto -> dto.toEntity(movieId = movieId, orderIndex = index) }
            movieDetailDao.replaceDetail(detail = detailEntity, videos = videoEntities)
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.toDomainError())
        }
    }

    private suspend fun fetchVideosFallback(movieId: Int) = try {
        withContext(dispatchers.io) { movieDetailApi.getMovieVideos(movieId = movieId).results }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        emptyList()
    }

    private companion object {
        val CACHE_TTL = 6.hours
    }
}
