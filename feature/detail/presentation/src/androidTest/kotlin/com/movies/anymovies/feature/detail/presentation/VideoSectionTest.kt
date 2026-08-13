package com.movies.anymovies.feature.detail.presentation

import android.content.ActivityNotFoundException
import android.content.ContextWrapper
import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.core.app.ApplicationProvider
import com.movies.anymovies.feature.detail.domain.model.Genre
import com.movies.anymovies.feature.detail.domain.model.MovieDetail
import com.movies.anymovies.feature.detail.presentation.model.ReviewUiModel
import com.movies.anymovies.feature.detail.presentation.model.toUiModel
import java.time.LocalDate
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test

class VideoSectionTest {

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
    fun noVideosHidesThePlayAffordanceAndTheThumbnailRow() {
        composeRule.setContent {
            val emptyReviews = flowOf(PagingData.empty<ReviewUiModel>()).collectAsLazyPagingItems()
            MovieDetailContent(
                state = MovieDetailUiState.Success(movie = movieDetail.toUiModel()),
                lazyPagingReviews = emptyReviews,
                onBack = {},
                onRetry = {},
            )
        }

        composeRule.onNodeWithTag(VideoSectionTestTags.PLAY_BUTTON).assertDoesNotExist()
        composeRule.onNodeWithTag(VideoSectionTestTags.THUMBNAIL_ROW).assertDoesNotExist()
    }

    @Test
    fun activityNotFoundExceptionShowsASnackbarInsteadOfCrashing() {
        val fakeContext = object : ContextWrapper(ApplicationProvider.getApplicationContext()) {
            override fun startActivity(intent: Intent) {
                throw ActivityNotFoundException()
            }
        }

        composeRule.setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            CompositionLocalProvider(LocalContext provides fakeContext) {
                val context = LocalContext.current
                Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
                    Button(
                        onClick = {
                            if (!launchYoutube(context, "dQw4w9WgXcQ")) {
                                scope.launch { snackbarHostState.showSnackbar(COULD_NOT_OPEN_YOUTUBE_MESSAGE) }
                            }
                        },
                        modifier = Modifier.padding(padding).testTag("open_youtube_trigger"),
                    ) {
                        Text("Open in YouTube")
                    }
                }
            }
        }

        composeRule.onNodeWithTag("open_youtube_trigger").performClick()

        composeRule.onNodeWithText(COULD_NOT_OPEN_YOUTUBE_MESSAGE).assertIsDisplayed()
    }
}
