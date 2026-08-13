package com.movies.anymovies.feature.detail.domain.repository

import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.detail.domain.model.MovieDetail
import kotlinx.coroutines.flow.Flow

public interface MovieDetailRepository {
    public fun observeMovieDetail(movieId: Int): Flow<Result<MovieDetail>>

    public suspend fun refresh(movieId: Int): Result<Unit>
}
