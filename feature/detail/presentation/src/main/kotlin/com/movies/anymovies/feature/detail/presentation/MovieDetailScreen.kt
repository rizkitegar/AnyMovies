package com.movies.anymovies.feature.detail.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.ui.R as CoreUiR
import com.movies.anymovies.core.ui.error.toUserMessage
import com.movies.anymovies.core.ui.state.ErrorState
import com.movies.anymovies.core.ui.state.LoadingShimmer
import com.movies.anymovies.core.ui.state.ShimmerBlock
import com.movies.anymovies.feature.detail.presentation.model.GenreUiModel
import com.movies.anymovies.feature.detail.presentation.model.MovieDetailUiModel
import com.movies.anymovies.feature.detail.presentation.model.VideoUiModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private val HeaderHeight = 280.dp
private val PosterWidth = 96.dp
private val PosterHeight = 144.dp
private const val HEADER_COLLAPSE_RANGE_PX = 400f
private const val OVERVIEW_COLLAPSED_MAX_LINES = 5

public object MovieDetailTestTags {
    public const val LOADING: String = "movie_detail_loading"
    public const val ERROR: String = "movie_detail_error"
    public const val NOT_FOUND: String = "movie_detail_not_found"
    public const val SUCCESS: String = "movie_detail_success"
    public const val RETRY_BUTTON: String = "movie_detail_retry"
    public const val BACK_BUTTON: String = "movie_detail_back"
    public const val OVERVIEW_TOGGLE: String = "movie_detail_overview_toggle"
}

@Composable
public fun MovieDetailScreen(
    movieId: Int,
    onBack: () -> Unit,
    onSeeAllReviews: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MovieDetailViewModel = koinViewModel(parameters = { parametersOf(movieId) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MovieDetailContent(
        state = uiState,
        onBack = onBack,
        onRetry = viewModel::onRetry,
        onSeeAllReviews = onSeeAllReviews,
        modifier = modifier,
    )
}

@Composable
internal fun MovieDetailContent(
    state: MovieDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSeeAllReviews: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is MovieDetailUiState.Loading -> MovieDetailLoading(onBack = onBack, modifier = modifier)

        is MovieDetailUiState.Error -> if (state.error == DomainError.NotFound) {
            MovieDetailNotFound(onBack = onBack, modifier = modifier)
        } else {
            MovieDetailError(
                error = state.error,
                isRetryable = state.isRetryable,
                onRetry = onRetry,
                onBack = onBack,
                modifier = modifier,
            )
        }

        is MovieDetailUiState.Success -> MovieDetailSuccess(
            movie = state.movie,
            onBack = onBack,
            onSeeAllReviews = onSeeAllReviews,
            modifier = modifier,
        )
    }
}

@Composable
private fun MovieDetailLoading(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().testTag(MovieDetailTestTags.LOADING)) {
        LoadingShimmer {
            ShimmerBlock(modifier = Modifier.fillMaxWidth().height(HeaderHeight))
            Spacer(modifier = Modifier.height(16.dp))
            repeat(4) {
                ShimmerBlock(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .fillMaxWidth()
                        .height(16.dp),
                )
            }
        }
        BackButton(onBack = onBack, modifier = Modifier.align(Alignment.TopStart))
    }
}

@Composable
private fun MovieDetailNotFound(onBack: () -> Unit, modifier: Modifier = Modifier) {
    ErrorState(
        message = DomainError.NotFound.toUserMessage(),
        title = stringResource(R.string.detail_error_title_not_found),
        actionLabel = stringResource(CoreUiR.string.core_ui_back),
        onAction = onBack,
        modifier = modifier.fillMaxSize().testTag(MovieDetailTestTags.NOT_FOUND),
    )
}

@Composable
private fun MovieDetailError(
    error: DomainError,
    isRetryable: Boolean,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ErrorState(
        message = error.toUserMessage(),
        title = stringResource(R.string.detail_error_title_generic),
        actionLabel = if (isRetryable) {
            stringResource(CoreUiR.string.core_ui_retry)
        } else {
            stringResource(CoreUiR.string.core_ui_back)
        },
        onAction = if (isRetryable) onRetry else onBack,
        actionModifier = if (isRetryable) Modifier.testTag(MovieDetailTestTags.RETRY_BUTTON) else Modifier,
        modifier = modifier.fillMaxSize().testTag(MovieDetailTestTags.ERROR),
    )
}

