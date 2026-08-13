package com.movies.anymovies.feature.detail.domain.usecase

import androidx.paging.PagingData
import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.detail.domain.model.PagedResult
import com.movies.anymovies.feature.detail.domain.model.Review
import com.movies.anymovies.feature.detail.domain.repository.ReviewRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private class FakeReviewRepository(
    private val previewResult: Result<PagedResult<Review>>,
) : ReviewRepository {
    override fun getReviewsPagingData(movieId: Int): Flow<PagingData<Review>> =
        error("not used by this test")

    override suspend fun getReviewsPreview(movieId: Int): Result<PagedResult<Review>> = previewResult
}

class GetReviewsPreviewUseCaseTest {

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
    fun `invoke returns the preview page from the repository`() = runTest {
        val page = PagedResult(items = listOf(review), page = 1, totalPages = 5, totalResults = 100)
        val useCase = GetReviewsPreviewUseCase(FakeReviewRepository(Result.Success(page)))

        val result = useCase(movieId = 27)

        assertEquals(Result.Success(page), result)
    }

    @Test
    fun `invoke returns an empty page when the movie has no reviews`() = runTest {
        val page = PagedResult<Review>(items = emptyList(), page = 1, totalPages = 0, totalResults = 0)
        val useCase = GetReviewsPreviewUseCase(FakeReviewRepository(Result.Success(page)))

        val result = useCase(movieId = 27)

        assertEquals(Result.Success(page), result)
    }

    @Test
    fun `invoke returns an error when the repository fails`() = runTest {
        val useCase = GetReviewsPreviewUseCase(FakeReviewRepository(Result.Error(DomainError.NoConnection)))

        val result = useCase(movieId = 27)

        assertEquals(Result.Error(DomainError.NoConnection), result)
    }
}
