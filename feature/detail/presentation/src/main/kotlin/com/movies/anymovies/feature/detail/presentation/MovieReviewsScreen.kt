package com.movies.anymovies.feature.detail.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.common.error.toUserMessage
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private const val LOAD_MORE_THRESHOLD = 4

public object MovieReviewsTestTags {
    public const val LOADING: String = "movie_reviews_loading"
    public const val EMPTY: String = "movie_reviews_empty"
    public const val ERROR: String = "movie_reviews_error"
    public const val RETRY_BUTTON: String = "movie_reviews_retry"
    public const val BACK_BUTTON: String = "movie_reviews_back"
    public const val LIST: String = "movie_reviews_list"
    public const val APPEND_LOADING: String = "movie_reviews_append_loading"
    public const val APPEND_ERROR: String = "movie_reviews_append_error"
    public const val APPEND_RETRY_BUTTON: String = "movie_reviews_append_retry"
    public const val END_REACHED: String = "movie_reviews_end_reached"
}

@Composable
public fun MovieReviewsScreen(
    movieId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MovieReviewsViewModel = koinViewModel(parameters = { parametersOf(movieId) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MovieReviewsContent(
        state = uiState,
        onBack = onBack,
        onRetry = viewModel::onRetry,
        onRetryAppend = viewModel::onRetryAppend,
        onLoadNextPage = viewModel::loadNextPage,
        modifier = modifier,
    )
}

@Composable
internal fun MovieReviewsContent(
    state: MovieReviewsUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRetryAppend: () -> Unit,
    onLoadNextPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        MovieReviewsTopBar(onBack = onBack)
        when (state) {
            is MovieReviewsUiState.Loading -> Text(
                text = "Loading reviews…",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp).testTag(MovieReviewsTestTags.LOADING),
            )

            is MovieReviewsUiState.Empty -> Text(
                text = "No reviews yet",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp).testTag(MovieReviewsTestTags.EMPTY),
            )

            is MovieReviewsUiState.Error -> Column(
                modifier = Modifier.padding(16.dp).testTag(MovieReviewsTestTags.ERROR),
            ) {
                Text(text = state.error.toUserMessage(), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onRetry, modifier = Modifier.testTag(MovieReviewsTestTags.RETRY_BUTTON)) {
                    Text(text = "Retry")
                }
            }

            is MovieReviewsUiState.Success -> MovieReviewsList(
                state = state,
                onRetryAppend = onRetryAppend,
                onLoadNextPage = onLoadNextPage,
            )
        }
    }
}

@Composable
private fun MovieReviewsTopBar(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp)) {
        IconButton(onClick = onBack, modifier = Modifier.testTag(MovieReviewsTestTags.BACK_BUTTON)) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(
            text = "Reviews",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 8.dp).align(Alignment.CenterVertically),
        )
    }
}

@Composable
private fun MovieReviewsList(
    state: MovieReviewsUiState.Success,
    onRetryAppend: () -> Unit,
    onLoadNextPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            totalItems > 0 && lastVisibleIndex >= totalItems - 1 - LOAD_MORE_THRESHOLD
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadNextPage()
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().testTag(MovieReviewsTestTags.LIST),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(items = state.reviews, key = { it.id }, contentType = { "review" }) { review ->
            ReviewCard(review = review)
        }
        item(key = "footer", contentType = "footer") {
            MovieReviewsFooter(
                isAppending = state.isAppending,
                appendError = state.appendError,
                endReached = state.endReached,
                onRetryAppend = onRetryAppend,
            )
        }
    }
}

@Composable
private fun MovieReviewsFooter(
    isAppending: Boolean,
    appendError: DomainError?,
    endReached: Boolean,
    onRetryAppend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        appendError != null -> Row(
            modifier = modifier.fillMaxWidth().testTag(MovieReviewsTestTags.APPEND_ERROR),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = appendError.toUserMessage(), style = MaterialTheme.typography.bodySmall)
            Button(onClick = onRetryAppend, modifier = Modifier.testTag(MovieReviewsTestTags.APPEND_RETRY_BUTTON)) {
                Text(text = "Retry")
            }
        }

        isAppending -> Row(
            modifier = modifier.fillMaxWidth().testTag(MovieReviewsTestTags.APPEND_LOADING),
            horizontalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }

        endReached -> Text(
            text = "You've reached the end",
            style = MaterialTheme.typography.bodySmall,
            modifier = modifier.fillMaxWidth().testTag(MovieReviewsTestTags.END_REACHED),
        )
    }
}
