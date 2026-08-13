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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.ui.R as CoreUiR
import com.movies.anymovies.core.ui.error.toUserMessage
import com.movies.anymovies.core.ui.state.AppendFooter
import com.movies.anymovies.core.ui.state.AppendFooterState
import com.movies.anymovies.core.ui.state.EmptyState
import com.movies.anymovies.core.ui.state.ErrorState
import com.movies.anymovies.core.ui.state.LoadingShimmer
import com.movies.anymovies.core.ui.state.ShimmerBlock
import com.movies.anymovies.feature.detail.domain.model.ReviewLoadException
import com.movies.anymovies.feature.detail.presentation.model.GenreUiModel
import com.movies.anymovies.feature.detail.presentation.model.MovieDetailUiModel
import com.movies.anymovies.feature.detail.presentation.model.ReviewUiModel
import com.movies.anymovies.feature.detail.presentation.model.VideoUiModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private val HeaderHeight = 280.dp
private val PosterWidth = 96.dp
private val PosterHeight = 144.dp
private val AvatarSize = 40.dp
private val ReviewShimmerRowHeight = 56.dp
private const val HEADER_COLLAPSE_RANGE_PX = 400f
private const val OVERVIEW_COLLAPSED_MAX_LINES = 5
private const val REVIEW_CONTENT_COLLAPSED_MAX_LINES = 6

public object MovieDetailTestTags {
    public const val LOADING: String = "movie_detail_loading"
    public const val ERROR: String = "movie_detail_error"
    public const val NOT_FOUND: String = "movie_detail_not_found"
    public const val SUCCESS: String = "movie_detail_success"
    public const val RETRY_BUTTON: String = "movie_detail_retry"
    public const val BACK_BUTTON: String = "movie_detail_back"
    public const val OVERVIEW_TOGGLE: String = "movie_detail_overview_toggle"
    public const val CONTENT_LIST: String = "movie_detail_content_list"
    public const val REVIEWS_HEADER: String = "movie_detail_reviews_header"
    public const val REVIEWS_LOADING: String = "movie_detail_reviews_loading"
    public const val REVIEWS_ERROR: String = "movie_detail_reviews_error"
    public const val REVIEWS_RETRY_BUTTON: String = "movie_detail_reviews_retry"
    public const val REVIEWS_EMPTY: String = "movie_detail_reviews_empty"
    public const val REVIEWS_APPEND_LOADING: String = "movie_detail_reviews_append_loading"
    public const val REVIEWS_APPEND_ERROR: String = "movie_detail_reviews_append_error"
    public const val REVIEWS_APPEND_RETRY_BUTTON: String = "movie_detail_reviews_append_retry"
    public const val REVIEWS_END_REACHED: String = "movie_detail_reviews_end_reached"

    public fun reviewItem(id: String): String = "movie_detail_review_item_$id"
    public fun reviewRatingChip(id: String): String = "movie_detail_review_rating_$id"
    public fun reviewContentToggle(id: String): String = "movie_detail_review_toggle_$id"
}

private fun Throwable.toReviewDomainError(): DomainError = (this as? ReviewLoadException)?.domainError ?: DomainError.Unknown

