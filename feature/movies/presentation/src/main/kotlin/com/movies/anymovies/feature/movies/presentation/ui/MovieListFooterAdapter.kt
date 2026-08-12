package com.movies.anymovies.feature.movies.presentation.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.movies.anymovies.feature.movies.presentation.databinding.ItemMovieFooterEndBinding
import com.movies.anymovies.feature.movies.presentation.databinding.ItemMovieFooterErrorBinding
import com.movies.anymovies.feature.movies.presentation.databinding.ItemMovieFooterLoadingBinding

internal enum class MovieListFooterState { NONE, LOADING, ERROR, END }

internal class MovieListFooterAdapter(
    private val onRetryAppend: () -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var state: MovieListFooterState = MovieListFooterState.NONE
    private var errorMessage: String = ""

    fun submit(state: MovieListFooterState, errorMessage: String = "") {
        val hadItem = this.state != MovieListFooterState.NONE
        val changed = this.state != state || this.errorMessage != errorMessage
        this.state = state
        this.errorMessage = errorMessage
        val hasItem = state != MovieListFooterState.NONE
        when {
            !hadItem && hasItem -> notifyItemInserted(0)
            hadItem && !hasItem -> notifyItemRemoved(0)
            hadItem && hasItem && changed -> notifyItemChanged(0)
        }
    }

    override fun getItemCount(): Int = if (state == MovieListFooterState.NONE) 0 else 1

    override fun getItemViewType(position: Int): Int = state.ordinal

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (MovieListFooterState.entries[viewType]) {
            MovieListFooterState.LOADING -> LoadingViewHolder(
                ItemMovieFooterLoadingBinding.inflate(inflater, parent, false),
            )
            MovieListFooterState.ERROR -> ErrorViewHolder(
                ItemMovieFooterErrorBinding.inflate(inflater, parent, false),
                onRetryAppend,
            )
            MovieListFooterState.END -> EndViewHolder(
                ItemMovieFooterEndBinding.inflate(inflater, parent, false),
            )
            MovieListFooterState.NONE -> error("NONE has no footer view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ErrorViewHolder) holder.bind(errorMessage)
    }

    internal class LoadingViewHolder(binding: ItemMovieFooterLoadingBinding) : RecyclerView.ViewHolder(binding.root)

    internal class EndViewHolder(binding: ItemMovieFooterEndBinding) : RecyclerView.ViewHolder(binding.root)

    internal class ErrorViewHolder(
        private val binding: ItemMovieFooterErrorBinding,
        onRetryAppend: () -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.movieFooterErrorRetryButton.setOnClickListener { onRetryAppend() }
        }

        fun bind(message: String) {
            binding.movieFooterErrorMessage.text = message
        }
    }
}
