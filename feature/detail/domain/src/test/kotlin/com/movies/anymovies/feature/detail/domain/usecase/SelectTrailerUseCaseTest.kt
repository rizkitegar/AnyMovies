package com.movies.anymovies.feature.detail.domain.usecase

import com.movies.anymovies.feature.detail.domain.model.Video
import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SelectTrailerUseCaseTest {

    private val useCase = SelectTrailerUseCase()

    private fun video(
        id: String,
        type: String,
        site: String = "YouTube",
        official: Boolean = false,
        publishedAt: Instant? = null,
    ) = Video(
        id = id,
        key = "key-$id",
        name = "video-$id",
        site = site,
        type = type,
        official = official,
        publishedAt = publishedAt,
    )

    @Test
    fun `returns null when no YouTube video exists`() {
        val videos = listOf(
            video(id = "1", type = "Trailer", site = "Vimeo", official = true),
            video(id = "2", type = "Teaser", site = "Vimeo"),
        )

        assertNull(useCase(videos))
    }

    @Test
    fun `returns null when video list is empty`() {
        assertNull(useCase(emptyList()))
    }

    @Test
    fun `picks official trailer over any other tier`() {
        val official = video(id = "official", type = "Trailer", official = true)
        val videos = listOf(
            video(id = "teaser", type = "Teaser"),
            video(id = "trailer", type = "Trailer", official = false),
            official,
            video(id = "other", type = "Clip"),
        )

        assertEquals(official, useCase(videos))
    }

    @Test
    fun `picks non-official trailer when no official trailer exists`() {
        val trailer = video(id = "trailer", type = "Trailer", official = false)
        val videos = listOf(
            video(id = "teaser", type = "Teaser"),
            trailer,
            video(id = "other", type = "Clip"),
        )

        assertEquals(trailer, useCase(videos))
    }

    @Test
    fun `picks teaser when no trailer of any kind exists`() {
        val teaser = video(id = "teaser", type = "Teaser")
        val videos = listOf(
            teaser,
            video(id = "other", type = "Clip"),
            video(id = "featurette", type = "Featurette"),
        )

        assertEquals(teaser, useCase(videos))
    }

    @Test
    fun `falls back to first YouTube entry when no trailer or teaser exists`() {
        val firstClip = video(id = "clip1", type = "Clip")
        val videos = listOf(
            firstClip,
            video(id = "clip2", type = "Featurette"),
        )

        assertEquals(firstClip, useCase(videos))
    }

    @Test
    fun `ties within official trailer tier broken by most recent publishedAt`() {
        val older = video(
            id = "older",
            type = "Trailer",
            official = true,
            publishedAt = Instant.parse("2024-01-01T00:00:00Z"),
        )
        val newer = video(
            id = "newer",
            type = "Trailer",
            official = true,
            publishedAt = Instant.parse("2025-06-01T00:00:00Z"),
        )
        val videos = listOf(older, newer)

        assertEquals(newer, useCase(videos))
    }

    @Test
    fun `ties within fallback tier broken by most recent publishedAt`() {
        val older = video(
            id = "older",
            type = "Clip",
            publishedAt = Instant.parse("2024-01-01T00:00:00Z"),
        )
        val newer = video(
            id = "newer",
            type = "Featurette",
            publishedAt = Instant.parse("2025-06-01T00:00:00Z"),
        )
        val videos = listOf(older, newer)

        assertEquals(newer, useCase(videos))
    }

    @Test
    fun `non-YouTube entries are ignored even when higher priority`() {
        val youtubeTeaser = video(id = "teaser", type = "Teaser", site = "YouTube")
        val videos = listOf(
            video(id = "vimeo-official-trailer", type = "Trailer", site = "Vimeo", official = true),
            youtubeTeaser,
        )

        assertEquals(youtubeTeaser, useCase(videos))
    }
}
