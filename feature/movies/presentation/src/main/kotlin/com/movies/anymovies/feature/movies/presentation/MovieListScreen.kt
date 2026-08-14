package com.movies.anymovies.feature.movies.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movies.anymovies.feature.movies.presentation.databinding.ViewMovieListBinding
import com.movies.anymovies.feature.movies.presentation.model.MovieUiModel
import com.movies.anymovies.feature.movies.presentation.ui.MovieListCallbacks
import com.movies.anymovies.feature.movies.presentation.ui.MovieListViewRenderer
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MovieListScreen(
    genreId: Int,
    genreName: String,
    onMovieClick: (movieId: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MovieListViewModel = koinViewModel(parameters = { parametersOf(genreId) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val callbacks = remember(viewModel, onMovieClick, onBack) {
        MovieListCallbacks(
            onMovieClick = { movie: MovieUiModel -> onMovieClick(movie.id) },
            onRetry = viewModel::onRetry,
            onRetryAppend = viewModel::onRetryAppend,
            onLoadNextPage = viewModel::loadNextPage,
            onBack = onBack,
        )
    }
    var renderer by remember { mutableStateOf<MovieListViewRenderer?>(null) }

    AndroidViewBinding(
        factory = ViewMovieListBinding::inflate,
        modifier = modifier.fillMaxSize(),
        update = {
            val current = renderer ?: MovieListViewRenderer(this, callbacks).also { renderer = it }
            current.render(genreName = genreName, state = uiState)
        },
    )
}
