package com.movies.anymovies.feature.detail.domain.usecase

import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.detail.domain.model.PagedResult
import com.movies.anymovies.feature.detail.domain.model.Review
import com.movies.anymovies.feature.detail.domain.repository.ReviewRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private class FakeReviewRepository(
    private val observeResult: Flow<Result<PagedResult<Review>>>,
) : ReviewRepository {
    override fun observeReviews(movieId: Int): Flow<Result<PagedResult<Review>>> = observeResult

    override suspend fun loadNextPage(movieId: Int): Result<Unit> = Result.Success(Unit)
}

class GetReviewsUseCaseTest {

    private val review = Review(
        id = "1",
        author = "Rina",
        username = "rina27",
        avatarUrl = "avatar.jpg",
        rating = 8.0,
        content = "Great watch.",
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
    )

    @Test
    fun `invoke emits paged reviews for movie from repository`() = runTest {
        val page = PagedResult(items = listOf(review), page = 1, totalPages = 5, totalResults = 100)
        val useCase = GetReviewsUseCase(FakeReviewRepository(flowOf(Result.Success(page))))

        val result = useCase(movieId = 27).first()

        assertEquals(Result.Success(page), result)
    }

    @Test
    fun `invoke emits empty page when movie has no reviews`() = runTest {
        val page = PagedResult<Review>(items = emptyList(), page = 1, totalPages = 0, totalResults = 0)
        val useCase = GetReviewsUseCase(FakeReviewRepository(flowOf(Result.Success(page))))

        val result = useCase(movieId = 27).first()

        assertEquals(Result.Success(page), result)
    }

    @Test
    fun `invoke emits error when repository fails`() = runTest {
        val useCase = GetReviewsUseCase(FakeReviewRepository(flowOf(Result.Error(DomainError.NoConnection))))

        val result = useCase(movieId = 27).first()

        assertEquals(Result.Error(DomainError.NoConnection), result)
    }
}
