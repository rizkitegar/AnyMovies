package com.movies.anymovies.feature.detail.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.movies.anymovies.core.common.error.toUserMessage
import com.movies.anymovies.feature.detail.presentation.model.ReviewUiModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private const val REVIEW_CONTENT_COLLAPSED_MAX_LINES = 6
private val AvatarSize = 40.dp

public object ReviewsSectionTestTags {
    public const val LOADING: String = "reviews_section_loading"
    public const val EMPTY: String = "reviews_section_empty"
    public const val ERROR: String = "reviews_section_error"
    public const val RETRY_BUTTON: String = "reviews_section_retry"
    public const val SEE_ALL: String = "reviews_section_see_all"

    public fun reviewItem(id: String): String = "reviews_section_item_$id"
    public fun ratingChip(id: String): String = "reviews_section_rating_$id"
    public fun contentToggle(id: String): String = "reviews_section_toggle_$id"
}

@Composable
internal fun ReviewsSection(
    movieId: Int,
    onSeeAllReviews: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReviewsSectionViewModel = koinViewModel(parameters = { parametersOf(movieId) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ReviewsSectionContent(
        state = uiState,
        onSeeAllReviews = onSeeAllReviews,
        onRetry = viewModel::onRetry,
        modifier = modifier,
    )
}

@Composable
internal fun ReviewsSectionContent(
    state: ReviewsSectionUiState,
    onSeeAllReviews: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Reviews", style = MaterialTheme.typography.titleMedium)
            if (state is ReviewsSectionUiState.Success) {
                Text(
                    text = "See all (${state.totalResults})",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .testTag(ReviewsSectionTestTags.SEE_ALL)
                        .clickable(onClick = onSeeAllReviews),
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        when (state) {
            is ReviewsSectionUiState.Loading -> {
                Text(
                    text = "Loading reviews…",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag(ReviewsSectionTestTags.LOADING),
                )
            }

            is ReviewsSectionUiState.Empty -> {
                Text(
                    text = "No reviews yet",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag(ReviewsSectionTestTags.EMPTY),
                )
            }

            is ReviewsSectionUiState.Error -> {
                Column(modifier = Modifier.testTag(ReviewsSectionTestTags.ERROR)) {
                    Text(text = state.error.toUserMessage(), style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.testTag(ReviewsSectionTestTags.RETRY_BUTTON),
                    ) {
                        Text(text = "Retry")
                    }
                }
            }

            is ReviewsSectionUiState.Success -> {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    state.reviews.forEach { review ->
                        ReviewCard(review = review, modifier = Modifier.testTag(ReviewsSectionTestTags.reviewItem(review.id)))
                    }
                }
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
                        modifier = Modifier.testTag(ReviewsSectionTestTags.ratingChip(review.id)),
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
                text = if (expanded) "Read less" else "Read more",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .testTag(ReviewsSectionTestTags.contentToggle(reviewId))
                    .clickable { expanded = !expanded },
            )
        }
    }
}
