package com.movies.anymovies.feature.movies.presentation.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.movies.anymovies.feature.movies.presentation.R
import com.movies.anymovies.feature.movies.presentation.databinding.ItemMovieBinding
import com.movies.anymovies.feature.movies.presentation.model.MovieUiModel

internal class MovieListAdapter(
    private val onMovieClick: (MovieUiModel) -> Unit,
) : ListAdapter<MovieUiModel, MovieListAdapter.MovieViewHolder>(DiffCallback) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = getItem(position).id.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding, onMovieClick)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    internal class MovieViewHolder(
        private val binding: ItemMovieBinding,
        private val onMovieClick: (MovieUiModel) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: MovieUiModel) {
            binding.moviePoster.load(movie.posterUrl) {
                placeholder(R.drawable.bg_movie_poster_placeholder)
                error(R.drawable.bg_movie_poster_error)
                crossfade(false)
            }
            binding.movieTitle.text = movie.title
            binding.movieYear.isVisible = movie.releaseYearLabel != null
            binding.movieYear.text = movie.releaseYearLabel
            binding.movieRating.isVisible = movie.voteAverageLabel != null
            binding.movieRating.text = movie.voteAverageLabel
            binding.root.setOnClickListener { onMovieClick(movie) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<MovieUiModel>() {
        override fun areItemsTheSame(oldItem: MovieUiModel, newItem: MovieUiModel) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MovieUiModel, newItem: MovieUiModel) = oldItem == newItem
    }
}
