package com.movies.anymovies.feature.genre.domain.usecase

import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.genre.domain.model.Genre
import com.movies.anymovies.feature.genre.domain.repository.GenreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private class FakeRefreshGenreRepository(
    private val refreshGenresResult: Result<Unit>,
) : GenreRepository {
    override fun observeGenres(): Flow<Result<List<Genre>>> = emptyFlow()

    override suspend fun refreshGenres(): Result<Unit> = refreshGenresResult
}

class RefreshGenresUseCaseTest {

    @Test
    fun `invoke returns success when repository refresh succeeds`() = runTest {
        val useCase = RefreshGenresUseCase(FakeRefreshGenreRepository(Result.Success(Unit)))

        val result = useCase()

        assertEquals(Result.Success(Unit), result)
    }

    @Test
    fun `invoke returns error when repository refresh fails`() = runTest {
        val useCase = RefreshGenresUseCase(FakeRefreshGenreRepository(Result.Error(DomainError.Server)))

        val result = useCase()

        assertEquals(Result.Error(DomainError.Server), result)
    }
}
