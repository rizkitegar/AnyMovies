package com.movies.anymovies.core.ui.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.movies.anymovies.core.ui.R

/** Endless-scroll footer state, shared by any `LazyColumn`/`RecyclerView` that paginates. */
public enum class AppendFooterState { IDLE, LOADING, ERROR, END_REACHED }

/**
 * Footer row appended to a paginated list: an in-flight spinner while the next page loads,
 * a retry row if the append failed, or an end-of-list message once the last page is loaded.
 * Renders nothing for [AppendFooterState.IDLE].
 */
@Composable
public fun AppendFooter(
    state: AppendFooterState,
    modifier: Modifier = Modifier,
    errorMessage: String = "",
    endReachedMessage: String = stringResource(R.string.core_ui_end_reached),
    retryLabel: String = stringResource(R.string.core_ui_retry),
    retryModifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
) {
    when (state) {
        AppendFooterState.IDLE -> Unit

        AppendFooterState.LOADING -> Row(
            modifier = modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }

        AppendFooterState.ERROR -> Row(
            modifier = modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = errorMessage, style = MaterialTheme.typography.bodySmall)
            Button(onClick = onRetry, modifier = retryModifier.heightIn(min = 48.dp)) {
                Text(text = retryLabel)
            }
        }

        AppendFooterState.END_REACHED -> Text(
            text = endReachedMessage,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = modifier.fillMaxWidth().padding(16.dp),
        )
    }
}
