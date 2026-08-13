package com.movies.anymovies.feature.detail.presentation

import app.cash.turbine.test
import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.detail.domain.model.PagedResult
import com.movies.anymovies.feature.detail.domain.model.Review
import com.movies.anymovies.feature.detail.domain.usecase.GetReviewsUseCase
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewsSectionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun review(id: String, rating: Double? = 7.0) = Review(
        id = id,
        author = "Author $id",
        username = "user$id",
        avatarUrl = null,
        rating = rating,
        content = "Review $id content",
        createdAt = Instant.parse("2026-08-12T00:00:00Z"),
    )

    private fun createViewModel(repository: FakeReviewRepository, movieId: Int = 27205): ReviewsSectionViewModel {
        return ReviewsSectionViewModel(
            movieId = movieId,
            getReviewsUseCase = GetReviewsUseCase(repository),
        )
    }

    @Test
    fun `shows loading before any data arrives`() = runTest {
        val repository = FakeReviewRepository()
        repository.enqueueObserveResult(MutableSharedFlow())
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(ReviewsSectionUiState.Loading, awaitItem())
        }
    }

    @Test
    fun `shows only the first three reviews with the total review count for see-all`() = runTest {
        val repository = FakeReviewRepository()
        val page = PagedResult(
            items = listOf(review("a"), review("b"), review("c"), review("d"), review("e")),
            page = 1,
            totalPages = 2,
            totalResults = 27,
        )
        repository.enqueueObserveResult(flowOf(Result.Success(page)))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem() as ReviewsSectionUiState.Success
            assertEquals(3, state.reviews.size)
            assertEquals(listOf("a", "b", "c"), state.reviews.map { it.id })
            assertEquals(27, state.totalResults)
        }
    }

    @Test
    fun `zero reviews render the empty state rather than an error`() = runTest {
        val repository = FakeReviewRepository()
        val page = PagedResult<Review>(items = emptyList(), page = 1, totalPages = 0, totalResults = 0)
        repository.enqueueObserveResult(flowOf(Result.Success(page)))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(ReviewsSectionUiState.Empty, awaitItem())
        }
    }

    @Test
    fun `a null rating on a review is mapped to a null rating label`() = runTest {
        val repository = FakeReviewRepository()
        val page = PagedResult(items = listOf(review("a", rating = null)), page = 1, totalPages = 1, totalResults = 1)
        repository.enqueueObserveResult(flowOf(Result.Success(page)))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem() as ReviewsSectionUiState.Success
            assertEquals(null, state.reviews.first().ratingLabel)
        }
    }

    @Test
    fun `the reviews call failing surfaces a retryable error scoped to this view model`() = runTest {
        val repository = FakeReviewRepository()
        repository.enqueueObserveResult(flowOf(Result.Error(DomainError.Server)))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(ReviewsSectionUiState.Error(DomainError.Server), awaitItem())
        }
    }

    @Test
    fun `retry re-issues the request and can recover into success`() = runTest {
        val repository = FakeReviewRepository()
        repository.enqueueObserveResult(flowOf(Result.Error(DomainError.NoConnection)))
        val page = PagedResult(items = listOf(review("a")), page = 1, totalPages = 1, totalResults = 1)
        repository.enqueueObserveResult(flowOf(Result.Success(page)))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(ReviewsSectionUiState.Error(DomainError.NoConnection), awaitItem())

            viewModel.onRetry()

            assertEquals(ReviewsSectionUiState.Loading, awaitItem())
            val state = awaitItem() as ReviewsSectionUiState.Success
            assertEquals(1, state.reviews.size)
        }
    }
}
