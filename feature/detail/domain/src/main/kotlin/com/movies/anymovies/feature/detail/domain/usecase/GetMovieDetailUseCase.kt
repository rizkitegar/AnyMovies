package com.movies.anymovies.feature.detail.domain.usecase

import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.detail.domain.model.MovieDetail
import com.movies.anymovies.feature.detail.domain.repository.MovieDetailRepository
import kotlinx.coroutines.flow.Flow

public class GetMovieDetailUseCase(
    private val movieDetailRepository: MovieDetailRepository,
) {
    public operator fun invoke(movieId: Int): Flow<Result<MovieDetail>> =
        movieDetailRepository.observeMovieDetail(movieId)
}
