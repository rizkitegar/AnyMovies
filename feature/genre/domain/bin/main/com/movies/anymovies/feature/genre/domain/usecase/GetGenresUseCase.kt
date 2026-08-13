package com.movies.anymovies.feature.genre.domain.usecase

import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.genre.domain.model.Genre
import com.movies.anymovies.feature.genre.domain.repository.GenreRepository
import kotlinx.coroutines.flow.Flow

public class GetGenresUseCase(
    private val genreRepository: GenreRepository,
) {
    public operator fun invoke(): Flow<Result<List<Genre>>> = genreRepository.observeGenres()
}
