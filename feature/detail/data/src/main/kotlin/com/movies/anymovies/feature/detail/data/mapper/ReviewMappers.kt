package com.movies.anymovies.feature.detail.data.mapper

import com.movies.anymovies.feature.detail.data.remote.dto.ReviewDto
import com.movies.anymovies.feature.detail.domain.model.Review
import java.time.Instant
import java.time.format.DateTimeParseException

private const val AVATAR_BASE_URL = "https://image.tmdb.org/t/p/w185"
private const val MAX_CONTENT_LENGTH = 10_000

private val HTML_TAG_REGEX = Regex("<[^>]*>")
private val ENTITY_REGEX = Regex("&(#x[0-9a-fA-F]+|#\\d+|[a-zA-Z]+);")
private val NAMED_ENTITIES = mapOf(
    "amp" to "&",
    "lt" to "<",
    "gt" to ">",
    "quot" to "\"",
    "apos" to "'",
    "nbsp" to " ",
)

internal fun ReviewDto.toDomain(): Review = Review(
    id = id,
    author = authorDetails.name.takeIf { it.isNotBlank() } ?: author,
    username = authorDetails.username.takeIf { it.isNotBlank() } ?: author,
    avatarUrl = authorDetails.avatarPath.toAvatarUrl(),
    rating = authorDetails.rating,
    content = sanitizeReviewContent(content),
    createdAt = createdAt.toInstantOrDefault(),
)

internal fun sanitizeReviewContent(raw: String): String {
    val withoutTags = HTML_TAG_REGEX.replace(raw, "")
    val decoded = decodeHtmlEntities(withoutTags)
    return decoded.trim().truncateSafely(MAX_CONTENT_LENGTH)
}

private fun decodeHtmlEntities(text: String): String = ENTITY_REGEX.replace(text) { match ->
    val body = match.groupValues[1]
    when {
        body.startsWith("#x", ignoreCase = true) ->
            body.substring(2).toIntOrNull(16)?.let { String(Character.toChars(it)) } ?: match.value
        body.startsWith("#") ->
            body.substring(1).toIntOrNull()?.let { String(Character.toChars(it)) } ?: match.value
        else -> NAMED_ENTITIES[body.lowercase()] ?: match.value
    }
}

private fun String.truncateSafely(maxLength: Int): String {
    if (length <= maxLength) return this
    var end = maxLength
    if (end > 0 && Character.isHighSurrogate(this[end - 1])) end--
    return substring(0, end)
}

private fun String?.toAvatarUrl(): String? {
    if (this.isNullOrBlank()) return null
    return if (startsWith("/http")) removePrefix("/") else "$AVATAR_BASE_URL$this"
}

private fun String?.toInstantOrDefault(): Instant {
    if (this.isNullOrBlank()) return Instant.EPOCH
    return try {
        Instant.parse(this)
    } catch (e: DateTimeParseException) {
        Instant.EPOCH
    }
}
