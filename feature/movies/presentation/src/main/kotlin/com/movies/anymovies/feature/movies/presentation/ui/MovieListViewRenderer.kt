package com.movies.anymovies.feature.movies.presentation.ui

import androidx.core.view.isVisible
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.movies.anymovies.core.common.error.toUserMessage
import com.movies.anymovies.core.uilegacy.state.AppendFooterAdapter
import com.movies.anymovies.core.uilegacy.state.AppendFooterState
import com.movies.anymovies.feature.movies.presentation.MovieListUiState
import com.movies.anymovies.feature.movies.presentation.R
import com.movies.anymovies.feature.movies.presentation.databinding.ViewMovieListBinding
import com.movies.anymovies.feature.movies.presentation.model.MovieUiModel

internal class MovieListCallbacks(
    val onMovieClick: (MovieUiModel) -> Unit,
    val onRetry: () -> Unit,
    val onRetryAppend: () -> Unit,
    val onLoadNextPage: () -> Unit,
    val onBack: () -> Unit,
)

private const val GRID_SPAN_COUNT = 2
private const val LOAD_MORE_THRESHOLD = 4

internal class MovieListViewRenderer(
    private val binding: ViewMovieListBinding,
    private val callbacks: MovieListCallbacks,
) {
    private val movieAdapter = MovieListAdapter(onMovieClick = callbacks.onMovieClick)
    private val footerAdapter = AppendFooterAdapter(onRetry = callbacks.onRetryAppend)
    private val layoutManager = GridLayoutManager(binding.root.context, GRID_SPAN_COUNT)

    init {
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position >= movieAdapter.itemCount) GRID_SPAN_COUNT else 1
            }
        }
        binding.movieRecyclerView.apply {
            layoutManager = this@MovieListViewRenderer.layoutManager
            adapter = ConcatAdapter(movieAdapter, footerAdapter)
            setHasFixedSize(true)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy <= 0) return
                    val lastVisible = this@MovieListViewRenderer.layoutManager.findLastVisibleItemPosition()
                    if (lastVisible < 0) return
                    if (lastVisible >= movieAdapter.itemCount - 1 - LOAD_MORE_THRESHOLD) {
                        callbacks.onLoadNextPage()
                    }
                }
            })
        }
    }

    fun render(genreName: String, state: MovieListUiState) {
        binding.movieToolbarTitle.text = genreName

        binding.movieShimmerContainer.isVisible = state is MovieListUiState.Loading
        binding.movieRecyclerView.isVisible = state is MovieListUiState.Success
        binding.movieErrorStateView.isVisible = state is MovieListUiState.Error
        binding.movieEmptyStateView.isVisible = state is MovieListUiState.Empty

        when (state) {
            is MovieListUiState.Success -> {
                movieAdapter.submitList(state.movies)
                val footerState = when {
                    state.appendError != null -> AppendFooterState.ERROR
                    state.isAppending -> AppendFooterState.LOADING
                    state.endReached -> AppendFooterState.END_REACHED
                    else -> AppendFooterState.IDLE
                }
                footerAdapter.submit(footerState, state.appendError?.toUserMessage().orEmpty())
            }
            is MovieListUiState.Error -> {
                binding.movieErrorStateView.render(
                    message = state.error.toUserMessage(),
                    onRetry = callbacks.onRetry,
                )
            }
            MovieListUiState.Empty -> {
                binding.movieEmptyStateView.render(
                    message = context.getString(R.string.movie_empty_title),
                    actionLabel = context.getString(R.string.movie_empty_back),
                    onAction = callbacks.onBack,
                )
            }
            MovieListUiState.Loading -> Unit
        }
    }

    private val context get() = binding.root.context
}