@Composable
public fun MovieDetailScreen(
    movieId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MovieDetailViewModel = koinViewModel(parameters = { parametersOf(movieId) }),
    reviewsViewModel: MovieReviewsViewModel = koinViewModel(parameters = { parametersOf(movieId) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lazyPagingReviews = reviewsViewModel.reviewsPagingData.collectAsLazyPagingItems()
    MovieDetailContent(
        state = uiState,
        lazyPagingReviews = lazyPagingReviews,
        onBack = onBack,
        onRetry = viewModel::onRetry,
        modifier = modifier,
    )
}

@Composable
internal fun MovieDetailContent(
    state: MovieDetailUiState,
    lazyPagingReviews: LazyPagingItems<ReviewUiModel>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
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
            lazyPagingReviews = lazyPagingReviews,
            onBack = onBack,
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
    lazyPagingReviews: LazyPagingItems<ReviewUiModel>,
    onBack: () -> Unit,
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
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().testTag(MovieDetailTestTags.CONTENT_LIST)) {
            item(key = "header", contentType = "header") {
                MovieDetailHeader(movie = movie, onPlayTrailer = onPlayTrailer)
            }
            item(key = "body", contentType = "body") {
                MovieDetailBody(movie = movie, onVideoClick = onVideoClick)
            }
            reviewsSection(lazyPagingReviews)
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

/**
 * Appends the reviews block directly into the caller's LazyColumn — a header item, then either
 * the review items + append footer, or a single loading/error/empty item in their place. Reviews
 * must never be a nested scrollable (Compose does not support LazyColumn-in-LazyColumn), so this
 * emits into the same [LazyListScope] as the rest of Movie Detail rather than owning its own list.
 */
private fun LazyListScope.reviewsSection(lazyPagingItems: LazyPagingItems<ReviewUiModel>) {
    item(key = "reviews_header", contentType = "reviews_header") {
        Text(
            text = stringResource(R.string.reviews_section_heading),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp).testTag(MovieDetailTestTags.REVIEWS_HEADER),
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    val refreshState = lazyPagingItems.loadState.refresh
    when {
        refreshState is LoadState.Loading && lazyPagingItems.itemCount == 0 -> {
            item(key = "reviews_loading", contentType = "reviews_loading") {
                LoadingShimmer(
                    modifier = Modifier.padding(horizontal = 16.dp).testTag(MovieDetailTestTags.REVIEWS_LOADING),
                ) {
                    repeat(2) {
                        ShimmerBlock(modifier = Modifier.fillMaxWidth().height(ReviewShimmerRowHeight))
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        refreshState is LoadState.Error && lazyPagingItems.itemCount == 0 -> {
            item(key = "reviews_error", contentType = "reviews_error") {
                ErrorState(
                    message = refreshState.error.toReviewDomainError().toUserMessage(),
                    actionLabel = stringResource(CoreUiR.string.core_ui_retry),
                    onAction = { lazyPagingItems.retry() },
                    actionModifier = Modifier.testTag(MovieDetailTestTags.REVIEWS_RETRY_BUTTON),
                    modifier = Modifier.fillMaxWidth().padding(16.dp).testTag(MovieDetailTestTags.REVIEWS_ERROR),
                )
            }
        }

        lazyPagingItems.itemCount == 0 && refreshState is LoadState.NotLoading -> {
            item(key = "reviews_empty", contentType = "reviews_empty") {
                EmptyState(
                    message = stringResource(R.string.reviews_empty_state),
                    modifier = Modifier.fillMaxWidth().padding(16.dp).testTag(MovieDetailTestTags.REVIEWS_EMPTY),
                )
            }
        }

        else -> {
            items(
                count = lazyPagingItems.itemCount,
                key = lazyPagingItems.itemKey { it.id },
                contentType = lazyPagingItems.itemContentType { "review" },
            ) { index ->
                val review = lazyPagingItems[index]
                if (review != null) {
                    ReviewCard(
                        review = review,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag(MovieDetailTestTags.reviewItem(review.id)),
                    )
                }
            }
            item(key = "reviews_footer", contentType = "reviews_footer") {
                val appendState = lazyPagingItems.loadState.append
                val footerState = when {
                    appendState is LoadState.Error -> AppendFooterState.ERROR
                    appendState is LoadState.Loading -> AppendFooterState.LOADING
                    appendState is LoadState.NotLoading && appendState.endOfPaginationReached -> AppendFooterState.END_REACHED
                    else -> AppendFooterState.IDLE
                }
                val footerTag = when (footerState) {
                    AppendFooterState.ERROR -> MovieDetailTestTags.REVIEWS_APPEND_ERROR
                    AppendFooterState.LOADING -> MovieDetailTestTags.REVIEWS_APPEND_LOADING
                    AppendFooterState.END_REACHED -> MovieDetailTestTags.REVIEWS_END_REACHED
                    AppendFooterState.IDLE -> ""
                }
                AppendFooter(
                    state = footerState,
                    errorMessage = (appendState as? LoadState.Error)?.error?.toReviewDomainError()?.toUserMessage().orEmpty(),
                    onRetry = { lazyPagingItems.retry() },
                    retryModifier = Modifier.testTag(MovieDetailTestTags.REVIEWS_APPEND_RETRY_BUTTON),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag(footerTag),
                )
            }
        }
    }
}

@Composable
internal fun ReviewCard(review: ReviewUiModel, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        ReviewAvatar(avatarUrl = review.avatarUrl, initial = review.avatarInitial)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = review.author,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (review.ratingLabel != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    RatingChip(
                        label = review.ratingLabel,
                        modifier = Modifier.testTag(MovieDetailTestTags.reviewRatingChip(review.id)),
                    )
                }
            }
            Text(text = review.createdDateLabel, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            ExpandableReviewContent(
                reviewId = review.id,
                text = review.content,
            )
        }
    }
}

@Composable
private fun ReviewAvatar(avatarUrl: String?, initial: String, modifier: Modifier = Modifier) {
    if (avatarUrl != null) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.size(AvatarSize).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
        )
    } else {
        Box(
            modifier = modifier
                .size(AvatarSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = initial, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun RatingChip(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ExpandableReviewContent(reviewId: String, text: String, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable(reviewId) { mutableStateOf(false) }
    var isOverflowing by remember(reviewId) { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (expanded) Int.MAX_VALUE else REVIEW_CONTENT_COLLAPSED_MAX_LINES,
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
                    .testTag(MovieDetailTestTags.reviewContentToggle(reviewId))
                    .clickable { expanded = !expanded },
            )
        }
    }
}
