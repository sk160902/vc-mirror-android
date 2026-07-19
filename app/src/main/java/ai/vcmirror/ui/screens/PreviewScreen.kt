package ai.vcmirror.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ai.vcmirror.data.UiState
import ai.vcmirror.data.formatTimestamp
import ai.vcmirror.ui.components.VideoPlayer
import ai.vcmirror.ui.theme.*

@Composable
fun PreviewScreen(
    state: UiState,
    onAnalyze: () -> Unit,
    onChange: () -> Unit,
    onBack: () -> Unit,
    onDismissError: () -> Unit,
) {
    val tooLong = state.videoDurationSeconds > 60

    Surface(color = Paper, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 32.dp)
        ) {
            TextButton(
                onClick = onBack,
                colors = ButtonDefaults.textButtonColors(contentColor = InkMuted),
                contentPadding = PaddingValues(0.dp),
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Back", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(20.dp))
            Text("Your pitch", style = MaterialTheme.typography.headlineMedium, color = Ink)
            Spacer(Modifier.height(6.dp))
            Text(
                "Length ${formatTimestamp(state.videoDurationSeconds)}",
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
            )

            Spacer(Modifier.height(20.dp))

            state.videoUri?.let { uri ->
                VideoPlayer(
                    uri = uri.toString(),
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                )
            }

            if (tooLong) {
                Spacer(Modifier.height(14.dp))
                Surface(color = SignalMedium.copy(alpha = 0.07f), shape = RoundedCornerShape(2.dp)) {
                    Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Outlined.WarningAmber,
                            contentDescription = null,
                            tint = SignalMedium,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(9.dp))
                        Text(
                            "This pitch is ${state.videoDurationSeconds}s. VC Mirror is tuned for 60 seconds or less, so analysis may be less precise.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SignalMedium,
                        )
                    }
                }
            }

            if (state.error != null) {
                Spacer(Modifier.height(16.dp))
                ErrorBanner(
                    message = state.error,
                    onDismiss = onDismissError,
                    onRetry = if (state.errorRetryable) onAnalyze else null,
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onAnalyze,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(3.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Paper),
            ) {
                Text("Analyze My Pitch", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(9.dp))
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(17.dp))
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onChange,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(3.dp),
                border = BorderStroke(1.dp, Line),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = InkSoft),
            ) {
                Text("Change video", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(14.dp))
            Text(
                "Analysis usually takes 20 to 45 seconds.",
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
