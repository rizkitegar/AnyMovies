package com.movies.anymovies.feature.detail.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.feature.detail.domain.model.Genre
import com.movies.anymovies.feature.detail.domain.model.MovieDetail
import com.movies.anymovies.feature.detail.presentation.model.toUiModel
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger
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

    @Test
    fun loadingStateShowsTheShimmerSkeleton() {
        composeRule.setContent {
            MovieDetailContent(
                state = MovieDetailUiState.Loading,
                onBack = {},
                onRetry = {},
                onSeeAllReviews = {},
            )
        }

        composeRule.onNodeWithTag(MovieDetailTestTags.LOADING).assertIsDisplayed()
    }

    @Test
    fun errorStateShowsTheMessageAndRetryInvokesTheCallback() {
        val retryCount = AtomicInteger(0)

        composeRule.setContent {
            MovieDetailContent(
                state = MovieDetailUiState.Error(DomainError.NoConnection, isRetryable = true),
                onBack = {},
                onRetry = { retryCount.incrementAndGet() },
                onSeeAllReviews = {},
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
            MovieDetailContent(
                state = MovieDetailUiState.Error(DomainError.NotFound, isRetryable = false),
                onBack = { backCount.incrementAndGet() },
                onRetry = {},
                onSeeAllReviews = {},
            )
        }

        composeRule.onNodeWithTag(MovieDetailTestTags.NOT_FOUND).assertIsDisplayed()
        composeRule.onNodeWithText("Back").performClick()

        assert(backCount.get() == 1)
    }

    @Test
    fun successStateRendersThePrimaryMovieInformation() {
        composeRule.setContent {
            MovieDetailContent(
                state = MovieDetailUiState.Success(movie = movieDetail.toUiModel()),
                onBack = {},
                onRetry = {},
                onSeeAllReviews = {},
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
            MovieDetailContent(
                state = MovieDetailUiState.Success(movie = movieDetail.toUiModel()),
                onBack = { backCount.incrementAndGet() },
                onRetry = {},
                onSeeAllReviews = {},
            )
        }

        composeRule.onNodeWithTag(MovieDetailTestTags.BACK_BUTTON).performClick()

        assert(backCount.get() == 1)
    }

    @Test
    fun longOverviewShowsAReadMoreToggleThatExpandsTheText() {
        val longOverview = "A mind-bending thriller. ".repeat(40)

        composeRule.setContent {
            MovieDetailContent(
                state = MovieDetailUiState.Success(movie = movieDetail.copy(overview = longOverview).toUiModel()),
                onBack = {},
                onRetry = {},
                onSeeAllReviews = {},
            )
        }

        composeRule.onNodeWithTag(MovieDetailTestTags.OVERVIEW_TOGGLE).assertIsDisplayed()
        composeRule.onNodeWithText("Read more").performClick()
        composeRule.onNodeWithText("Read less").assertIsDisplayed()
    }
}
