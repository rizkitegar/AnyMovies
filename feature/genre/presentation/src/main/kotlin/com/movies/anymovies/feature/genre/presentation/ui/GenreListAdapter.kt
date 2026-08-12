package com.movies.anymovies.feature.genre.presentation.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.movies.anymovies.feature.genre.domain.model.Genre
import com.movies.anymovies.feature.genre.presentation.databinding.ItemGenreBinding

internal class GenreListAdapter(
    private val onGenreClick: (Genre) -> Unit,
) : ListAdapter<Genre, GenreListAdapter.GenreViewHolder>(DiffCallback) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = getItem(position).id.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val binding = ItemGenreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GenreViewHolder(binding, onGenreClick)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    internal class GenreViewHolder(
        private val binding: ItemGenreBinding,
        private val onGenreClick: (Genre) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(genre: Genre) {
            binding.genreItemName.text = genre.name
            binding.genreItemName.setOnClickListener { onGenreClick(genre) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Genre>() {
        override fun areItemsTheSame(oldItem: Genre, newItem: Genre): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Genre, newItem: Genre): Boolean = oldItem == newItem
    }
}
