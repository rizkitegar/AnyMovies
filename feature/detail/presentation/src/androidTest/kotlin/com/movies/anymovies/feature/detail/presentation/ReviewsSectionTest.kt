package com.movies.anymovies.feature.detail.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.feature.detail.presentation.model.ReviewUiModel
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.collections.immutable.toImmutableList
import org.junit.Rule
import org.junit.Test

class ReviewsSectionTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun reviewUiModel(id: String, ratingLabel: String? = "7.0") = ReviewUiModel(
        id = id,
        author = "Author $id",
        avatarUrl = null,
        avatarInitial = "A",
        ratingLabel = ratingLabel,
        createdDateLabel = "12 Aug 2026",
        content = "Content for review $id",
    )

    @Test
    fun successStateShowsUpToThreeReviewsWithTheTotalCountInSeeAll() {
        val reviews = listOf(reviewUiModel("a"), reviewUiModel("b"), reviewUiModel("c")).toImmutableList()

        composeRule.setContent {
            ReviewsSectionContent(
                state = ReviewsSectionUiState.Success(reviews = reviews, totalResults = 42),
                onSeeAllReviews = {},
                onRetry = {},
            )
        }

        composeRule.onNodeWithTag(ReviewsSectionTestTags.reviewItem("a")).assertIsDisplayed()
        composeRule.onNodeWithTag(ReviewsSectionTestTags.reviewItem("b")).assertIsDisplayed()
        composeRule.onNodeWithTag(ReviewsSectionTestTags.reviewItem("c")).assertIsDisplayed()
        composeRule.onNodeWithText("See all (42)").assertIsDisplayed()
    }

    @Test
    fun seeAllAffordanceInvokesTheCallback() {
        val seeAllCount = AtomicInteger(0)
        val reviews = listOf(reviewUiModel("a")).toImmutableList()

        composeRule.setContent {
            ReviewsSectionContent(
                state = ReviewsSectionUiState.Success(reviews = reviews, totalResults = 1),
                onSeeAllReviews = { seeAllCount.incrementAndGet() },
                onRetry = {},
            )
        }

        composeRule.onNodeWithTag(ReviewsSectionTestTags.SEE_ALL).performClick()

        assert(seeAllCount.get() == 1)
    }

    @Test
    fun aReviewWithNoRatingHidesTheRatingChip() {
        val reviews = listOf(reviewUiModel("a", ratingLabel = null)).toImmutableList()

        composeRule.setContent {
            ReviewsSectionContent(
                state = ReviewsSectionUiState.Success(reviews = reviews, totalResults = 1),
                onSeeAllReviews = {},
                onRetry = {},
            )
        }

        composeRule.onNodeWithTag(ReviewsSectionTestTags.ratingChip("a")).assertDoesNotExist()
    }

    @Test
    fun emptyStateShowsTheMessageAndHidesTheSeeAllAffordance() {
        composeRule.setContent {
            ReviewsSectionContent(
                state = ReviewsSectionUiState.Empty,
                onSeeAllReviews = {},
                onRetry = {},
            )
        }

        composeRule.onNodeWithTag(ReviewsSectionTestTags.EMPTY).assertIsDisplayed()
        composeRule.onNodeWithTag(ReviewsSectionTestTags.SEE_ALL).assertDoesNotExist()
    }

    @Test
    fun errorStateShowsARetryScopedToTheSectionOnly() {
        val retryCount = AtomicInteger(0)

        composeRule.setContent {
            ReviewsSectionContent(
                state = ReviewsSectionUiState.Error(DomainError.Server),
                onSeeAllReviews = {},
                onRetry = { retryCount.incrementAndGet() },
            )
        }

        composeRule.onNodeWithTag(ReviewsSectionTestTags.ERROR).assertIsDisplayed()
        composeRule.onNodeWithTag(ReviewsSectionTestTags.RETRY_BUTTON).performClick()

        assert(retryCount.get() == 1)
    }

    @Test
    fun loadingStateShowsALoadingIndicator() {
        composeRule.setContent {
            ReviewsSectionContent(
                state = ReviewsSectionUiState.Loading,
                onSeeAllReviews = {},
                onRetry = {},
            )
        }

        composeRule.onNodeWithTag(ReviewsSectionTestTags.LOADING).assertIsDisplayed()
    }
}
