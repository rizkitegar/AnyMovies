package com.movies.anymovies.feature.detail.domain.usecase

import com.movies.anymovies.feature.detail.domain.model.Video
import java.time.Instant

public class SelectTrailerUseCase {
    public operator fun invoke(videos: List<Video>): Video? {
        val youtubeVideos = videos.filter { it.site == "YouTube" }
        if (youtubeVideos.isEmpty()) return null

        val officialTrailers = youtubeVideos.filter { it.official && it.type == "Trailer" }
        if (officialTrailers.isNotEmpty()) return officialTrailers.mostRecent()

        val trailers = youtubeVideos.filter { it.type == "Trailer" }
        if (trailers.isNotEmpty()) return trailers.mostRecent()

        val teasers = youtubeVideos.filter { it.type == "Teaser" }
        if (teasers.isNotEmpty()) return teasers.mostRecent()

        return youtubeVideos.mostRecent()
    }

    private fun List<Video>.mostRecent(): Video =
        maxByOrNull { it.publishedAt ?: Instant.MIN } ?: first()
}
