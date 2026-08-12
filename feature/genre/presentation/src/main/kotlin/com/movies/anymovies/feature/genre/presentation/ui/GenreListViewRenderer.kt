package com.movies.anymovies.feature.genre.presentation.ui

import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.movies.anymovies.core.common.error.toUserMessage
import com.movies.anymovies.feature.genre.domain.model.Genre
import com.movies.anymovies.feature.genre.presentation.GenreListUiState
import com.movies.anymovies.feature.genre.presentation.databinding.ViewGenreListBinding

internal class GenreListCallbacks(
    val onGenreClick: (Genre) -> Unit,
    val onRetry: () -> Unit,
    val onRefresh: () -> Unit,
)

internal class GenreListViewRenderer(
    private val binding: ViewGenreListBinding,
    private val callbacks: GenreListCallbacks,
) {
    private val adapter = GenreListAdapter(onGenreClick = callbacks.onGenreClick)

    init {
        binding.genreRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@GenreListViewRenderer.adapter
            setHasFixedSize(true)
        }
        binding.genreSwipeRefresh.setOnRefreshListener { callbacks.onRefresh() }
        binding.genreErrorRetryButton.setOnClickListener { callbacks.onRetry() }
    }

    fun render(state: GenreListUiState) {
        binding.genreShimmerContainer.isVisible = state is GenreListUiState.Loading
        binding.genreSwipeRefresh.isVisible = state is GenreListUiState.Success
        binding.genreErrorContainer.isVisible = state is GenreListUiState.Error
        binding.genreEmptyContainer.isVisible = state is GenreListUiState.Empty

        when (state) {
            is GenreListUiState.Success -> {
                adapter.submitList(state.genres)
                if (binding.genreSwipeRefresh.isRefreshing != state.isRefreshing) {
                    binding.genreSwipeRefresh.isRefreshing = state.isRefreshing
                }
            }
            is GenreListUiState.Error -> {
                binding.genreErrorMessage.text = state.error.toUserMessage()
            }
            GenreListUiState.Empty, GenreListUiState.Loading -> Unit
        }
    }
}
