package com.movies.anymovies.feature.detail.domain.usecase

import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.detail.domain.model.PagedResult
import com.movies.anymovies.feature.detail.domain.model.Review
import com.movies.anymovies.feature.detail.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private class FakePagingReviewRepository(
    page: Int,
    private val totalPages: Int,
    private val nextPageResult: Result<Unit> = Result.Success(Unit),
) : ReviewRepository {
    var currentPage: Int = page
        private set
    var loadNextPageCallCount: Int = 0
        private set

    override fun observeReviews(movieId: Int): Flow<Result<PagedResult<Review>>> = emptyFlow()

    override suspend fun loadNextPage(movieId: Int): Result<Unit> {
        if (currentPage >= totalPages) {
            return Result.Success(Unit)
        }
        loadNextPageCallCount++
        return nextPageResult.also {
            if (it is Result.Success) currentPage++
        }
    }
}

class LoadNextReviewsPageUseCaseTest {

    @Test
    fun `invoke advances to next page when more pages exist`() = runTest {
        val repository = FakePagingReviewRepository(page = 1, totalPages = 3)
        val useCase = LoadNextReviewsPageUseCase(repository)

        val result = useCase(movieId = 27)

        assertEquals(Result.Success(Unit), result)
        assertEquals(2, repository.currentPage)
        assertEquals(1, repository.loadNextPageCallCount)
    }

    @Test
    fun `invoke is a no-op when current page equals total pages`() = runTest {
        val repository = FakePagingReviewRepository(page = 3, totalPages = 3)
        val useCase = LoadNextReviewsPageUseCase(repository)

        val result = useCase(movieId = 27)

        assertEquals(Result.Success(Unit), result)
        assertEquals(3, repository.currentPage)
        assertEquals(0, repository.loadNextPageCallCount)
    }

    @Test
    fun `invoke returns error when repository fails to load next page`() = runTest {
        val repository = FakePagingReviewRepository(
            page = 1,
            totalPages = 3,
            nextPageResult = Result.Error(DomainError.Server),
        )
        val useCase = LoadNextReviewsPageUseCase(repository)

        val result = useCase(movieId = 27)

        assertEquals(Result.Error(DomainError.Server), result)
        assertEquals(1, repository.currentPage)
    }
}
