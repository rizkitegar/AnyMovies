package com.movies.anymovies.feature.detail.presentation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.launch

public sealed interface TrailerPlaybackState {
    public data object Loading : TrailerPlaybackState
    public data object Playing : TrailerPlaybackState
    public data class EmbedError(val message: String) : TrailerPlaybackState
    public data object NetworkError : TrailerPlaybackState
}

public object TrailerPlayerTestTags {
    public const val ROOT: String = "trailer_player"
    public const val LOADING: String = "trailer_player_loading"
    public const val CLOSE_BUTTON: String = "trailer_player_close"
    public const val OPEN_YOUTUBE_BUTTON: String = "trailer_player_open_youtube"
    public const val RETRY_BUTTON: String = "trailer_player_retry"
    public const val ERROR_MESSAGE: String = "trailer_player_error_message"
}

internal const val COULD_NOT_OPEN_YOUTUBE_MESSAGE = "Couldn't open YouTube. No app found."
private const val NETWORK_ERROR_MESSAGE = "Connection lost. Check your network and try again."

/**
 * [PlayerConstants.PlayerError.HTML_5_PLAYER] and [PlayerConstants.PlayerError.UNKNOWN] are
 * documented by YouTube as transient failures ("an error occurred, try again") rather than a
 * permanent embedding restriction, so they route to the retryable [TrailerPlaybackState.NetworkError]
 * path instead of the "open in YouTube" one.
 */
private fun isRetryable(error: PlayerConstants.PlayerError): Boolean = when (error) {
    PlayerConstants.PlayerError.HTML_5_PLAYER,
    PlayerConstants.PlayerError.UNKNOWN,
    -> true
    else -> false
}

private fun embedErrorMessageFor(error: PlayerConstants.PlayerError): String = when (error) {
    PlayerConstants.PlayerError.VIDEO_NOT_FOUND -> "This trailer is no longer available."
    else -> "This trailer can't be played here."
}

/** Fires the YouTube deep link; returns false if no app can handle it. */
internal fun launchYoutube(context: Context, videoKey: String): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, "https://www.youtube.com/watch?v=$videoKey".toUri())
    return try {
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}

/**
 * Embedded YouTube trailer player. Registers the underlying view as a lifecycle
 * observer so it pauses on backgrounding and releases on destroy, and releases
 * again via [AndroidView]'s onRelease so no instance survives navigating away.
 */
@Composable
public fun TrailerPlayer(
    videoKey: String,
    onClose: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    var playbackState by remember(videoKey) {
        mutableStateOf<TrailerPlaybackState>(TrailerPlaybackState.Loading)
    }
    // Bumped on retry to force the AndroidView below to tear down and rebuild the
    // YouTubePlayerView from scratch — a failed init leaves no live YouTubePlayer
    // to re-drive, so retrying has to restart the whole init sequence.
    var retryAttempt by remember(videoKey) { mutableStateOf(0) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val onRetry: () -> Unit = remember(videoKey) {
        {
            playbackState = TrailerPlaybackState.Loading
            retryAttempt += 1
        }
    }
    val onOpenYoutube = remember(videoKey, context, snackbarHostState, coroutineScope) {
        {
            if (!launchYoutube(context, videoKey)) {
                coroutineScope.launch { snackbarHostState.showSnackbar(COULD_NOT_OPEN_YOUTUBE_MESSAGE) }
            }
        }
    }

    TrailerPlayerFrame(
        state = playbackState,
        onClose = onClose,
        onRetry = onRetry,
        onOpenYoutube = onOpenYoutube,
        modifier = modifier,
    ) {
        key(videoKey, retryAttempt) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    YouTubePlayerView(ctx).apply {
                        lifecycleOwner.lifecycle.addObserver(this)
                        addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                youTubePlayer.loadVideo(videoKey, 0f)
                            }

                            override fun onStateChange(
                                youTubePlayer: YouTubePlayer,
                                state: PlayerConstants.PlayerState,
                            ) {
                                if (state == PlayerConstants.PlayerState.PLAYING) {
                                    playbackState = TrailerPlaybackState.Playing
                                }
                            }

                            override fun onError(
                                youTubePlayer: YouTubePlayer,
                                error: PlayerConstants.PlayerError,
                            ) {
                                playbackState = if (isRetryable(error)) {
                                    TrailerPlaybackState.NetworkError
                                } else {
                                    TrailerPlaybackState.EmbedError(embedErrorMessageFor(error))
                                }
                            }
                        })
                    }
                },
                onRelease = { view -> view.release() },
            )
        }
    }
}

@Composable
internal fun TrailerPlayerFrame(
    state: TrailerPlaybackState,
    onClose: () -> Unit,
    onRetry: () -> Unit,
    onOpenYoutube: () -> Unit,
    modifier: Modifier = Modifier,
    playerContent: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black)
            .testTag(TrailerPlayerTestTags.ROOT),
    ) {
        playerContent()

        when (state) {
            is TrailerPlaybackState.Loading -> Box(
                modifier = Modifier.fillMaxSize().testTag(TrailerPlayerTestTags.LOADING),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Color.White)
            }

            is TrailerPlaybackState.EmbedError -> TrailerErrorOverlay(
                message = state.message,
                actionLabel = "Open in YouTube",
                actionTestTag = TrailerPlayerTestTags.OPEN_YOUTUBE_BUTTON,
                onAction = onOpenYoutube,
            )

            is TrailerPlaybackState.NetworkError -> TrailerErrorOverlay(
                message = NETWORK_ERROR_MESSAGE,
                actionLabel = "Retry",
                actionTestTag = TrailerPlayerTestTags.RETRY_BUTTON,
                onAction = onRetry,
            )

            is TrailerPlaybackState.Playing -> Unit
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopEnd).testTag(TrailerPlayerTestTags.CLOSE_BUTTON),
        ) {
            Icon(imageVector = Icons.Filled.Close, contentDescription = "Close trailer", tint = Color.White)
        }
    }
}

@Composable
private fun TrailerErrorOverlay(
    message: String,
    actionLabel: String,
    actionTestTag: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag(TrailerPlayerTestTags.ERROR_MESSAGE),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAction, modifier = Modifier.testTag(actionTestTag)) {
            Text(text = actionLabel)
        }
    }
}
