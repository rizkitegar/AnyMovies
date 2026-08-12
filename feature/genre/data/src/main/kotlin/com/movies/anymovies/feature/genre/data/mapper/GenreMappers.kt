package com.movies.anymovies.feature.genre.data.mapper

import com.movies.anymovies.core.database.genre.GenreEntity
import com.movies.anymovies.feature.genre.data.remote.dto.GenreDto
import com.movies.anymovies.feature.genre.domain.model.Genre

internal fun GenreDto.toEntity(): GenreEntity = GenreEntity(id = id, name = name)

internal fun GenreEntity.toDomain(): Genre = Genre(id = id, name = name)
