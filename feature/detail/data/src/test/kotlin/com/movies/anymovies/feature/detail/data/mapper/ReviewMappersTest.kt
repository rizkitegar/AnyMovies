package com.movies.anymovies.feature.detail.data.mapper

import com.movies.anymovies.feature.detail.data.remote.dto.AuthorDetailsDto
import com.movies.anymovies.feature.detail.data.remote.dto.ReviewDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ReviewMappersTest {

    @Test
    fun `toDomain strips html tags and decodes named and numeric entities`() {
        val dto = ReviewDto(
            id = "r1",
            author = "rina27",
            authorDetails = AuthorDetailsDto(name = "Rina", username = "rina27"),
            content = "<p>Great movie &amp; a &lt;must-watch&gt;! Rating: 5&#33; &#x2764;</p>",
            createdAt = "2026-01-01T00:00:00.000Z",
        )

        val review = dto.toDomain()

        assertEquals("Great movie & a <must-watch>! Rating: 5! ❤", review.content)
    }

    @Test
    fun `toDomain truncates content longer than 10000 characters without crashing`() {
        val longContent = "a".repeat(10_500)
        val dto = ReviewDto(id = "r1", content = longContent)

        val review = dto.toDomain()

        assertEquals(10_000, review.content.length)
    }

    @Test
    fun `toDomain maps null author rating to null instead of zero`() {
        val dto = ReviewDto(
            id = "r1",
            authorDetails = AuthorDetailsDto(name = "Rina", username = "rina27", rating = null),
            content = "Solid film.",
        )

        val review = dto.toDomain()

        assertNull(review.rating)
    }

    @Test
    fun `toDomain preserves a present author rating`() {
        val dto = ReviewDto(
            id = "r1",
            authorDetails = AuthorDetailsDto(name = "Rina", username = "rina27", rating = 8.0),
            content = "Solid film.",
        )

        val review = dto.toDomain()

        assertEquals(8.0, review.rating)
    }

    @Test
    fun `toDomain maps external gravatar avatar path without prefixing tmdb base url`() {
        val dto = ReviewDto(
            id = "r1",
            authorDetails = AuthorDetailsDto(
                name = "Rina",
                username = "rina27",
                avatarPath = "/https://gravatar.com/avatar/abc.jpg",
            ),
            content = "Solid film.",
        )

        val review = dto.toDomain()

        assertEquals("https://gravatar.com/avatar/abc.jpg", review.avatarUrl)
    }

    @Test
    fun `toDomain prefixes tmdb-hosted avatar path with the image base url`() {
        val dto = ReviewDto(
            id = "r1",
            authorDetails = AuthorDetailsDto(name = "Rina", username = "rina27", avatarPath = "/abc123.jpg"),
            content = "Solid film.",
        )

        val review = dto.toDomain()

        assertEquals("https://image.tmdb.org/t/p/w185/abc123.jpg", review.avatarUrl)
    }

    @Test
    fun `toDomain maps null avatar path to null`() {
        val dto = ReviewDto(
            id = "r1",
            authorDetails = AuthorDetailsDto(name = "Rina", username = "rina27", avatarPath = null),
            content = "Solid film.",
        )

        val review = dto.toDomain()

        assertNull(review.avatarUrl)
    }

    @Test
    fun `toDomain falls back to epoch instant on unparsable created_at`() {
        val dto = ReviewDto(id = "r1", content = "Solid film.", createdAt = "not-a-date")

        val review = dto.toDomain()

        assertTrue(review.createdAt.epochSecond == 0L)
    }
}
