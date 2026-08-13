package com.movies.anymovies.feature.movies.domain.usecase

import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.movies.domain.model.Movie
import com.movies.anymovies.feature.movies.domain.model.PagedResult
import com.movies.anymovies.feature.movies.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private class FakePagingMovieRepository(
    page: Int,
    private val totalPages: Int,
    private val nextPageResult: Result<Unit> = Result.Success(Unit),
) : MovieRepository {
    var currentPage: Int = page
        private set
    var loadNextPageCallCount: Int = 0
        private set

    override fun observeMoviesByGenre(genreId: Int): Flow<Result<PagedResult<Movie>>> = emptyFlow()

    override suspend fun loadNextPage(genreId: Int): Result<Unit> {
        if (currentPage >= totalPages) {
            return Result.Success(Unit)
        }
        loadNextPageCallCount++
        return nextPageResult.also {
            if (it is Result.Success) currentPage++
        }
    }

    override suspend fun refresh(genreId: Int): Result<Unit> = Result.Success(Unit)
}

class LoadNextMoviesPageUseCaseTest {

    @Test
    fun `invoke advances to next page when more pages exist`() = runTest {
        val repository = FakePagingMovieRepository(page = 1, totalPages = 3)
        val useCase = LoadNextMoviesPageUseCase(repository)

        val result = useCase(genreId = 27)

        assertEquals(Result.Success(Unit), result)
        assertEquals(2, repository.currentPage)
        assertEquals(1, repository.loadNextPageCallCount)
    }

    @Test
    fun `invoke is a no-op when current page equals total pages`() = runTest {
        val repository = FakePagingMovieRepository(page = 3, totalPages = 3)
        val useCase = LoadNextMoviesPageUseCase(repository)

        val result = useCase(genreId = 27)

        assertEquals(Result.Success(Unit), result)
        assertEquals(3, repository.currentPage)
        assertEquals(0, repository.loadNextPageCallCount)
    }

    @Test
    fun `invoke returns error when repository fails to load next page`() = runTest {
        val repository = FakePagingMovieRepository(
            page = 1,
            totalPages = 3,
            nextPageResult = Result.Error(DomainError.Server),
        )
        val useCase = LoadNextMoviesPageUseCase(repository)

        val result = useCase(genreId = 27)

        assertEquals(Result.Error(DomainError.Server), result)
        assertEquals(1, repository.currentPage)
    }
}
