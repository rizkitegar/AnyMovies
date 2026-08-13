package com.movies.anymovies.feature.genre.presentation

import app.cash.turbine.test
import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.genre.domain.model.Genre
import com.movies.anymovies.feature.genre.domain.repository.GenreRepository
import com.movies.anymovies.feature.genre.domain.usecase.GetGenresUseCase
import com.movies.anymovies.feature.genre.domain.usecase.RefreshGenresUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

private class FakeGenreRepository : GenreRepository {

    private val observeQueue = ArrayDeque<Flow<Result<List<Genre>>>>()

    var refreshResult: Result<Unit> = Result.Success(Unit)
    var refreshCallCount: Int = 0
        private set
    var observeCallCount: Int = 0
        private set

    fun enqueueObserveResult(flow: Flow<Result<List<Genre>>>) {
        observeQueue.addLast(flow)
    }

    override fun observeGenres(): Flow<Result<List<Genre>>> {
        observeCallCount++
        return observeQueue.removeFirstOrNull() ?: emptyFlow()
    }

    override suspend fun refreshGenres(): Result<Unit> {
        refreshCallCount++
        return refreshResult
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class GenreListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val actionGenre = Genre(id = 28, name = "Action")
    private val comedyGenre = Genre(id = 35, name = "Comedy")

    private fun createViewModel(repository: FakeGenreRepository): GenreListViewModel {
        return GenreListViewModel(
            getGenresUseCase = GetGenresUseCase(repository),
            refreshGenresUseCase = RefreshGenresUseCase(repository),
        )
    }

    @Test
    fun `shows loading before any data arrives`() = runTest {
        val repository = FakeGenreRepository()
        repository.enqueueObserveResult(MutableSharedFlow())
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(GenreListUiState.Loading, awaitItem())
        }
    }

    @Test
    fun `renders the genre list on success`() = runTest {
        val repository = FakeGenreRepository()
        repository.enqueueObserveResult(flowOf(Result.Success(listOf(actionGenre, comedyGenre))))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(GenreListUiState.Success(listOf(actionGenre, comedyGenre)), awaitItem())
        }
    }

    @Test
    fun `renders cached genres immediately and updates once the background refresh lands`() = runTest {
        val repository = FakeGenreRepository()
        val liveFlow = MutableSharedFlow<Result<List<Genre>>>(replay = 1)
        liveFlow.tryEmit(Result.Success(listOf(actionGenre)))
        repository.enqueueObserveResult(liveFlow)
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(GenreListUiState.Success(listOf(actionGenre)), awaitItem())

            liveFlow.emit(Result.Success(listOf(actionGenre, comedyGenre)))

            assertEquals(GenreListUiState.Success(listOf(actionGenre, comedyGenre)), awaitItem())
        }
    }

    @Test
    fun `pull to refresh flags refreshing and clears it once the network call returns`() = runTest {
        val repository = FakeGenreRepository()
        repository.enqueueObserveResult(flowOf(Result.Success(listOf(actionGenre))))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(GenreListUiState.Success(listOf(actionGenre)), awaitItem())

            viewModel.onRefresh()

            assertEquals(
                GenreListUiState.Success(listOf(actionGenre), isRefreshing = true),
                awaitItem(),
            )
            assertEquals(
                GenreListUiState.Success(listOf(actionGenre), isRefreshing = false),
                awaitItem(),
            )
        }
        assertEquals(1, repository.refreshCallCount)
    }

    @Test
    fun `no connection with no cache shows a retryable error`() = runTest {
        val repository = FakeGenreRepository()
        repository.enqueueObserveResult(flowOf(Result.Error(DomainError.NoConnection)))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(GenreListUiState.Error(DomainError.NoConnection), awaitItem())
        }
    }

    @Test
    fun `cached genres stay visible when the background refresh fails offline`() = runTest {
        // The repository only surfaces a failed background refresh as an error when no
        // cache exists yet; with a cache present it keeps emitting the cached success value.
        val repository = FakeGenreRepository()
        repository.enqueueObserveResult(flowOf(Result.Success(listOf(actionGenre))))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(GenreListUiState.Success(listOf(actionGenre)), awaitItem())
        }
    }

    @Test
    fun `invalid credential failure shows a generic service error, never the raw cause`() = runTest {
        val repository = FakeGenreRepository()
        repository.enqueueObserveResult(flowOf(Result.Error(DomainError.Unauthorized)))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(GenreListUiState.Error(DomainError.Unauthorized), state)
        }
    }

    @Test
    fun `server failure shows an error and retry re-issues the request`() = runTest {
        val repository = FakeGenreRepository()
        repository.enqueueObserveResult(flowOf(Result.Error(DomainError.Server)))
        repository.enqueueObserveResult(flowOf(Result.Success(listOf(actionGenre))))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(GenreListUiState.Error(DomainError.Server), awaitItem())

            viewModel.onRetry()

            assertEquals(GenreListUiState.Loading, awaitItem())
            assertEquals(GenreListUiState.Success(listOf(actionGenre)), awaitItem())
        }
        assertEquals(2, repository.observeCallCount)
    }

    @Test
    fun `timeout failure surfaces as a retryable error state`() = runTest {
        val repository = FakeGenreRepository()
        repository.enqueueObserveResult(flowOf(Result.Error(DomainError.Timeout)))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(GenreListUiState.Error(DomainError.Timeout), awaitItem())
        }
    }

    @Test
    fun `empty genre list shows the empty state rather than an error`() = runTest {
        val repository = FakeGenreRepository()
        repository.enqueueObserveResult(flowOf(Result.Success(emptyList())))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(GenreListUiState.Empty, awaitItem())
        }
    }

    @Test
    fun `malformed response is mapped to a generic error without crashing`() = runTest {
        val repository = FakeGenreRepository()
        repository.enqueueObserveResult(flowOf(Result.Error(DomainError.Parse)))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(GenreListUiState.Error(DomainError.Parse), awaitItem())
        }
    }
}
