package com.movies.anymovies.feature.detail.presentation.model

import com.movies.anymovies.feature.detail.domain.model.Review
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewUiModelTest {

    private val baseReview = Review(
        id = "abc123",
        author = "jane doe",
        username = "janedoe",
        avatarUrl = "https://image.tmdb.org/t/p/w185/avatar.jpg",
        rating = 8.0,
        content = "Great movie, loved every minute of it.",
        createdAt = Instant.parse("2026-08-12T00:00:00Z"),
    )

    @Test
    fun `maps author, avatar, rating and formatted date from the domain review`() {
        val uiModel = baseReview.toUiModel()

        assertEquals("jane doe", uiModel.author)
        assertEquals("https://image.tmdb.org/t/p/w185/avatar.jpg", uiModel.avatarUrl)
        assertEquals("8.0", uiModel.ratingLabel)
        assertEquals("12 Aug 2026", uiModel.createdDateLabel)
        assertEquals("Great movie, loved every minute of it.", uiModel.content)
    }

    @Test
    fun `a null rating maps to a null rating label rather than 0 or a placeholder string`() {
        val uiModel = baseReview.copy(rating = null).toUiModel()

        assertNull(uiModel.ratingLabel)
    }

    @Test
    fun `a null avatar url produces an uppercase initial letter fallback`() {
        val uiModel = baseReview.copy(avatarUrl = null).toUiModel()

        assertNull(uiModel.avatarUrl)
        assertEquals("J", uiModel.avatarInitial)
    }

    @Test
    fun `blank author falls back to a question mark initial instead of crashing`() {
        val uiModel = baseReview.copy(author = "").toUiModel()

        assertEquals("?", uiModel.avatarInitial)
    }

    @Test
    fun `an extremely long review body is truncated for display without ballooning the composed text`() {
        val longContent = "a".repeat(50_000)
        val uiModel = baseReview.copy(content = longContent).toUiModel()

        assertTrue(uiModel.content.length <= 2_001)
        assertTrue(uiModel.content.endsWith("…"))
    }

    @Test
    fun `a short review body is left untouched`() {
        val uiModel = baseReview.copy(content = "Short review.").toUiModel()

        assertEquals("Short review.", uiModel.content)
    }
}
