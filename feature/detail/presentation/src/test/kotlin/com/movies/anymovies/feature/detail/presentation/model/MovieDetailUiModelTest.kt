package com.movies.anymovies.feature.detail.presentation.model

import com.movies.anymovies.feature.detail.domain.model.Genre
import com.movies.anymovies.feature.detail.domain.model.MovieDetail
import com.movies.anymovies.feature.detail.domain.model.Video
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MovieDetailUiModelTest {

    private fun fullDetail(
        tagline: String? = "Your mind is the scene of the crime.",
        overview: String = "A mind-bending thriller.",
        posterUrl: String? = "https://image.tmdb.org/t/p/w500/poster.jpg",
        backdropUrl: String? = "https://image.tmdb.org/t/p/w780/backdrop.jpg",
        releaseDate: LocalDate? = LocalDate.of(2026, 8, 12),
        runtimeMinutes: Int? = 148,
        homepage: String? = "https://example.com",
    ) = MovieDetail(
        id = 27205,
        title = "Inception",
        tagline = tagline,
        overview = overview,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        releaseDate = releaseDate,
        runtimeMinutes = runtimeMinutes,
        genres = listOf(Genre(28, "Action"), Genre(12, "Adventure")),
        voteAverage = 8.4,
        voteCount = 12304,
        status = "Released",
        originalLanguage = "en",
        homepage = homepage,
        videos = emptyList(),
    )

    @Test
    fun `maps every primary field for display`() {
        val uiModel = fullDetail().toUiModel()

        assertEquals(27205, uiModel.id)
        assertEquals("Inception", uiModel.title)
        assertEquals("Your mind is the scene of the crime.", uiModel.tagline)
        assertEquals("A mind-bending thriller.", uiModel.overviewLabel)
        assertEquals("https://image.tmdb.org/t/p/w500/poster.jpg", uiModel.posterUrl)
        assertEquals("https://image.tmdb.org/t/p/w780/backdrop.jpg", uiModel.headerImageUrl)
        assertEquals("12 Aug 2026", uiModel.releaseDateLabel)
        assertEquals("2h 28m", uiModel.runtimeLabel)
        assertEquals(listOf(GenreUiModel(28, "Action"), GenreUiModel(12, "Adventure")), uiModel.genres)
        assertEquals("8.4 · 12,304 votes", uiModel.ratingLabel)
        assertEquals("Released", uiModel.status)
        assertEquals("EN", uiModel.originalLanguageLabel)
        assertEquals("https://example.com", uiModel.homepage)
    }

    @Test
    fun `runtime formats as hours and minutes`() {
        assertEquals("2h 28m", fullDetail(runtimeMinutes = 148).toUiModel().runtimeLabel)
        assertEquals("0h 45m", fullDetail(runtimeMinutes = 45).toUiModel().runtimeLabel)
    }

    @Test
    fun `runtime chip is hidden when runtime is null`() {
        assertNull(fullDetail(runtimeMinutes = null).toUiModel().runtimeLabel)
    }

    @Test
    fun `runtime chip is hidden when runtime is zero`() {
        assertNull(fullDetail(runtimeMinutes = 0).toUiModel().runtimeLabel)
    }

    @Test
    fun `blank overview falls back to the no-synopsis message`() {
        assertEquals("No synopsis available", fullDetail(overview = "").toUiModel().overviewLabel)
        assertEquals("No synopsis available", fullDetail(overview = "   ").toUiModel().overviewLabel)
    }

    @Test
    fun `blank tagline is dropped rather than shown empty`() {
        assertNull(fullDetail(tagline = "").toUiModel().tagline)
        assertNull(fullDetail(tagline = null).toUiModel().tagline)
    }

    @Test
    fun `null release date hides the release date label`() {
        assertNull(fullDetail(releaseDate = null).toUiModel().releaseDateLabel)
    }

    @Test
    fun `header image falls back to the poster when the backdrop is missing`() {
        val uiModel = fullDetail(backdropUrl = null, posterUrl = "https://image.tmdb.org/t/p/w500/poster.jpg").toUiModel()
        assertEquals("https://image.tmdb.org/t/p/w500/poster.jpg", uiModel.headerImageUrl)
    }

    @Test
    fun `header image is null when both backdrop and poster are missing`() {
        val uiModel = fullDetail(backdropUrl = null, posterUrl = null).toUiModel()
        assertNull(uiModel.headerImageUrl)
    }

    @Test
    fun `blank homepage is dropped rather than shown as a dead link`() {
        assertNull(fullDetail(homepage = "").toUiModel().homepage)
    }

    @Test
    fun `zero votes still render a rating label rather than hiding it`() {
        val detail = fullDetail().copy(voteAverage = 0.0, voteCount = 0)
        assertEquals("0.0 · 0 votes", detail.toUiModel().ratingLabel)
    }

    @Test
    fun `no videos yields an empty video list and no selected trailer`() {
        val uiModel = fullDetail().toUiModel()

        assertTrue(uiModel.videos.isEmpty())
        assertNull(uiModel.selectedTrailerKey)
    }

    @Test
    fun `selected trailer key follows the official trailer priority`() {
        val teaser = Video(
            id = "1",
            key = "teaserKey",
            name = "Teaser",
            site = "YouTube",
            type = "Teaser",
            official = false,
            publishedAt = Instant.parse("2026-01-01T00:00:00Z"),
        )
        val officialTrailer = Video(
            id = "2",
            key = "trailerKey",
            name = "Official Trailer",
            site = "YouTube",
            type = "Trailer",
            official = true,
            publishedAt = Instant.parse("2026-02-01T00:00:00Z"),
        )
        val detail = fullDetail().copy(videos = listOf(teaser, officialTrailer))

        val uiModel = detail.toUiModel()

        assertEquals("trailerKey", uiModel.selectedTrailerKey)
        assertEquals(2, uiModel.videos.size)
        assertEquals("https://img.youtube.com/vi/trailerKey/mqdefault.jpg", uiModel.videos[1].thumbnailUrl)
    }
}
