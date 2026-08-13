package com.movies.anymovies.feature.detail.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.compose.collectAsLazyPagingItems
import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.feature.detail.domain.model.Genre
import com.movies.anymovies.feature.detail.domain.model.MovieDetail
import com.movies.anymovies.feature.detail.presentation.model.ReviewUiModel
import com.movies.anymovies.feature.detail.presentation.model.toUiModel
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

class MovieDetailScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val movieDetail = MovieDetail(
        id = 27205,
        title = "Inception",
        tagline = "Your mind is the scene of the crime.",
        overview = "A mind-bending thriller.",
        posterUrl = "https://image.tmdb.org/t/p/w500/poster.jpg",
        backdropUrl = "https://image.tmdb.org/t/p/w780/backdrop.jpg",
        releaseDate = LocalDate.of(2026, 8, 12),
        runtimeMinutes = 148,
        genres = listOf(Genre(28, "Action")),
        voteAverage = 8.4,
        voteCount = 12304,
        status = "Released",
        originalLanguage = "en",
        homepage = "https://example.com",
        videos = emptyList(),
    )

    private fun review(id: String) = ReviewUiModel(
        id = id,
        author = "Author $id",
        avatarUrl = null,
        avatarInitial = "A",
        ratingLabel = "7.0",
        createdDateLabel = "12 Aug 2026",
        content = "Content $id",
    )

    private fun reviews(count: Int): List<ReviewUiModel> = (1..count).map { review("$it") }

    private fun reviewLoadStates(
        refresh: LoadState = LoadState.NotLoading(endOfPaginationReached = false),
        append: LoadState = LoadState.NotLoading(endOfPaginationReached = false),
    ) = LoadStates(refresh = refresh, prepend = LoadState.NotLoading(endOfPaginationReached = true), append = append)

    @Test
    fun loadingStateShowsTheShimmerSkeleton() {
        composeRule.setContent {
            val emptyReviews = flowOf(PagingData.empty<ReviewUiModel>()).collectAsLazyPagingItems()
            MovieDetailContent(
                state = MovieDetailUiState.Loading,
                lazyPagingReviews = emptyReviews,
                onBack = {},
                onRetry = {},
            )
        }

        composeRule.onNodeWithTag(MovieDetailTestTags.LOADING).assertIsDisplayed()
    }

    @Test
    fun errorStateShowsTheMessageAndRetryInvokesTheCallback() {
        val retryCount = AtomicInteger(0)

        composeRule.setContent {
            val emptyReviews = flowOf(PagingData.empty<ReviewUiModel>()).collectAsLazyPagingItems()
            MovieDetailContent(
                state = MovieDetailUiState.Error(DomainError.NoConnection, isRetryable = true),
                lazyPagingReviews = emptyReviews,
                onBack = {},
                onRetry = { retryCount.incrementAndGet() },
            )
        }

        composeRule.onNodeWithTag(MovieDetailTestTags.ERROR).assertIsDisplayed()
        composeRule.onNodeWithTag(MovieDetailTestTags.RETRY_BUTTON).performClick()

        assert(retryCount.get() == 1)
    }

    @Test
    fun notFoundStateShowsBackOnlyAndInvokesBack() {
        val backCount = AtomicInteger(0)

        composeRule.setContent {
            val emptyReviews = flowOf(PagingData.empty<ReviewUiModel>()).collectAsLazyPagingItems()
            MovieDetailContent(
                state = MovieDetailUiState.Error(DomainError.NotFound, isRetryable = false),
                lazyPagingReviews = emptyReviews,
                onBack = { backCount.incrementAndGet() },
                onRetry = {},
            )
        }

        composeRule.onNodeWithTag(MovieDetailTestTags.NOT_FOUND).assertIsDisplayed()
        composeRule.onNodeWithText("Back").performClick()

        assert(backCount.get() == 1)
    }

    @Test
    fun successStateRendersThePrimaryMovieInformation() {
        composeRule.setContent {
            val emptyReviews = flowOf(PagingData.empty<ReviewUiModel>()).collectAsLazyPagingItems()
            MovieDetailContent(
                state = MovieDetailUiState.Success(movie = movieDetail.toUiModel()),
                lazyPagingReviews = emptyReviews,
                onBack = {},
                onRetry = {},
            )
        }

        composeRule.onNodeWithTag(MovieDetailTestTags.SUCCESS).assertIsDisplayed()
        composeRule.onNodeWithText("Inception").assertIsDisplayed()
        composeRule.onNodeWithText("8.4 · 12,304 votes").assertIsDisplayed()
    }

    @Test
    fun successStateBackButtonInvokesTheCallback() {
        val backCount = AtomicInteger(0)

        composeRule.setContent {
            val emptyReviews = flowOf(PagingData.empty<ReviewUiModel>()).collectAsLazyPagingItems()
            MovieDetailContent(
                state = MovieDetailUiState.Success(movie = movieDetail.toUiModel()),
                lazyPagingReviews = emptyReviews,
                onBack = { backCount.incrementAndGet() },
                onRetry = {},
            )
        }

        composeRule.onNodeWithTag(MovieDetailTestTags.BACK_BUTTON).performClick()

        assert(backCount.get() == 1)
    }

    @Test
    fun longOverviewShowsAReadMoreToggleThatExpandsTheText() {
        val longOverview = "A mind-bending thriller. ".repeat(40)

        composeRule.setContent {
            val emptyReviews = flowOf(PagingData.empty<ReviewUiModel>()).collectAsLazyPagingItems()
            MovieDetailContent(
                state = MovieDetailUiState.Success(movie = movieDetail.copy(overview = longOverview).toUiModel()),
                lazyPagingReviews = emptyReviews,
                onBack = {},
                onRetry = {},
            )
        }

        composeRule.onNodeWithTag(MovieDetailTestTags.OVERVIEW_TOGGLE).assertIsDisplayed()
        composeRule.onNodeWithText("Read more").performClick()
        composeRule.onNodeWithText("Read less").assertIsDisplayed()
    }

    // --- Reviews, merged directly into the same scrollable content ---

    @Test
    fun reviewsHeaderAndItemsRenderAsPartOfTheSameScreen() {
        composeRule.setContent {
            val items = flowOf(PagingData.from(reviews(2))).collectAsLazyPagingItems()
            MovieDetailContent(
                state = MovieDetailUiState.Success(movie = movieDetail.toUiModel()),
                lazyPagingReviews = items,
                onBack = {},
                onRetry = {},
            )
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(MovieDetailTestTags.REVIEWS_HEADER).assertIsDisplayed()
        composeRule.onNodeWithTag(MovieDetailTestTags.reviewItem("1")).assertIsDisplayed()
    }

    @Test
    fun reviewsLoadingShimmerRendersInPlaceOfTheReviewItems() {
        composeRule.setContent {
            val items = flowOf(
                PagingData.empty<ReviewUiModel>(sourceLoadStates = reviewLoadStates(refresh = LoadState.Loading)),
            ).collectAsLazyPagingItems()
            MovieDetailContent(
                state = MovieDetailUiState.Success(movie = movieDetail.toUiModel()),
                lazyPagingReviews = items,
                onBack = {},
                onRetry = {},
            )
        }

        composeRule.onNodeWithTag(MovieDetailTestTags.REVIEWS_LOADING).assertIsDisplayed()
    }

    @Test
    fun reviewsEmptyStateShowsTheNoReviewsMessageWithoutBlockingTheRestOfTheScreen() {
        composeRule.setContent {
            val items = flowOf(
                PagingData.empty<ReviewUiModel>(sourceLoadStates = reviewLoadStates(refresh = LoadState.NotLoading(endOfPaginationReached = true))),
            ).collectAsLazyPagingItems()
            MovieDetailContent(
                state = MovieDetailUiState.Success(movie = movieDetail.toUiModel()),
                lazyPagingReviews = items,
                onBack = {},
                onRetry = {},
            )
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(MovieDetailTestTags.REVIEWS_EMPTY).assertIsDisplayed()
        composeRule.onNodeWithText("Inception").assertIsDisplayed()
    }

    @Test
    fun reviewsErrorIsScopedToTheReviewsPortionAndDoesNotBlockTheMovieInfoAboveIt() {
        composeRule.setContent {
            val items = flowOf(
                PagingData.empty<ReviewUiModel>(sourceLoadStates = reviewLoadStates(refresh = LoadState.Error(RuntimeException("boom")))),
            ).collectAsLazyPagingItems()
            MovieDetailContent(
                state = MovieDetailUiState.Success(movie = movieDetail.toUiModel()),
                lazyPagingReviews = items,
                onBack = {},
                onRetry = {},
            )
        }
        composeRule.waitForIdle()

        // Movie info above the reviews block is still fully usable.
        composeRule.onNodeWithText("Inception").assertIsDisplayed()
        composeRule.onNodeWithTag(MovieDetailTestTags.REVIEWS_ERROR).assertIsDisplayed()
        composeRule.onNodeWithTag(MovieDetailTestTags.REVIEWS_RETRY_BUTTON).assertIsDisplayed()
    }

    @Test
    fun reviewsAppendFooterRendersAfterTheLastReviewItem() {
        composeRule.setContent {
            val items = flowOf(
                PagingData.from(reviews(2), sourceLoadStates = reviewLoadStates(append = LoadState.Loading)),
            ).collectAsLazyPagingItems()
            MovieDetailContent(
                state = MovieDetailUiState.Success(movie = movieDetail.toUiModel()),
                lazyPagingReviews = items,
                onBack = {},
                onRetry = {},
            )
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(MovieDetailTestTags.reviewItem("2")).assertIsDisplayed()
        composeRule.onNodeWithTag(MovieDetailTestTags.REVIEWS_APPEND_LOADING).assertIsDisplayed()
    }

    @Test
    fun reachingTheLastReviewPageShowsAnEndOfListFooter() {
        composeRule.setContent {
            val items = flowOf(
                PagingData.from(reviews(2), sourceLoadStates = reviewLoadStates(append = LoadState.NotLoading(endOfPaginationReached = true))),
            ).collectAsLazyPagingItems()
            MovieDetailContent(
                state = MovieDetailUiState.Success(movie = movieDetail.toUiModel()),
                lazyPagingReviews = items,
                onBack = {},
                onRetry = {},
            )
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(MovieDetailTestTags.REVIEWS_END_REACHED).assertIsDisplayed()
    }

    @Test
    fun scrollingFromMovieInfoIntoTheReviewsPortionAutomaticallyLoadsTheNextPageAndAFailedAppendCanBeRetried() {
        val pagingSource = FlakyAppendPagingSource(failFirstAppend = true)
        val pager = Pager(
            config = PagingConfig(pageSize = 20, prefetchDistance = 4, enablePlaceholders = false),
            pagingSourceFactory = { pagingSource },
        )

        composeRule.setContent {
            val items = pager.flow.collectAsLazyPagingItems()
            MovieDetailContent(
                state = MovieDetailUiState.Success(movie = movieDetail.toUiModel()),
                lazyPagingReviews = items,
                onBack = {},
                onRetry = {},
            )
        }
        composeRule.waitForIdle()

        // header(0) + body(1) + reviews_header(2) + 20 review items -> scroll well past the
        // review items so the outer LazyColumn (not a nested one) drives the append trigger.
        composeRule.onNodeWithTag(MovieDetailTestTags.CONTENT_LIST).performScrollToIndex(25)
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(MovieDetailTestTags.REVIEWS_APPEND_ERROR).assertIsDisplayed()
        assert(pagingSource.appendAttempts == 1)

        composeRule.onNodeWithTag(MovieDetailTestTags.REVIEWS_APPEND_RETRY_BUTTON).performClick()
        composeRule.waitForIdle()

        assert(pagingSource.appendAttempts == 2)
        composeRule.onNodeWithTag(MovieDetailTestTags.REVIEWS_APPEND_ERROR).assertDoesNotExist()
    }
}

/**
 * Page 1 always succeeds (20 items). The first attempt at page 2 fails once (if configured),
 * so tests can drive a real "append error, retry, success" cycle through [PagingSource.load].
 */
internal class FlakyAppendPagingSource(private val failFirstAppend: Boolean) : PagingSource<Int, ReviewUiModel>() {

    var appendAttempts: Int = 0
        private set

    override fun getRefreshKey(state: PagingState<Int, ReviewUiModel>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ReviewUiModel> {
        val page = params.key ?: 1
        if (page == 2) {
            appendAttempts++
            if (failFirstAppend && appendAttempts == 1) {
                return LoadResult.Error(RuntimeException("boom"))
            }
        }
        val items = if (page == 1) {
            (1..20).map { index ->
                ReviewUiModel(
                    id = "p1-$index",
                    author = "Author $index",
                    avatarUrl = null,
                    avatarInitial = "A",
                    ratingLabel = "7.0",
                    createdDateLabel = "12 Aug 2026",
                    content = "Content p1-$index",
                )
            }
        } else {
            listOf(
                ReviewUiModel(
                    id = "p2-1",
                    author = "Author p2-1",
                    avatarUrl = null,
                    avatarInitial = "A",
                    ratingLabel = "7.0",
                    createdDateLabel = "12 Aug 2026",
                    content = "Content p2-1",
                ),
            )
        }
        return LoadResult.Page(
            data = items,
            prevKey = if (page == 1) null else 1,
            nextKey = if (page == 1) 2 else null,
        )
    }
}
