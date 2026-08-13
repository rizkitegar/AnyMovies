package com.movies.anymovies.feature.detail.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.feature.detail.presentation.model.ReviewUiModel
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.collections.immutable.toImmutableList
import org.junit.Rule
import org.junit.Test

class MovieReviewsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun reviews(count: Int): List<ReviewUiModel> = (1..count).map { index ->
        ReviewUiModel(
            id = "$index",
            author = "Author $index",
            avatarUrl = null,
            avatarInitial = "A",
            ratingLabel = "7.0",
            createdDateLabel = "12 Aug 2026",
            content = "Content $index",
        )
    }

    @Test
    fun appendingShowsALoadingFooterAtTheEndOfTheList() {
        composeRule.setContent {
            MovieReviewsContent(
                state = MovieReviewsUiState.Success(
                    reviews = reviews(2).toImmutableList(),
                    isAppending = true,
                ),
                onBack = {},
                onRetry = {},
                onRetryAppend = {},
                onLoadNextPage = {},
            )
        }

        composeRule.onNodeWithTag(MovieReviewsTestTags.APPEND_LOADING).assertIsDisplayed()
    }

    @Test
    fun anAppendFailureShowsAFooterErrorWithARetryThatInvokesTheCallback() {
        val retryCount = AtomicInteger(0)

        composeRule.setContent {
            MovieReviewsContent(
                state = MovieReviewsUiState.Success(
                    reviews = reviews(2).toImmutableList(),
                    appendError = DomainError.Server,
                ),
                onBack = {},
                onRetry = {},
                onRetryAppend = { retryCount.incrementAndGet() },
                onLoadNextPage = {},
            )
        }

        composeRule.onNodeWithTag(MovieReviewsTestTags.APPEND_ERROR).assertIsDisplayed()
        composeRule.onNodeWithTag(MovieReviewsTestTags.APPEND_RETRY_BUTTON).performClick()

        assert(retryCount.get() == 1)
    }

    @Test
    fun reachingTheLastPageShowsAnEndOfListFooter() {
        composeRule.setContent {
            MovieReviewsContent(
                state = MovieReviewsUiState.Success(
                    reviews = reviews(2).toImmutableList(),
                    endReached = true,
                ),
                onBack = {},
                onRetry = {},
                onRetryAppend = {},
                onLoadNextPage = {},
            )
        }

        composeRule.onNodeWithTag(MovieReviewsTestTags.END_REACHED).assertIsDisplayed()
    }

    @Test
    fun scrollingNearTheEndOfTheListTriggersTheNextPageRequest() {
        val loadNextPageCount = AtomicInteger(0)
        val items = reviews(30)

        composeRule.setContent {
            MovieReviewsContent(
                state = MovieReviewsUiState.Success(reviews = items.toImmutableList()),
                onBack = {},
                onRetry = {},
                onRetryAppend = {},
                onLoadNextPage = { loadNextPageCount.incrementAndGet() },
            )
        }

        composeRule.onNodeWithTag(MovieReviewsTestTags.LIST).performScrollToIndex(items.size - 1)
        composeRule.waitForIdle()

        assert(loadNextPageCount.get() > 0)
    }

    @Test
    fun errorStateShowsARetryThatInvokesTheCallback() {
        val retryCount = AtomicInteger(0)

        composeRule.setContent {
            MovieReviewsContent(
                state = MovieReviewsUiState.Error(DomainError.NoConnection),
                onBack = {},
                onRetry = { retryCount.incrementAndGet() },
                onRetryAppend = {},
                onLoadNextPage = {},
            )
        }

        composeRule.onNodeWithTag(MovieReviewsTestTags.ERROR).assertIsDisplayed()
        composeRule.onNodeWithTag(MovieReviewsTestTags.RETRY_BUTTON).performClick()

        assert(retryCount.get() == 1)
    }

    @Test
    fun emptyStateShowsTheNoReviewsMessage() {
        composeRule.setContent {
            MovieReviewsContent(
                state = MovieReviewsUiState.Empty,
                onBack = {},
                onRetry = {},
                onRetryAppend = {},
                onLoadNextPage = {},
            )
        }

        composeRule.onNodeWithTag(MovieReviewsTestTags.EMPTY).assertIsDisplayed()
    }

    @Test
    fun backButtonInvokesTheCallback() {
        val backCount = AtomicInteger(0)

        composeRule.setContent {
            MovieReviewsContent(
                state = MovieReviewsUiState.Loading,
                onBack = { backCount.incrementAndGet() },
                onRetry = {},
                onRetryAppend = {},
                onLoadNextPage = {},
            )
        }

        composeRule.onNodeWithTag(MovieReviewsTestTags.BACK_BUTTON).performClick()

        assert(backCount.get() == 1)
    }
}
