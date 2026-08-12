package com.movies.anymovies.feature.movies.domain.usecase

import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.movies.domain.repository.MovieRepository

public class LoadNextMoviesPageUseCase(
    private val movieRepository: MovieRepository,
) {
    public suspend operator fun invoke(genreId: Int): Result<Unit> = movieRepository.loadNextPage(genreId)
}
