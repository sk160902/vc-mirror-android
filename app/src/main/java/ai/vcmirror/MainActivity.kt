package ai.vcmirror

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.vcmirror.data.PitchViewModel
import ai.vcmirror.data.Screen
import ai.vcmirror.ui.screens.HomeScreen
import ai.vcmirror.ui.screens.PreviewScreen
import ai.vcmirror.ui.screens.ProcessingScreen
import ai.vcmirror.ui.screens.ResultsScreen
import ai.vcmirror.ui.theme.VCMirrorTheme
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VCMirrorTheme {
                val vm: PitchViewModel = viewModel()
                val state by vm.state.collectAsState()
                val context = this

                // Copy the picked video into app cache so we own a real File to
                // upload; content:// URIs are not directly readable as files.
                val pickVideo = androidx.activity.compose.rememberLauncherForActivityResult(
                    ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    if (uri != null) {
                        val file = copyToCache(context, uri)
                        if (file != null) {
                            vm.onVideoSelected(uri, file, durationOf(context, uri))
                        }
                    }
                }

                var recordTarget: Pair<Uri, File>? = null
                val recordVideo = androidx.activity.compose.rememberLauncherForActivityResult(
                    ActivityResultContracts.CaptureVideo()
                ) { success: Boolean ->
                    val target = recordTarget
                    if (success && target != null) {
                        vm.onVideoSelected(target.first, target.second, durationOf(context, target.first))
                    }
                }

                when (state.screen) {
                    Screen.HOME -> HomeScreen(
                        state = state,
                        onRecord = {
                            val target = newRecordingTarget(context)
                            recordTarget = target
                            recordVideo.launch(target.first)
                        },
                        onSelect = { pickVideo.launch("video/*") },
                        onSample = vm::loadSample,
                        onDismissError = vm::dismissError,
                    )

                    Screen.PREVIEW -> PreviewScreen(
                        state = state,
                        onAnalyze = vm::analyze,
                        onChange = vm::clearVideo,
                        onBack = vm::goHome,
                        onDismissError = vm::dismissError,
                    )

                    Screen.PROCESSING -> ProcessingScreen(state = state)

                    Screen.RESULTS -> ResultsScreen(
                        state = state,
                        onSelectMoment = vm::selectMoment,
                        onRetryVerification = vm::verify,
                        onBack = vm::goHome,
                    )
                }
            }
        }
    }

    private fun newRecordingTarget(context: Context): Pair<Uri, File> {
        val dir = File(context.cacheDir, "pitches").apply { mkdirs() }
        val file = File(dir, "pitch_${System.currentTimeMillis()}.mp4")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return uri to file
    }

    private fun copyToCache(context: Context, uri: Uri): File? = try {
        val dir = File(context.cacheDir, "pitches").apply { mkdirs() }
        val file = File(dir, "pitch_${System.currentTimeMillis()}.mp4")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        file.takeIf { it.length() > 0 }
    } catch (e: Exception) {
        null
    }

    private fun durationOf(context: Context, uri: Uri): Int = try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val ms = retriever
            .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            ?.toLongOrNull() ?: 0L
        retriever.release()
        (ms / 1000).toInt().coerceAtLeast(1)
    } catch (e: Exception) {
        60
    }
}
