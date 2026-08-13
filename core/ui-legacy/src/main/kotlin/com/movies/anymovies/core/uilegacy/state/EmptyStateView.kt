package com.movies.anymovies.core.uilegacy.state

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.movies.anymovies.core.uilegacy.databinding.ViewStateEmptyBinding

/** Empty-result state (not an error): message plus an optional contextual action. */
public class EmptyStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewStateEmptyBinding.inflate(LayoutInflater.from(context), this, true)

    public fun render(
        message: String,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
    ) {
        binding.stateEmptyMessage.text = message
        val showAction = actionLabel != null && onAction != null
        binding.stateEmptyActionButton.isVisible = showAction
        if (showAction) {
            binding.stateEmptyActionButton.text = actionLabel
            binding.stateEmptyActionButton.setOnClickListener { onAction?.invoke() }
        } else {
            binding.stateEmptyActionButton.setOnClickListener(null)
        }
    }
}
