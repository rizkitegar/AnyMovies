package com.movies.anymovies.feature.detail.presentation

import app.cash.turbine.test
import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.detail.domain.model.Genre
import com.movies.anymovies.feature.detail.domain.model.MovieDetail
import com.movies.anymovies.feature.detail.domain.repository.MovieDetailRepository
import com.movies.anymovies.feature.detail.domain.usecase.GetMovieDetailUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

private class FakeMovieDetailRepository : MovieDetailRepository {

    private val observeQueue = ArrayDeque<Flow<Result<MovieDetail>>>()

    fun enqueueObserveResult(flow: Flow<Result<MovieDetail>>) {
        observeQueue.addLast(flow)
    }

    override fun observeMovieDetail(movieId: Int): Flow<Result<MovieDetail>> {
        return observeQueue.removeFirstOrNull() ?: emptyFlow()
    }

    override suspend fun refresh(movieId: Int): Result<Unit> = Result.Success(Unit)
}

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val cachedDetail = MovieDetail(
        id = 27205,
        title = "Inception",
        tagline = "Your mind is the scene of the crime.",
        overview = "A mind-bending thriller.",
        posterUrl = "https://image.tmdb.org/t/p/w500/poster.jpg",
        backdropUrl = "https://image.tmdb.org/t/p/w780/backdrop.jpg",
        releaseDate = java.time.LocalDate.of(2026, 8, 12),
        runtimeMinutes = 148,
        genres = listOf(Genre(28, "Action")),
        voteAverage = 8.4,
        voteCount = 12304,
        status = "Released",
        originalLanguage = "en",
        homepage = "https://example.com",
        videos = emptyList(),
    )

    private val refreshedDetail = cachedDetail.copy(voteCount = 12400)

    private fun createViewModel(repository: FakeMovieDetailRepository, movieId: Int = 27205): MovieDetailViewModel {
        return MovieDetailViewModel(
            movieId = movieId,
            getMovieDetailUseCase = GetMovieDetailUseCase(repository),
        )
    }

    @Test
    fun `shows loading before any data arrives`() = runTest {
        val repository = FakeMovieDetailRepository()
        repository.enqueueObserveResult(MutableSharedFlow())
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(MovieDetailUiState.Loading, awaitItem())
        }
    }

    @Test
    fun `renders the primary movie information on success`() = runTest {
        val repository = FakeMovieDetailRepository()
        repository.enqueueObserveResult(flowOf(Result.Success(cachedDetail)))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem() as MovieDetailUiState.Success
            assertEquals("Inception", state.movie.title)
            assertEquals("2h 28m", state.movie.runtimeLabel)
            assertEquals("12 Aug 2026", state.movie.releaseDateLabel)
            assertEquals("8.4 · 12,304 votes", state.movie.ratingLabel)
            assertEquals("A mind-bending thriller.", state.movie.overviewLabel)
        }
    }

    @Test
    fun `cached detail renders immediately and a background refresh updates it in place`() = runTest {
        val repository = FakeMovieDetailRepository()
        val liveFlow = MutableSharedFlow<Result<MovieDetail>>(replay = 1)
        liveFlow.tryEmit(Result.Success(cachedDetail))
        repository.enqueueObserveResult(liveFlow)
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            val cached = awaitItem() as MovieDetailUiState.Success
            assertEquals("8.4 · 12,304 votes", cached.movie.ratingLabel)

            liveFlow.emit(Result.Success(refreshedDetail))

            val refreshed = awaitItem() as MovieDetailUiState.Success
            assertEquals("8.4 · 12,400 votes", refreshed.movie.ratingLabel)
        }
    }

    @Test
    fun `a 404 is surfaced as a non-retryable not-found error`() = runTest {
        val repository = FakeMovieDetailRepository()
        repository.enqueueObserveResult(flowOf(Result.Error(DomainError.NotFound)))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem() as MovieDetailUiState.Error
            assertEquals(DomainError.NotFound, state.error)
            assertEquals(false, state.isRetryable)
        }
    }

    @Test
    fun `a network failure with no cache is surfaced as a retryable error`() = runTest {
        val repository = FakeMovieDetailRepository()
        repository.enqueueObserveResult(flowOf(Result.Error(DomainError.NoConnection)))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem() as MovieDetailUiState.Error
            assertEquals(DomainError.NoConnection, state.error)
            assertEquals(true, state.isRetryable)
        }
    }

    @Test
    fun `a server error is also retryable`() = runTest {
        val repository = FakeMovieDetailRepository()
        repository.enqueueObserveResult(flowOf(Result.Error(DomainError.Server)))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem() as MovieDetailUiState.Error
            assertEquals(true, state.isRetryable)
        }
    }

    @Test
    fun `retry re-issues the request and can recover into success`() = runTest {
        val repository = FakeMovieDetailRepository()
        repository.enqueueObserveResult(flowOf(Result.Error(DomainError.NoConnection)))
        repository.enqueueObserveResult(flowOf(Result.Success(cachedDetail)))
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(
                MovieDetailUiState.Error(DomainError.NoConnection, isRetryable = true),
                awaitItem(),
            )

            viewModel.onRetry()

            assertEquals(MovieDetailUiState.Loading, awaitItem())
            val state = awaitItem() as MovieDetailUiState.Success
            assertEquals("Inception", state.movie.title)
        }
    }
}
