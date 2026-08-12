package com.movies.anymovies.feature.detail.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.movies.anymovies.feature.detail.presentation.model.VideoUiModel
import kotlinx.collections.immutable.ImmutableList

public object VideoSectionTestTags {
    public const val PLAY_BUTTON: String = "video_section_play_button"
    public const val THUMBNAIL_ROW: String = "video_section_thumbnail_row"
}

@Composable
internal fun TrailerPlayButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClickLabel = "Play trailer", onClick = onClick)
            .testTag(VideoSectionTestTags.PLAY_BUTTON),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Play trailer",
            tint = Color.White,
            modifier = Modifier.size(32.dp),
        )
    }
}

/** Horizontally scrollable video row. Hidden entirely for zero or one video. */
@Composable
internal fun VideoSection(
    videos: ImmutableList<VideoUiModel>,
    onVideoClick: (VideoUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (videos.size <= 1) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = "Videos", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().testTag(VideoSectionTestTags.THUMBNAIL_ROW),
        ) {
            items(videos, key = { it.id }) { video ->
                VideoThumbnail(video = video, onClick = { onVideoClick(video) })
            }
        }
    }
}

@Composable
private fun VideoThumbnail(video: VideoUiModel, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.width(160.dp).clickable(onClick = onClick)) {
        Box {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.align(Alignment.Center).size(28.dp),
            )
        }
        Text(
            text = video.name,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
