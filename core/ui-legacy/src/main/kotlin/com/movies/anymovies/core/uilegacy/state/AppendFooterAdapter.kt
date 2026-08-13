package com.movies.anymovies.core.uilegacy.state

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Generic single-item `RecyclerView` footer adapter for endless-scroll append state.
 * Attach alongside the content adapter via `ConcatAdapter`.
 */
public class AppendFooterAdapter(
    private val onRetry: () -> Unit,
) : RecyclerView.Adapter<AppendFooterAdapter.FooterViewHolder>() {

    private var state: AppendFooterState = AppendFooterState.IDLE
    private var errorMessage: String = ""

    public fun submit(state: AppendFooterState, errorMessage: String = "") {
        val hadItem = this.state != AppendFooterState.IDLE
        val changed = this.state != state || this.errorMessage != errorMessage
        this.state = state
        this.errorMessage = errorMessage
        val hasItem = state != AppendFooterState.IDLE
        when {
            !hadItem && hasItem -> notifyItemInserted(0)
            hadItem && !hasItem -> notifyItemRemoved(0)
            hadItem && hasItem && changed -> notifyItemChanged(0)
        }
    }

    override fun getItemCount(): Int = if (state == AppendFooterState.IDLE) 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FooterViewHolder {
        val footerView = AppendFooterView(parent.context)
        return FooterViewHolder(footerView)
    }

    override fun onBindViewHolder(holder: FooterViewHolder, position: Int) {
        holder.footerView.render(state = state, errorMessage = errorMessage, onRetry = onRetry)
    }

    public class FooterViewHolder(internal val footerView: AppendFooterView) : RecyclerView.ViewHolder(footerView)
}
