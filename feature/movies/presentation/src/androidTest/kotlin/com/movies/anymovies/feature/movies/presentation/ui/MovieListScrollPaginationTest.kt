package com.movies.anymovies.feature.movies.presentation.ui

import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.movies.domain.model.Movie
import com.movies.anymovies.feature.movies.domain.model.PagedResult
import com.movies.anymovies.feature.movies.domain.repository.MovieRepository
import com.movies.anymovies.feature.movies.domain.usecase.DiscoverMoviesUseCase
import com.movies.anymovies.feature.movies.domain.usecase.LoadNextMoviesPageUseCase
import com.movies.anymovies.feature.movies.presentation.MovieListViewModel
import com.movies.anymovies.feature.movies.presentation.databinding.ViewMovieListBinding
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

private fun movies(count: Int): List<Movie> = (1..count).map { index ->
    Movie(
        id = index,
        title = "Movie $index",
        posterUrl = null,
        backdropUrl = null,
        releaseYear = "2024",
        voteAverage = 7.0,
        voteCount = 10,
        overview = "",
    )
}

private class FakeMovieRepository(
    private val observeFlow: Flow<Result<PagedResult<Movie>>>,
) : MovieRepository {
    val loadNextPageCallCount = AtomicInteger(0)

    override fun observeMoviesByGenre(genreId: Int): Flow<Result<PagedResult<Movie>>> = observeFlow

    override suspend fun loadNextPage(genreId: Int): Result<Unit> {
        loadNextPageCallCount.incrementAndGet()
        return Result.Success(Unit)
    }

    override suspend fun refresh(genreId: Int): Result<Unit> = Result.Success(Unit)
}

@RunWith(AndroidJUnit4::class)
class MovieListScrollPaginationTest {

    @Test
    fun scrollingToTheBottomRequestsTheNextPageExactlyOnce() {
        val firstPage = PagedResult(items = movies(20), page = 1, totalPages = 3, totalResults = 60)
        val repository = FakeMovieRepository(observeFlow = flowOf(Result.Success(firstPage)))
        val viewModel = MovieListViewModel(
            genreId = 28,
            discoverMoviesUseCase = DiscoverMoviesUseCase(repository),
            loadNextMoviesPageUseCase = LoadNextMoviesPageUseCase(repository),
        )

        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            lateinit var binding: ViewMovieListBinding
            lateinit var renderer: MovieListViewRenderer

            scenario.onActivity { activity ->
                binding = ViewMovieListBinding.inflate(activity.layoutInflater)
                activity.setContentView(binding.root)
                val callbacks = MovieListCallbacks(
                    onMovieClick = {},
                    onRetry = {},
                    onRetryAppend = {},
                    onLoadNextPage = {
                        viewModel.loadNextPage()
                        renderer.render("Action", viewModel.uiState.value)
                    },
                    onBack = {},
                )
                renderer = MovieListViewRenderer(binding, callbacks)
                renderer.render("Action", viewModel.uiState.value)
            }

            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity {
                binding.movieRecyclerView.scrollBy(0, 100_000)
            }

            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        }

        assertEquals(1, repository.loadNextPageCallCount.get())
    }
}
