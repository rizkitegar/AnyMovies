package com.movies.anymovies.feature.genre.data.repository

import com.movies.anymovies.core.common.dispatchers.CoroutineDispatchers
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.core.database.genre.GenreDao
import com.movies.anymovies.core.network.error.toDomainError
import com.movies.anymovies.feature.genre.data.mapper.toDomain
import com.movies.anymovies.feature.genre.data.mapper.toEntity
import com.movies.anymovies.feature.genre.data.remote.GenreApi
import com.movies.anymovies.feature.genre.domain.model.Genre
import com.movies.anymovies.feature.genre.domain.repository.GenreRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class GenreRepositoryImpl(
    private val genreApi: GenreApi,
    private val genreDao: GenreDao,
    private val dispatchers: CoroutineDispatchers,
) : GenreRepository {

    override fun observeGenres(): Flow<Result<List<Genre>>> = flow {
        val cached = genreDao.observeAll().first()
        val hasCache = cached.isNotEmpty()
        if (hasCache) {
            emit(Result.Success(cached.map { it.toDomain() }))
        }

        val refreshResult = refreshGenres()
        if (refreshResult is Result.Error && !hasCache) {
            emit(Result.Error(refreshResult.error))
            return@flow
        }

        emitAll(genreDao.observeAll().map { entities -> Result.Success(entities.map { it.toDomain() }) })
    }

    override suspend fun refreshGenres(): Result<Unit> {
        return try {
            val response = withContext(dispatchers.io) { genreApi.getGenres() }
            genreDao.replaceAll(response.genres.map { it.toEntity() })
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.toDomainError())
        }
    }
}
