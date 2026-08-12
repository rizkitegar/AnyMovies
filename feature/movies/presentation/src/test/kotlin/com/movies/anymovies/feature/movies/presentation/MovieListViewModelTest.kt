package com.movies.anymovies.feature.movies.presentation

import app.cash.turbine.test
import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.movies.domain.model.Movie
import com.movies.anymovies.feature.movies.domain.model.PagedResult
import com.movies.anymovies.feature.movies.domain.repository.MovieRepository
import com.movies.anymovies.feature.movies.domain.usecase.DiscoverMoviesUseCase
import com.movies.anymovies.feature.movies.domain.usecase.LoadNextMoviesPageUseCase
import com.movies.anymovies.feature.movies.presentation.model.MovieUiModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private class FakeMovieRepository : MovieRepository {

    private val observeQueue = ArrayDeque<Flow<Result<PagedResult<Movie>>>>()
    private val loadNextPageResults = ArrayDeque<Result<Unit>>()
    private val loadNextPageGate = Channel<Unit>(Channel.UNLIMITED)

    var suspendLoadNextPage: Boolean = false
    var loadNextPageCallCount: Int = 0
        private set

    fun enqueueObserveResult(flow: Flow<Result<PagedResult<Movie>>>) {
        observeQueue.addLast(flow)
    }

    fun enqueueLoadNextPageResult(result: Result<Unit>) {
        loadNextPageResults.addLast(result)
    }

    fun releaseLoadNextPage() {
        loadNextPageGate.trySend(Unit)
    }

    override fun observeMoviesByGenre(genreId: Int): Flow<Result<PagedResult<Movie>>> {
        return observeQueue.removeFirstOrNull() ?: emptyFlow()
    }

    override suspend fun loadNextPage(genreId: Int): Result<Unit> {
        loadNextPageCallCount++
        if (suspendLoadNextPage) {
            loadNextPageGate.receive()
        }
        return loadNextPageResults.removeFirstOrNull() ?: Result.Success(Unit)
    }

    override suspend fun refresh(genreId: Int): Result<Unit> = Result.Success(Unit)
}

