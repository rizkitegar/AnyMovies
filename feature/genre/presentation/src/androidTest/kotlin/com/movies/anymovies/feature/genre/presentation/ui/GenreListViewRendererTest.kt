package com.movies.anymovies.feature.genre.presentation.ui

import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.uilegacy.R as UiLegacyR
import com.movies.anymovies.feature.genre.domain.model.Genre
import com.movies.anymovies.feature.genre.presentation.GenreListUiState
import com.movies.anymovies.feature.genre.presentation.R
import com.movies.anymovies.feature.genre.presentation.databinding.ViewGenreListBinding
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GenreListViewRendererTest {

    @Test
    fun tappingRetryOnTheErrorStateInvokesTheRetryCallback() {
        val retryInvoked = AtomicBoolean(false)

        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val binding = ViewGenreListBinding.inflate(activity.layoutInflater)
                activity.setContentView(binding.root)
                val renderer = GenreListViewRenderer(
                    binding = binding,
                    callbacks = GenreListCallbacks(
                        onGenreClick = {},
                        onRetry = { retryInvoked.set(true) },
                        onRefresh = {},
                    ),
                )
                renderer.render(GenreListUiState.Error(DomainError.Server))
            }

            onView(withId(UiLegacyR.id.stateErrorRetryButton)).check(matches(isDisplayed()))
            onView(withId(UiLegacyR.id.stateErrorRetryButton)).perform(click())
        }

        assertTrue(retryInvoked.get())
    }

    @Test
    fun swipingDownOnTheGenreListInvokesTheRefreshCallback() {
        val refreshInvoked = AtomicBoolean(false)

        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val binding = ViewGenreListBinding.inflate(activity.layoutInflater)
                activity.setContentView(binding.root)
                val renderer = GenreListViewRenderer(
                    binding = binding,
                    callbacks = GenreListCallbacks(
                        onGenreClick = {},
                        onRetry = {},
                        onRefresh = { refreshInvoked.set(true) },
                    ),
                )
                renderer.render(GenreListUiState.Success(genres = listOf(Genre(id = 28, name = "Action"))))
            }

            onView(withId(R.id.genreSwipeRefresh)).check(matches(isDisplayed()))
            onView(withId(R.id.genreSwipeRefresh)).perform(swipeDown())
        }

        assertTrue(refreshInvoked.get())
    }
}
