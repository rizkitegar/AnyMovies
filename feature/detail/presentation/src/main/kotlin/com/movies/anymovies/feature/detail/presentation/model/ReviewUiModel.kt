package com.movies.anymovies.feature.detail.presentation.model

import androidx.compose.runtime.Immutable
import com.movies.anymovies.feature.detail.domain.model.Review
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val REVIEW_CONTENT_UI_MAX_CHARS = 2_000

private val reviewDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM uuuu", Locale.US).withZone(ZoneOffset.UTC)

@Immutable
public data class ReviewUiModel(
    val id: String,
    val author: String,
    val avatarUrl: String?,
    val avatarInitial: String,
    val ratingLabel: String?,
    val createdDateLabel: String,
    val content: String,
)

public fun Review.toUiModel(): ReviewUiModel = ReviewUiModel(
    id = id,
    author = author,
    avatarUrl = avatarUrl,
    avatarInitial = author.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?",
    ratingLabel = rating?.let { String.format(Locale.US, "%.1f", it) },
    createdDateLabel = reviewDateFormatter.format(createdAt),
    content = content.truncateForDisplay(REVIEW_CONTENT_UI_MAX_CHARS),
)

private fun String.truncateForDisplay(maxLength: Int): String {
    if (length <= maxLength) return this
    var end = maxLength
    if (end > 0 && Character.isHighSurrogate(this[end - 1])) end--
    return substring(0, end) + "…"
}
