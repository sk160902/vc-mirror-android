package ai.vcmirror.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/**
 * Holds an ExoPlayer and exposes seeking so the timeline can drive it.
 * Seeking pauses rather than plays: the founder is reading the analysis, not
 * watching straight through.
 */
class VideoController(val player: ExoPlayer) {
    fun seekTo(seconds: Int) {
        player.pause()
        player.seekTo(seconds.coerceAtLeast(0) * 1000L)
    }
}

@Composable
fun rememberVideoController(uri: String?): VideoController? {
    val context = LocalContext.current
    if (uri == null) return null

    val controller = remember(uri) {
        val player = ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = false
        }
        VideoController(player)
    }

    DisposableEffect(controller) {
        onDispose { controller.player.release() }
    }

    return controller
}

@Composable
fun VideoSurface(controller: VideoController, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = controller.player
                useController = true
            }
        },
    )
}

/** Self-contained player for the preview screen, where seeking is not needed. */
@Composable
fun VideoPlayer(uri: String, modifier: Modifier = Modifier) {
    val controller = rememberVideoController(uri) ?: return
    VideoSurface(controller, modifier)
}