@OptIn(ExperimentalCoroutinesApi::class)
class MovieListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val movieA = Movie(
        id = 1,
        title = "Movie A",
        posterUrl = "https://image.tmdb.org/t/p/w342/a.jpg",
        backdropUrl = null,
        releaseYear = "2023",
        voteAverage = 7.5,
        voteCount = 10,
        overview = "",
    )
    private val movieBWithNulls = Movie(
        id = 2,
        title = "Movie B",
        posterUrl = null,
        backdropUrl = null,
        releaseYear = null,
        voteAverage = 0.0,
        voteCount = 0,
        overview = "",
    )
    private val movieC = Movie(
        id = 3,
        title = "Movie C",
        posterUrl = "https://image.tmdb.org/t/p/w342/c.jpg",
        backdropUrl = null,
        releaseYear = "2022",
        voteAverage = 6.1,
        voteCount = 5,
        overview = "",
    )

    private fun createViewModel(repository: FakeMovieRepository, genreId: Int = 28): MovieListViewModel {
        return MovieListViewModel(
            genreId = genreId,
            discoverMoviesUseCase = DiscoverMoviesUseCase(repository),
            loadNextMoviesPageUseCase = LoadNextMoviesPageUseCase(repository),
        )
    }

    @Test
    fun `shows loading before any data arrives`() = runTest {
        val repository = FakeMovieRepository()
        repository.enqueueObserveResult(MutableSharedFlow())
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(MovieListUiState.Loading, awaitItem())
        }
    }

    @Test
    fun `renders page one with poster, title, year and rating formatted for display`() = runTest {
        val repository = FakeMovieRepository()
        val page = PagedResult(items = listOf(movieA, movieBWithNulls), page = 1, totalPages = 2, totalResults = 3)
        repository.enqueueObserveResult(flowOf(Result.Success(page)))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem() as MovieListUiState.Success
            assertEquals(
                listOf(
                    MovieUiModel(
                        id = 1,
                        title = "Movie A",
                        posterUrl = "https://image.tmdb.org/t/p/w342/a.jpg",
                        releaseYearLabel = "2023",
                        voteAverageLabel = "7.5",
                    ),
                    MovieUiModel(
                        id = 2,
                        title = "Movie B",
                        posterUrl = null,
                        releaseYearLabel = null,
                        voteAverageLabel = null,
                    ),
                ),
                state.movies,
            )
            assertEquals(false, state.endReached)
        }
    }

    @Test
    fun `zero results show the empty state rather than an error`() = runTest {
        val repository = FakeMovieRepository()
        val page = PagedResult<Movie>(items = emptyList(), page = 1, totalPages = 0, totalResults = 0)
        repository.enqueueObserveResult(flowOf(Result.Success(page)))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(MovieListUiState.Empty, awaitItem())
        }
    }

    @Test
    fun `initial page load failure shows a retryable error`() = runTest {
        val repository = FakeMovieRepository()
        repository.enqueueObserveResult(flowOf(Result.Error(DomainError.Server)))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(MovieListUiState.Error(DomainError.Server), awaitItem())
        }
    }

    @Test
    fun `retry re-issues the initial request`() = runTest {
        val repository = FakeMovieRepository()
        repository.enqueueObserveResult(flowOf(Result.Error(DomainError.NoConnection)))
        val page = PagedResult(items = listOf(movieA), page = 1, totalPages = 1, totalResults = 1)
        repository.enqueueObserveResult(flowOf(Result.Success(page)))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(MovieListUiState.Error(DomainError.NoConnection), awaitItem())

            viewModel.onRetry()

            assertEquals(MovieListUiState.Loading, awaitItem())
            val state = awaitItem() as MovieListUiState.Success
            assertEquals(1, state.movies.size)
        }
    }

    @Test
    fun `loading the next page flags appending until the appended page arrives`() = runTest {
        val repository = FakeMovieRepository()
        val liveFlow = MutableSharedFlow<Result<PagedResult<Movie>>>(replay = 1)
        liveFlow.tryEmit(Result.Success(PagedResult(listOf(movieA), page = 1, totalPages = 2, totalResults = 3)))
        repository.enqueueObserveResult(liveFlow)
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            val initial = awaitItem() as MovieListUiState.Success
            assertEquals(false, initial.isAppending)

            viewModel.loadNextPage()

            val appending = awaitItem() as MovieListUiState.Success
            assertEquals(true, appending.isAppending)
            assertEquals(1, appending.movies.size)

            liveFlow.emit(
                Result.Success(PagedResult(listOf(movieA, movieBWithNulls), page = 2, totalPages = 2, totalResults = 3)),
            )

            val appended = awaitItem() as MovieListUiState.Success
            assertEquals(false, appended.isAppending)
            assertEquals(2, appended.movies.size)
            assertEquals(true, appended.endReached)
        }
        assertEquals(1, repository.loadNextPageCallCount)
    }

    @Test
    fun `reaching the last page stops further append requests`() = runTest {
        val repository = FakeMovieRepository()
        val page = PagedResult(items = listOf(movieA, movieBWithNulls, movieC), page = 1, totalPages = 1, totalResults = 3)
        repository.enqueueObserveResult(flowOf(Result.Success(page)))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem() as MovieListUiState.Success
            assertEquals(true, state.endReached)
        }

        viewModel.loadNextPage()

        assertEquals(0, repository.loadNextPageCallCount)
    }

    @Test
    fun `rapid duplicate append triggers issue only one in-flight request`() = runTest {
        val repository = FakeMovieRepository()
        repository.suspendLoadNextPage = true
        val liveFlow = MutableSharedFlow<Result<PagedResult<Movie>>>(replay = 1)
        liveFlow.tryEmit(Result.Success(PagedResult(listOf(movieA), page = 1, totalPages = 2, totalResults = 2)))
        repository.enqueueObserveResult(liveFlow)
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            awaitItem() // initial success

            viewModel.loadNextPage()
            val appending = awaitItem() as MovieListUiState.Success
            assertTrue(appending.isAppending)

            // A second trigger while the first append is still in flight must not fire a new request.
            viewModel.loadNextPage()
            expectNoEvents()
        }

        assertEquals(1, repository.loadNextPageCallCount)
        repository.releaseLoadNextPage()
    }

    @Test
    fun `append failure keeps existing items visible and exposes a retryable footer error`() = runTest {
        val repository = FakeMovieRepository()
        repository.enqueueLoadNextPageResult(Result.Error(DomainError.Server))
        val liveFlow = MutableSharedFlow<Result<PagedResult<Movie>>>(replay = 1)
        liveFlow.tryEmit(Result.Success(PagedResult(listOf(movieA), page = 1, totalPages = 2, totalResults = 2)))
        repository.enqueueObserveResult(liveFlow)
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            awaitItem() // initial success

            viewModel.loadNextPage()
            awaitItem() // isAppending = true

            val failed = awaitItem() as MovieListUiState.Success
            assertEquals(false, failed.isAppending)
            assertEquals(DomainError.Server, failed.appendError)
            assertEquals(1, failed.movies.size)
        }
    }

    @Test
    fun `retrying a failed append re-requests only the failed page`() = runTest {
        val repository = FakeMovieRepository()
        repository.enqueueLoadNextPageResult(Result.Error(DomainError.Timeout))
        val liveFlow = MutableSharedFlow<Result<PagedResult<Movie>>>(replay = 1)
        liveFlow.tryEmit(Result.Success(PagedResult(listOf(movieA), page = 1, totalPages = 2, totalResults = 2)))
        repository.enqueueObserveResult(liveFlow)
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            awaitItem() // initial success

            viewModel.loadNextPage()
            awaitItem() // isAppending = true
            val failed = awaitItem() as MovieListUiState.Success
            assertEquals(DomainError.Timeout, failed.appendError)

            viewModel.onRetryAppend()

            val retrying = awaitItem() as MovieListUiState.Success
            assertEquals(true, retrying.isAppending)
            assertEquals(null, retrying.appendError)
        }
        assertEquals(2, repository.loadNextPageCallCount)
    }
}
