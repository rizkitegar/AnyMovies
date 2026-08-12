package com.movies.anymovies.feature.movies.domain.usecase

import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.movies.domain.model.Movie
import com.movies.anymovies.feature.movies.domain.model.PagedResult
import com.movies.anymovies.feature.movies.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

public class DiscoverMoviesUseCase(
    private val movieRepository: MovieRepository,
) {
    public operator fun invoke(genreId: Int): Flow<Result<PagedResult<Movie>>> =
        movieRepository.observeMoviesByGenre(genreId)
}
