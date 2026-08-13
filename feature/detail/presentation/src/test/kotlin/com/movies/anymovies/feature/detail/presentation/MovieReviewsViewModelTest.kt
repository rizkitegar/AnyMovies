package com.movies.anymovies.feature.detail.presentation

import app.cash.turbine.test
import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.detail.domain.model.PagedResult
import com.movies.anymovies.feature.detail.domain.model.Review
import com.movies.anymovies.feature.detail.domain.usecase.GetReviewsUseCase
import com.movies.anymovies.feature.detail.domain.usecase.LoadNextReviewsPageUseCase
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieReviewsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun review(id: String) = Review(
        id = id,
        author = "Author $id",
        username = "user$id",
        avatarUrl = null,
        rating = 7.0,
        content = "Review $id content",
        createdAt = Instant.parse("2026-08-12T00:00:00Z"),
    )

    private fun createViewModel(repository: FakeReviewRepository, movieId: Int = 27205): MovieReviewsViewModel {
        return MovieReviewsViewModel(
            movieId = movieId,
            getReviewsUseCase = GetReviewsUseCase(repository),
            loadNextReviewsPageUseCase = LoadNextReviewsPageUseCase(repository),
        )
    }

    @Test
    fun `shows loading before any data arrives`() = runTest {
        val repository = FakeReviewRepository()
        repository.enqueueObserveResult(MutableSharedFlow())
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(MovieReviewsUiState.Loading, awaitItem())
        }
    }

    @Test
    fun `renders page one of reviews`() = runTest {
        val repository = FakeReviewRepository()
        val page = PagedResult(items = listOf(review("a"), review("b")), page = 1, totalPages = 2, totalResults = 3)
        repository.enqueueObserveResult(flowOf(Result.Success(page)))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem() as MovieReviewsUiState.Success
            assertEquals(listOf("a", "b"), state.reviews.map { it.id })
            assertEquals(false, state.endReached)
        }
    }

    @Test
    fun `zero reviews show the empty state rather than an error`() = runTest {
        val repository = FakeReviewRepository()
        val page = PagedResult<Review>(items = emptyList(), page = 1, totalPages = 0, totalResults = 0)
        repository.enqueueObserveResult(flowOf(Result.Success(page)))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(MovieReviewsUiState.Empty, awaitItem())
        }
    }

    @Test
    fun `initial page load failure shows a retryable error`() = runTest {
        val repository = FakeReviewRepository()
        repository.enqueueObserveResult(flowOf(Result.Error(DomainError.Server)))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(MovieReviewsUiState.Error(DomainError.Server), awaitItem())
        }
    }

    @Test
    fun `retry re-issues the initial request`() = runTest {
        val repository = FakeReviewRepository()
        repository.enqueueObserveResult(flowOf(Result.Error(DomainError.NoConnection)))
        val page = PagedResult(items = listOf(review("a")), page = 1, totalPages = 1, totalResults = 1)
        repository.enqueueObserveResult(flowOf(Result.Success(page)))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(MovieReviewsUiState.Error(DomainError.NoConnection), awaitItem())

            viewModel.onRetry()

            assertEquals(MovieReviewsUiState.Loading, awaitItem())
            val state = awaitItem() as MovieReviewsUiState.Success
            assertEquals(1, state.reviews.size)
        }
    }

    @Test
    fun `loading the next page flags appending until the appended page arrives`() = runTest {
        val repository = FakeReviewRepository()
        val liveFlow = MutableSharedFlow<Result<PagedResult<Review>>>(replay = 1)
        liveFlow.tryEmit(Result.Success(PagedResult(listOf(review("a")), page = 1, totalPages = 2, totalResults = 2)))
        repository.enqueueObserveResult(liveFlow)
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            val initial = awaitItem() as MovieReviewsUiState.Success
            assertEquals(false, initial.isAppending)

            viewModel.loadNextPage()

            val appending = awaitItem() as MovieReviewsUiState.Success
            assertEquals(true, appending.isAppending)
            assertEquals(1, appending.reviews.size)

            liveFlow.emit(
                Result.Success(PagedResult(listOf(review("a"), review("b")), page = 2, totalPages = 2, totalResults = 2)),
            )

            val appended = awaitItem() as MovieReviewsUiState.Success
            assertEquals(false, appended.isAppending)
            assertEquals(2, appended.reviews.size)
            assertEquals(true, appended.endReached)
        }
        assertEquals(1, repository.loadNextPageCallCount)
    }

    @Test
    fun `reaching the last page stops further append requests`() = runTest {
        val repository = FakeReviewRepository()
        val page = PagedResult(items = listOf(review("a"), review("b")), page = 1, totalPages = 1, totalResults = 2)
        repository.enqueueObserveResult(flowOf(Result.Success(page)))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem() as MovieReviewsUiState.Success
            assertEquals(true, state.endReached)
        }

        viewModel.loadNextPage()

        assertEquals(0, repository.loadNextPageCallCount)
    }

    @Test
    fun `rapid duplicate append triggers issue only one in-flight request`() = runTest {
        val repository = FakeReviewRepository()
        repository.suspendLoadNextPage = true
        val liveFlow = MutableSharedFlow<Result<PagedResult<Review>>>(replay = 1)
        liveFlow.tryEmit(Result.Success(PagedResult(listOf(review("a")), page = 1, totalPages = 2, totalResults = 2)))
        repository.enqueueObserveResult(liveFlow)
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            awaitItem() // initial success

            viewModel.loadNextPage()
            val appending = awaitItem() as MovieReviewsUiState.Success
            assertTrue(appending.isAppending)

            viewModel.loadNextPage()
            expectNoEvents()
        }

        assertEquals(1, repository.loadNextPageCallCount)
        repository.releaseLoadNextPage()
    }

    @Test
    fun `append failure keeps existing items visible and exposes a retryable footer error`() = runTest {
        val repository = FakeReviewRepository()
        repository.enqueueLoadNextPageResult(Result.Error(DomainError.Server))
        val liveFlow = MutableSharedFlow<Result<PagedResult<Review>>>(replay = 1)
        liveFlow.tryEmit(Result.Success(PagedResult(listOf(review("a")), page = 1, totalPages = 2, totalResults = 2)))
        repository.enqueueObserveResult(liveFlow)
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            awaitItem() // initial success

            viewModel.loadNextPage()
            awaitItem() // isAppending = true

            val failed = awaitItem() as MovieReviewsUiState.Success
            assertEquals(false, failed.isAppending)
            assertEquals(DomainError.Server, failed.appendError)
            assertEquals(1, failed.reviews.size)
        }
    }

    @Test
    fun `retrying a failed append re-requests only the failed page`() = runTest {
        val repository = FakeReviewRepository()
        repository.enqueueLoadNextPageResult(Result.Error(DomainError.Timeout))
        val liveFlow = MutableSharedFlow<Result<PagedResult<Review>>>(replay = 1)
        liveFlow.tryEmit(Result.Success(PagedResult(listOf(review("a")), page = 1, totalPages = 2, totalResults = 2)))
        repository.enqueueObserveResult(liveFlow)
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            awaitItem() // initial success

            viewModel.loadNextPage()
            awaitItem() // isAppending = true
            val failed = awaitItem() as MovieReviewsUiState.Success
            assertEquals(DomainError.Timeout, failed.appendError)

            viewModel.onRetryAppend()

            val retrying = awaitItem() as MovieReviewsUiState.Success
            assertEquals(true, retrying.isAppending)
            assertEquals(null, retrying.appendError)
        }
        assertEquals(2, repository.loadNextPageCallCount)
    }
}
