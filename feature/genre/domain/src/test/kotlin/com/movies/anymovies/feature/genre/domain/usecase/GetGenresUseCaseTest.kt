package com.movies.anymovies.feature.genre.domain.usecase

import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.genre.domain.model.Genre
import com.movies.anymovies.feature.genre.domain.repository.GenreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private class FakeGenreRepository(
    private val observeGenresResult: Flow<Result<List<Genre>>>,
) : GenreRepository {
    override fun observeGenres(): Flow<Result<List<Genre>>> = observeGenresResult

    override suspend fun refreshGenres(): Result<Unit> = Result.Success(Unit)
}

class GetGenresUseCaseTest {

    @Test
    fun `invoke emits genres from repository on success`() = runTest {
        val genres = listOf(Genre(id = 28, name = "Action"), Genre(id = 35, name = "Comedy"))
        val useCase = GetGenresUseCase(FakeGenreRepository(flowOf(Result.Success(genres))))

        val result = useCase().first()

        assertEquals(Result.Success(genres), result)
    }

    @Test
    fun `invoke emits empty list when repository returns none`() = runTest {
        val useCase = GetGenresUseCase(FakeGenreRepository(flowOf(Result.Success(emptyList()))))

        val result = useCase().first()

        assertEquals(Result.Success(emptyList<Genre>()), result)
    }

    @Test
    fun `invoke emits error when repository fails`() = runTest {
        val useCase = GetGenresUseCase(FakeGenreRepository(flowOf(Result.Error(DomainError.NoConnection))))

        val result = useCase().first()

        assertEquals(Result.Error(DomainError.NoConnection), result)
    }
}
