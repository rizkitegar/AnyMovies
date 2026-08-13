package com.movies.anymovies.feature.detail.presentation

import androidx.paging.PagingData
import androidx.paging.map
import androidx.paging.testing.asSnapshot
import com.movies.anymovies.feature.detail.domain.model.Review
import com.movies.anymovies.feature.detail.domain.usecase.GetReviewsUseCase
import com.movies.anymovies.feature.detail.presentation.model.toUiModel
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

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
        )
    }

    @Test
    fun `the review-to-ui-model mapping the view model applies maps domain reviews correctly`() = runTest {
        // Same transform MovieReviewsViewModel applies before cachedIn(); verified directly here
        // since paging-testing's asSnapshot() cannot drive a flow that has already gone through
        // cachedIn(viewModelScope) inside a virtual-time test dispatcher (see the caching test below
        // for cachedIn's own multicast behavior, verified without asSnapshot).
        val repository = FakeReviewRepository()
        repository.pagingDataFlow = flowOf(PagingData.from(listOf(review("a"), review("b"))))
        val useCase = GetReviewsUseCase(repository)

        val snapshot = useCase(movieId = 27205)
            .map { pagingData -> pagingData.map { it.toUiModel() } }
            .asSnapshot()

        assertEquals(listOf("a", "b"), snapshot.map { it.id })
    }

    @Test
    fun `reviews paging data is cached in the view model scope so a second collector does not re-trigger the repository`() = runTest {
        val repository = FakeReviewRepository()
        var collectionCount = 0
        repository.pagingDataFlow = flow {
            collectionCount++
            emit(PagingData.from(listOf(review("a"))))
        }
        val viewModel = createViewModel(repository)

        viewModel.reviewsPagingData.first()
        viewModel.reviewsPagingData.first()

        assertEquals(1, collectionCount)
    }
}
