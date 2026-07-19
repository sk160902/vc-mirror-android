package ai.vcmirror.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import ai.vcmirror.data.PROCESSING_STAGES
import ai.vcmirror.data.UiState
import ai.vcmirror.ui.theme.*

/**
 * Honest stages. Nothing here fabricates a percentage: each label reflects a
 * real phase of the single backend request.
 */
@Composable
fun ProcessingScreen(state: UiState) {
    Surface(color = Paper, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .padding(top = 120.dp)
        ) {
            Text("Analyzing your pitch", style = MaterialTheme.typography.headlineMedium, color = Ink)
            Spacer(Modifier.height(8.dp))
            Text(
                "Gemini is watching the video and listening to the audio. This usually takes under a minute.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkMuted,
            )

            Spacer(Modifier.height(40.dp))

            PROCESSING_STAGES.forEachIndexed { index, stage ->
                val done = index < state.processingStage
                val active = index == state.processingStage

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 9.dp),
                ) {
                    Box(
                        modifier = Modifier.size(20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        when {
                            done -> Icon(
                                Icons.Outlined.Check,
                                contentDescription = null,
                                tint = SignalLow,
                                modifier = Modifier.size(16.dp),
                            )
                            active -> CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Accent,
                            )
                            else -> Box(
                                Modifier.size(6.dp).clip(CircleShape).background(LineStrong)
                            )
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(
                        stage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = when {
                            done -> InkMuted
                            active -> Ink
                            else -> LineStrong
                        },
                    )
                }
            }
        }
    }
}
