package com.movies.anymovies.feature.genre.domain.usecase

import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.feature.genre.domain.repository.GenreRepository

public class RefreshGenresUseCase(
    private val genreRepository: GenreRepository,
) {
    public suspend operator fun invoke(): Result<Unit> = genreRepository.refreshGenres()
}
