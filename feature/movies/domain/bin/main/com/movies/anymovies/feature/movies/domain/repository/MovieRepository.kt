package com.movies.anymovies.feature.movies.domain.repository

import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.movies.domain.model.Movie
import com.movies.anymovies.feature.movies.domain.model.PagedResult
import kotlinx.coroutines.flow.Flow

public interface MovieRepository {
    public fun observeMoviesByGenre(genreId: Int): Flow<Result<PagedResult<Movie>>>

    public suspend fun loadNextPage(genreId: Int): Result<Unit>

    public suspend fun refresh(genreId: Int): Result<Unit>
}
