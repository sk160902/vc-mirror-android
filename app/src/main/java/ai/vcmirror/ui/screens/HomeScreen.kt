package ai.vcmirror.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ai.vcmirror.data.UiState
import ai.vcmirror.ui.theme.*

@Composable
fun HomeScreen(
    state: UiState,
    onRecord: () -> Unit,
    onSelect: () -> Unit,
    onSample: () -> Unit,
    onDismissError: () -> Unit,
) {
    Surface(color = Paper, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 64.dp, bottom = 40.dp)
        ) {
            Text(
                "VC MIRROR",
                style = MaterialTheme.typography.labelSmall,
                color = Accent,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "See what investors hear.",
                style = MaterialTheme.typography.displaySmall,
                color = Ink,
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "Record a short pitch. VC Mirror marks the exact moments that build or break conviction, checks the claims an investor would look up, and tells you which questions are coming.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft,
            )

            Spacer(Modifier.height(36.dp))

            Button(
                onClick = onRecord,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(3.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Paper),
            ) {
                Icon(Icons.Outlined.Videocam, contentDescription = null, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(10.dp))
                Text("Record Pitch", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(3.dp),
                border = BorderStroke(1.dp, Ink),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink),
            ) {
                Icon(Icons.Outlined.VideoLibrary, contentDescription = null, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(10.dp))
                Text("Select Video", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(20.dp))

            TextButton(
                onClick = onSample,
                enabled = !state.sampleLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = InkSoft),
            ) {
                if (state.sampleLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(15.dp),
                        strokeWidth = 2.dp,
                        color = Accent,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Loading sample…", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text(
                        "View Sample Analysis",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            if (state.error != null) {
                Spacer(Modifier.height(20.dp))
                ErrorBanner(message = state.error, onDismiss = onDismissError)
            }

            Spacer(Modifier.height(44.dp))
            HorizontalDivider(color = Line)
            Spacer(Modifier.height(28.dp))

            ValuePoint(
                Icons.Outlined.CenterFocusStrong,
                "Find the weak moment",
                "Feedback is attached to the exact second it happens, not summarized at the end.",
            )
            Spacer(Modifier.height(24.dp))
            ValuePoint(
                Icons.Outlined.VerifiedUser,
                "Verify the claim",
                "Checkable numbers are searched against live sources, with the citations shown.",
            )
            Spacer(Modifier.height(24.dp))
            ValuePoint(
                Icons.Outlined.QuestionAnswer,
                "Prepare the hard question",
                "Questions drawn from the specific gaps in your pitch, with answer structure.",
            )

            Spacer(Modifier.height(36.dp))
            Text(
                "Your video is sent to Google Gemini for analysis and is not stored in any VC Mirror database. Google may process or temporarily retain uploaded content under its own service terms.",
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
            )
        }
    }
}

@Composable
private fun ValuePoint(icon: ImageVector, title: String, body: String) {
    Row {
        Icon(icon, contentDescription = null, tint = Accent, modifier = Modifier.size(19.dp).padding(top = 2.dp))
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = Ink)
            Spacer(Modifier.height(4.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = InkMuted)
        }
    }
}

@Composable
fun ErrorBanner(message: String, onDismiss: (() -> Unit)? = null, onRetry: (() -> Unit)? = null) {
    Surface(color = SignalHigh.copy(alpha = 0.06f), shape = RoundedCornerShape(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = SignalHigh,
                modifier = Modifier.size(17.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(message, style = MaterialTheme.typography.bodyMedium, color = SignalHigh)
                if (onRetry != null) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onRetry,
                        shape = RoundedCornerShape(2.dp),
                        border = BorderStroke(1.dp, SignalHigh),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SignalHigh),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        Text("Retry", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (onDismiss != null) {
                IconButton(onClick = onDismiss, modifier = Modifier.size(22.dp)) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Dismiss",
                        tint = SignalHigh,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = InkSoft,
        fontFamily = FontFamily.Serif,
    )
}

@Composable
fun CenteredNote(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = InkMuted,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(20.dp),
    )
}
