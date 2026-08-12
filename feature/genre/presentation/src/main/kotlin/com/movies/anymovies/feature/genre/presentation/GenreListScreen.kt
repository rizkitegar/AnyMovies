package com.movies.anymovies.feature.genre.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movies.anymovies.feature.genre.domain.model.Genre
import com.movies.anymovies.feature.genre.presentation.databinding.ViewGenreListBinding
import com.movies.anymovies.feature.genre.presentation.ui.GenreListCallbacks
import com.movies.anymovies.feature.genre.presentation.ui.GenreListViewRenderer
import org.koin.androidx.compose.koinViewModel

@Composable
public fun GenreListScreen(
    onGenreClick: (genreId: Int, genreName: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GenreListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val callbacks = remember(viewModel, onGenreClick) {
        GenreListCallbacks(
            onGenreClick = { genre: Genre -> onGenreClick(genre.id, genre.name) },
            onRetry = viewModel::onRetry,
            onRefresh = viewModel::onRefresh,
        )
    }
    var renderer by remember { mutableStateOf<GenreListViewRenderer?>(null) }

    AndroidViewBinding(
        factory = ViewGenreListBinding::inflate,
        modifier = modifier.fillMaxSize(),
        update = {
            val current = renderer ?: GenreListViewRenderer(this, callbacks).also { renderer = it }
            current.render(uiState)
        },
    )
}
