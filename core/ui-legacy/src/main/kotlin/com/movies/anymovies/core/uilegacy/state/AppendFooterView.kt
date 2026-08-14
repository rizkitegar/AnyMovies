package com.movies.anymovies.core.uilegacy.state

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.movies.anymovies.core.uilegacy.databinding.ViewAppendFooterBinding

/**
 * Footer appended to a paginated list: an in-flight spinner while the next page loads, a
 * retry row if the append failed, or an end-of-list message once the last page is loaded.
 */
class AppendFooterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewAppendFooterBinding.inflate(LayoutInflater.from(context), this, true)

    fun render(
        state: AppendFooterState,
        errorMessage: String = "",
        onRetry: (() -> Unit)? = null,
    ) {
        binding.appendFooterLoadingContainer.isVisible = state == AppendFooterState.LOADING
        binding.appendFooterErrorContainer.isVisible = state == AppendFooterState.ERROR
        binding.appendFooterEndMessage.isVisible = state == AppendFooterState.END_REACHED

        if (state == AppendFooterState.ERROR) {
            binding.appendFooterErrorMessage.text = errorMessage
            binding.appendFooterRetryButton.setOnClickListener { onRetry?.invoke() }
        }
    }
}