@Composable
private fun MovieDetailSuccess(
    movie: MovieDetailUiModel,
    onBack: () -> Unit,
    onSeeAllReviews: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val couldNotOpenYoutubeMessage = stringResource(R.string.detail_youtube_open_failed)

    val onOpenYoutube = remember(context, snackbarHostState, coroutineScope, couldNotOpenYoutubeMessage) {
        { key: String ->
            if (!launchYoutube(context, key)) {
                coroutineScope.launch { snackbarHostState.showSnackbar(couldNotOpenYoutubeMessage) }
            }
        }
    }

    // Derived so recomposition only happens when the collapse fraction
    // actually changes, not on every raw scroll pixel.
    val collapseFraction by remember {
        derivedStateOf {
            val firstVisible = listState.firstVisibleItemIndex
            val offset = if (firstVisible == 0) listState.firstVisibleItemScrollOffset else Int.MAX_VALUE
            (offset / HEADER_COLLAPSE_RANGE_PX).coerceIn(0f, 1f)
        }
    }
    val titleAlpha by animateFloatAsState(targetValue = collapseFraction, label = "titleAlpha")

    val onPlayTrailer = remember(movie.selectedTrailerKey, onOpenYoutube) {
        movie.selectedTrailerKey?.let { key -> { onOpenYoutube(key) } }
    }
    val onVideoClick = remember(onOpenYoutube) {
        { video: VideoUiModel -> onOpenYoutube(video.key) }
    }

    Box(modifier = modifier.fillMaxSize().testTag(MovieDetailTestTags.SUCCESS)) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            item(key = "header", contentType = "header") {
                MovieDetailHeader(movie = movie, onPlayTrailer = onPlayTrailer)
            }
            item(key = "body", contentType = "body") {
                MovieDetailBody(
                    movie = movie,
                    onSeeAllReviews = onSeeAllReviews,
                    onVideoClick = onVideoClick,
                )
            }
        }
        CollapsingTopBar(
            title = movie.title,
            titleAlpha = titleAlpha,
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun CollapsingTopBar(
    title: String,
    titleAlpha: Float,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = titleAlpha))
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BackButton(onBack = onBack)
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(start = 8.dp, end = 16.dp)
                .alpha(titleAlpha),
        )
    }
}

@Composable
private fun BackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onBack, modifier = modifier.testTag(MovieDetailTestTags.BACK_BUTTON)) {
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(CoreUiR.string.core_ui_back))
    }
}

@Composable
private fun MovieDetailHeader(
    movie: MovieDetailUiModel,
    onPlayTrailer: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth().height(HeaderHeight)) {
        if (movie.headerImageUrl != null) {
            AsyncImage(
                model = movie.headerImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Icon(
                    imageVector = Icons.Filled.BrokenImage,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center).size(48.dp),
                )
            }
        }
        if (onPlayTrailer != null) {
            TrailerPlayButton(onClick = onPlayTrailer, modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun MovieDetailBody(
    movie: MovieDetailUiModel,
    onSeeAllReviews: () -> Unit,
    onVideoClick: (VideoUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Row {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = PosterWidth, height = PosterHeight)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = movie.title, style = MaterialTheme.typography.headlineSmall)
                if (movie.tagline != null) {
                    Text(text = movie.tagline, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = movie.ratingLabel, style = MaterialTheme.typography.bodyMedium)
                if (movie.releaseDateLabel != null) {
                    Text(text = movie.releaseDateLabel, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        MovieDetailChips(movie = movie)

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(R.string.detail_section_overview), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        ExpandableOverview(text = movie.overviewLabel)

        Spacer(modifier = Modifier.height(24.dp))
        VideoSection(videos = movie.videos, onVideoClick = onVideoClick)

        Spacer(modifier = Modifier.height(24.dp))
        ReviewsSection(movieId = movie.id, onSeeAllReviews = onSeeAllReviews)
    }
}

@Composable
private fun MovieDetailChips(movie: MovieDetailUiModel, modifier: Modifier = Modifier) {
    val chips = remember(movie.runtimeLabel, movie.genres) {
        buildList {
            if (movie.runtimeLabel != null) add(movie.runtimeLabel)
            addAll(movie.genres.map(GenreUiModel::name))
        }
    }
    if (chips.isEmpty()) return
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        chips.forEach { label ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(text = label, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun ExpandableOverview(text: String, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var isOverflowing by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (expanded) Int.MAX_VALUE else OVERVIEW_COLLAPSED_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result ->
                if (!expanded) isOverflowing = result.didOverflowHeight || result.hasVisualOverflow
            },
        )
        if (isOverflowing || expanded) {
            Text(
                text = if (expanded) stringResource(R.string.detail_read_less) else stringResource(R.string.detail_read_more),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .testTag(MovieDetailTestTags.OVERVIEW_TOGGLE)
                    .clickable { expanded = !expanded },
            )
        }
    }
}
