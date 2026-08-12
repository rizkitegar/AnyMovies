package com.movies.anymovies.feature.detail.data.mapper

import com.movies.anymovies.core.database.moviedetail.MovieDetailEntity
import com.movies.anymovies.core.database.moviedetail.VideoEntity
import com.movies.anymovies.feature.detail.data.remote.dto.GenreDto
import com.movies.anymovies.feature.detail.data.remote.dto.MovieDetailDto
import com.movies.anymovies.feature.detail.data.remote.dto.VideoDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class MovieDetailMappersTest {

    private fun entity(
        overview: String = "An overview",
        posterPath: String? = "/poster.jpg",
        backdropPath: String? = "/backdrop.jpg",
        runtime: Int? = 120,
        genreIds: String = "",
        genreNames: String = "",
    ) = MovieDetailEntity(
        id = 1,
        title = "Title",
        tagline = "Tagline",
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = "2024-05-01",
        runtime = runtime,
        genreIds = genreIds,
        genreNames = genreNames,
        voteAverage = 7.5,
        voteCount = 100,
        status = "Released",
        originalLanguage = "en",
        homepage = "https://example.com",
        fetchedAtMillis = 0L,
    )

    @Test
    fun `toEntity coerces null overview to empty string`() {
        val dto = MovieDetailDto(id = 1, overview = null)

        val result = dto.toEntity(fetchedAtMillis = 0L)

        assertEquals("", result.overview)
    }

    @Test
    fun `toDomain falls back to poster when backdrop is null`() {
        val result = entity(backdropPath = null, posterPath = "/poster.jpg").toDomain(emptyList())

        assertEquals("https://image.tmdb.org/t/p/w500/poster.jpg", result.backdropUrl)
    }

    @Test
    fun `toDomain returns null header when both backdrop and poster are null`() {
        val result = entity(backdropPath = null, posterPath = null).toDomain(emptyList())

        assertNull(result.backdropUrl)
    }

    @Test
    fun `toDomain keeps overview as empty string rather than null`() {
        val result = entity(overview = "").toDomain(emptyList())

        assertEquals("", result.overview)
    }

    @Test
    fun `toDomain hides runtime chip when runtime is null`() {
        val result = entity(runtime = null).toDomain(emptyList())

        assertNull(result.runtimeMinutes)
    }

    @Test
    fun `toDomain hides runtime chip when runtime is zero`() {
        val result = entity(runtime = 0).toDomain(emptyList())

        assertNull(result.runtimeMinutes)
    }

    @Test
    fun `toDomain keeps a positive runtime`() {
        val result = entity(runtime = 128).toDomain(emptyList())

        assertEquals(128, result.runtimeMinutes)
    }

    @Test
    fun `toEntity round trips genres through delimited columns`() {
        val dto = MovieDetailDto(id = 1, genres = listOf(GenreDto(id = 28, name = "Action"), GenreDto(id = 12, name = "Adventure")))

        val result = dto.toEntity(fetchedAtMillis = 0L).toDomain(emptyList())

        assertEquals(listOf(28 to "Action", 12 to "Adventure"), result.genres.map { it.id to it.name })
    }

    @Test
    fun `video toDomain returns null publishedAt on unparseable date`() {
        val videoEntity = VideoEntity(
            id = "v1",
            movieId = 1,
            key = "abc",
            name = "Trailer",
            site = "YouTube",
            type = "Trailer",
            official = true,
            publishedAt = "not-a-date",
            orderIndex = 0,
        )

        val result = videoEntity.toDomain()

        assertNull(result.publishedAt)
    }

    @Test
    fun `video dto with null published_at maps safely`() {
        val dto = VideoDto(id = "v1", publishedAt = null)

        val result = dto.toEntity(movieId = 1, orderIndex = 0)

        assertNull(result.publishedAt)
    }
}
