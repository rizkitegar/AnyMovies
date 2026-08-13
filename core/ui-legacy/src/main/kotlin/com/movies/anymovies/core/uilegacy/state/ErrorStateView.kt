package com.movies.anymovies.core.uilegacy.state

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.movies.anymovies.core.uilegacy.R
import com.movies.anymovies.core.uilegacy.databinding.ViewStateErrorBinding

/**
 * Blocking error state: message plus an optional retry action. The message text must always
 * come from `DomainError.toUserMessage()` at the call site — never a raw exception, stack
 * trace, or HTTP code.
 */
public class ErrorStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewStateErrorBinding.inflate(LayoutInflater.from(context), this, true)

    public fun render(
        message: String,
        retryLabel: String = context.getString(R.string.core_ui_legacy_retry),
        onRetry: (() -> Unit)? = null,
    ) {
        binding.stateErrorMessage.text = message
        binding.stateErrorRetryButton.isVisible = onRetry != null
        binding.stateErrorRetryButton.text = retryLabel
        binding.stateErrorRetryButton.setOnClickListener { onRetry?.invoke() }
    }
}
