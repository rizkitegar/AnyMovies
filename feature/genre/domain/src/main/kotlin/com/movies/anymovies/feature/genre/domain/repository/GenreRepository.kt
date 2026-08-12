package com.movies.anymovies.feature.genre.domain.repository

import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.genre.domain.model.Genre
import kotlinx.coroutines.flow.Flow

public interface GenreRepository {
    public fun observeGenres(): Flow<Result<List<Genre>>>

    public suspend fun refreshGenres(): Result<Unit>
}
