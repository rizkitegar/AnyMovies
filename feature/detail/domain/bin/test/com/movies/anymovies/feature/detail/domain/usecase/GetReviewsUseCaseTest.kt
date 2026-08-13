package com.movies.anymovies.feature.detail.domain.usecase

import androidx.paging.PagingData
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.detail.domain.model.PagedResult
import com.movies.anymovies.feature.detail.domain.model.Review
import com.movies.anymovies.feature.detail.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

private class FakeReviewRepository(
    private val pagingDataFlow: Flow<PagingData<Review>>,
) : ReviewRepository {
    override fun getReviewsPagingData(movieId: Int): Flow<PagingData<Review>> = pagingDataFlow

    override suspend fun getReviewsPreview(movieId: Int): Result<PagedResult<Review>> =
        error("not used by this test")
}

class GetReviewsUseCaseTest {

    @Test
    fun `invoke delegates to the repository's paging data flow for the given movie`() = runTest {
        val expected = flowOf(PagingData.empty<Review>())
        val useCase = GetReviewsUseCase(FakeReviewRepository(expected))

        val result = useCase(movieId = 27)

        assertSame(expected, result)
    }
}
