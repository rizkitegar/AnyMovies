package com.movies.anymovies.feature.movies.domain.usecase

import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.movies.domain.model.Movie
import com.movies.anymovies.feature.movies.domain.model.PagedResult
import com.movies.anymovies.feature.movies.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private class FakeMovieRepository(
    private val observeResult: Flow<Result<PagedResult<Movie>>>,
) : MovieRepository {
    override fun observeMoviesByGenre(genreId: Int): Flow<Result<PagedResult<Movie>>> = observeResult

    override suspend fun loadNextPage(genreId: Int): Result<Unit> = Result.Success(Unit)

    override suspend fun refresh(genreId: Int): Result<Unit> = Result.Success(Unit)
}

class DiscoverMoviesUseCaseTest {

    private val movie = Movie(
        id = 27,
        title = "Alien",
        posterUrl = "poster.jpg",
        backdropUrl = "backdrop.jpg",
        releaseYear = "1979",
        voteAverage = 7.9,
        voteCount = 12000,
        overview = "A crew encounters a deadly creature.",
    )

    @Test
    fun `invoke emits paged movies for genre from repository`() = runTest {
        val page = PagedResult(items = listOf(movie), page = 1, totalPages = 5, totalResults = 100)
        val useCase = DiscoverMoviesUseCase(FakeMovieRepository(flowOf(Result.Success(page))))

        val result = useCase(genreId = 27).first()

        assertEquals(Result.Success(page), result)
    }

    @Test
    fun `invoke emits empty page when genre has no results`() = runTest {
        val page = PagedResult<Movie>(items = emptyList(), page = 1, totalPages = 0, totalResults = 0)
        val useCase = DiscoverMoviesUseCase(FakeMovieRepository(flowOf(Result.Success(page))))

        val result = useCase(genreId = 27).first()

        assertEquals(Result.Success(page), result)
    }

    @Test
    fun `invoke emits error when repository fails`() = runTest {
        val useCase = DiscoverMoviesUseCase(FakeMovieRepository(flowOf(Result.Error(DomainError.NoConnection))))

        val result = useCase(genreId = 27).first()

        assertEquals(Result.Error(DomainError.NoConnection), result)
    }
}
