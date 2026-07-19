package ai.vcmirror.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import ai.vcmirror.data.*
import ai.vcmirror.ui.components.VideoSurface
import ai.vcmirror.ui.components.rememberVideoController
import ai.vcmirror.ui.theme.*

@Composable
fun ResultsScreen(
    state: UiState,
    onSelectMoment: (String) -> Unit,
    onRetryVerification: () -> Unit,
    onBack: () -> Unit,
) {
    val analysis = state.analysis ?: return
    val videoUri = if (state.isSample) state.sampleVideoUrl else state.videoUri?.toString()
    val controller = rememberVideoController(videoUri)
    val selected = state.selectedMomentId?.let { analysis.moment(it) }

    fun seek(seconds: Int) {
        controller?.seekTo(seconds)
    }

    Surface(color = Paper, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 44.dp, bottom = 44.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onBack,
                    colors = ButtonDefaults.textButtonColors(contentColor = InkMuted),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Analyze another", style = MaterialTheme.typography.bodyMedium)
                }
                if (state.isSample) {
                    Surface(
                        color = PaperRaised,
                        border = BorderStroke(1.dp, LineStrong),
                        shape = RoundedCornerShape(2.dp),
                    ) {
                        Text(
                            "PRE-ANALYZED SAMPLE",
                            style = MaterialTheme.typography.labelSmall,
                            color = InkMuted,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                analysis.company.name ?: "Your pitch",
                style = MaterialTheme.typography.displaySmall,
                color = Ink,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                analysis.overallSummary.oneSentenceAssessment,
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft,
            )

            Spacer(Modifier.height(24.dp))

            if (controller != null) {
                VideoSurface(
                    controller,
                    Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(Color.Black),
                )
            } else {
                Surface(
                    color = PaperRaised,
                    border = BorderStroke(1.dp, LineStrong),
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CenteredNote("Video unavailable. The analysis below is complete and interactive.")
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
            MetricRow(analysis, state)

            Spacer(Modifier.height(28.dp))
            SectionLabel("Pitch timeline")
            Spacer(Modifier.height(10.dp))
            TimelineStrip(
                analysis = analysis,
                selectedId = state.selectedMomentId,
                onSelect = { event ->
                    onSelectMoment(event.id)
                    seek(event.timestampSeconds)
                },
            )

            Spacer(Modifier.height(22.dp))
            HighlightCards(analysis) { id ->
                onSelectMoment(id)
                analysis.moment(id)?.let { seek(it.timestampSeconds) }
            }

            Spacer(Modifier.height(28.dp))
            SectionLabel("Selected moment")
            Spacer(Modifier.height(10.dp))
            MomentInspector(selected)

            Spacer(Modifier.height(34.dp))
            RubricSection(analysis, ::seek)

            Spacer(Modifier.height(34.dp))
            QuestionsSection(analysis, ::seek)

            Spacer(Modifier.height(34.dp))
            VerificationSection(
                analysis = analysis,
                verification = state.verification,
                loading = state.verificationLoading,
                error = state.verificationError,
                onRetry = onRetryVerification,
                onSeek = ::seek,
            )

            Spacer(Modifier.height(36.dp))
            HorizontalDivider(color = Line)
            Spacer(Modifier.height(14.dp))
            Text(
                "The pitch readiness heuristic is a structured preparation aid computed from the six rubric scores. It is not a prediction of funding.",
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
            )
        }
    }
}

@Composable
private fun MetricRow(analysis: PitchAnalysis, state: UiState) {
    val coverage = when {
        analysis.claimsToVerify.isEmpty() -> "—"
        state.verificationLoading -> "…"
        state.verification != null &&
            state.verification.status != "insufficient_evidence" &&
            state.verification.sources.isNotEmpty() -> "100%"
        state.verification != null -> "0%"
        else -> "—"
    }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Metric("Readiness", analysis.readinessScore.toString(), Modifier.weight(1f))
        Metric("High risk", analysis.highRiskMoments.toString(), Modifier.weight(1f))
        Metric("Evidence", coverage, Modifier.weight(1f))
        Metric("Questions", analysis.investorQuestions.size.toString(), Modifier.weight(1f))
    }
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = PaperRaised,
        border = BorderStroke(1.dp, Line),
        shape = RoundedCornerShape(2.dp),
    ) {
        Column(Modifier.padding(vertical = 12.dp, horizontal = 10.dp)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = InkMuted)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, color = Ink)
        }
    }
}

/** Horizontal clickable timestamp timeline. Tapping seeks the player. */
@Composable
private fun TimelineStrip(
    analysis: PitchAnalysis,
    selectedId: String?,
    onSelect: (TimelineEvent) -> Unit,
) {
    if (analysis.timeline.isEmpty()) {
        CenteredNote("No distinct moments were identified in this pitch.")
        return
    }

    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        analysis.timeline.forEach { event ->
            val isSelected = event.id == selectedId
            val tone = momentColor(event.isPositive, event.severity)

            Surface(
                color = if (isSelected) Ink else PaperRaised,
                border = BorderStroke(1.dp, if (isSelected) Ink else Line),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.clickable { onSelect(event) },
            ) {
                Row(
                    Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(tone))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            formatTimestamp(event.timestampSeconds),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected) Paper else Ink,
                        )
                        Text(
                            Rubric.MOMENT_LABELS[event.type] ?: event.type,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) Paper.copy(alpha = 0.75f) else InkMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HighlightCards(analysis: PitchAnalysis, onJump: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        analysis.strongestMoment?.let { moment ->
            HighlightCard(
                "Strongest moment",
                SignalLow,
                Icons.Outlined.EmojiEvents,
                moment,
                Modifier.weight(1f),
            ) { onJump(moment.id) }
        }
        analysis.biggestConcern?.let { moment ->
            HighlightCard(
                "Biggest concern",
                SignalHigh,
                Icons.Outlined.WarningAmber,
                moment,
                Modifier.weight(1f),
            ) { onJump(moment.id) }
        }
    }
}

@Composable
private fun HighlightCard(
    label: String,
    tone: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    moment: TimelineEvent,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = PaperRaised,
        border = BorderStroke(1.dp, Line),
        shape = RoundedCornerShape(2.dp),
    ) {
        Column(Modifier.padding(13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = tone, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = tone)
            }
            Spacer(Modifier.height(7.dp))
            Text(
                formatTimestamp(moment.timestampSeconds),
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
            )
            Spacer(Modifier.height(3.dp))
            Text(moment.observation, style = MaterialTheme.typography.bodySmall, color = InkSoft)
        }
    }
}

/** The core panel: what was said, what an investor hears, why, what is missing. */
@Composable
private fun MomentInspector(moment: TimelineEvent?) {
    if (moment == null) {
        Surface(color = PaperRaised, border = BorderStroke(1.dp, Line), shape = RoundedCornerShape(2.dp)) {
            CenteredNote("Select a moment on the timeline to see what an investor takes from it.")
        }
        return
    }

    val tone = momentColor(moment.isPositive, moment.severity)

    Surface(color = PaperRaised, border = BorderStroke(1.dp, Line), shape = RoundedCornerShape(2.dp)) {
        Column {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    formatTimestamp(moment.timestampSeconds),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Ink,
                )
                Spacer(Modifier.width(10.dp))
                Surface(color = tone.copy(alpha = 0.12f), shape = RoundedCornerShape(2.dp)) {
                    Text(
                        Rubric.MOMENT_LABELS[moment.type] ?: moment.type,
                        style = MaterialTheme.typography.labelSmall,
                        color = tone,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    )
                }
            }
            HorizontalDivider(color = Line)

            Column(Modifier.padding(16.dp)) {
                Field("What you said") {
                    Row {
                        Box(Modifier.width(2.dp).height(IntrinsicSize.Min).background(tone))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "“${moment.quote}”",
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkSoft,
                            fontStyle = FontStyle.Italic,
                        )
                    }
                }
                Field("What an investor may hear") {
                    Text(moment.investorInterpretation, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                }
                Field("Why it matters") {
                    Text(moment.whyItMatters, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                }
                if (moment.missingInformation.isNotEmpty()) {
                    Field("What is missing") {
                        Column {
                            moment.missingInformation.forEach {
                                Text("•  $it", style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                            }
                        }
                    }
                }
                moment.strongerWording?.let { stronger ->
                    Surface(
                        color = AccentSoft,
                        border = BorderStroke(1.dp, Accent.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(2.dp),
                    ) {
                        Column(Modifier.padding(13.dp)) {
                            Text("STRONGER WORDING", style = MaterialTheme.typography.labelSmall, color = Accent)
                            Spacer(Modifier.height(6.dp))
                            Text("“$stronger”", style = MaterialTheme.typography.bodyMedium, color = Ink)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Field(label: String, content: @Composable () -> Unit) {
    Column(Modifier.padding(bottom = 16.dp)) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = InkMuted)
        Spacer(Modifier.height(6.dp))
        content()
    }
}

@Composable
private fun RubricSection(analysis: PitchAnalysis, onSeek: (Int) -> Unit) {
    if (analysis.rubric.isEmpty()) return

    SectionLabel("Evaluation rubric")
    Spacer(Modifier.height(4.dp))
    Text(
        "Six dimensions scored against what is present in the video. The readiness heuristic is computed from these, not generated by the model.",
        style = MaterialTheme.typography.bodySmall,
        color = InkMuted,
    )
    Spacer(Modifier.height(12.dp))

    Surface(color = PaperRaised, border = BorderStroke(1.dp, Line), shape = RoundedCornerShape(2.dp)) {
        Column {
            analysis.rubric.forEachIndexed { index, entry ->
                if (index > 0) HorizontalDivider(color = Line)
                Column(Modifier.padding(14.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            Rubric.LABELS[entry.dimension] ?: entry.dimension,
                            style = MaterialTheme.typography.titleMedium,
                            color = Ink,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "${entry.score} / 5",
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkMuted,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(5) { i ->
                            val filled = i < entry.score
                            val tone = when {
                                entry.score >= 4 -> SignalLow
                                entry.score >= 3 -> SignalMedium
                                else -> SignalHigh
                            }
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (filled) tone else Line)
                            )
                        }
                    }
                    Spacer(Modifier.height(9.dp))
                    Text(entry.summary, style = MaterialTheme.typography.bodyMedium, color = InkSoft)

                    if (entry.evidenceTimestamps.isNotEmpty()) {
                        Spacer(Modifier.height(9.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            entry.evidenceTimestamps.forEach { ts ->
                                Surface(
                                    border = BorderStroke(1.dp, Line),
                                    shape = RoundedCornerShape(2.dp),
                                    color = Paper,
                                    modifier = Modifier.clickable { onSeek(ts) },
                                ) {
                                    Text(
                                        formatTimestamp(ts),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = InkSoft,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionsSection(analysis: PitchAnalysis, onSeek: (Int) -> Unit) {
    if (analysis.investorQuestions.isEmpty()) return
    var openId by remember { mutableStateOf(analysis.investorQuestions.firstOrNull()?.id) }

    SectionLabel("Questions to prepare for")
    Spacer(Modifier.height(4.dp))
    Text(
        "Each question is tied to a specific weakness in the pitch.",
        style = MaterialTheme.typography.bodySmall,
        color = InkMuted,
    )
    Spacer(Modifier.height(12.dp))

    Surface(color = PaperRaised, border = BorderStroke(1.dp, Line), shape = RoundedCornerShape(2.dp)) {
        Column {
            analysis.investorQuestions.forEachIndexed { index, q ->
                if (index > 0) HorizontalDivider(color = Line)
                val open = openId == q.id

                Column(
                    Modifier
                        .clickable { openId = if (open) null else q.id }
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            "%02d".format(index + 1),
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted,
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            q.question,
                            style = MaterialTheme.typography.titleMedium,
                            color = Ink,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            if (open) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            tint = InkMuted,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    if (open) {
                        Spacer(Modifier.height(12.dp))
                        Column(Modifier.padding(start = 30.dp)) {
                            Text("WHY THEY WILL ASK", style = MaterialTheme.typography.labelSmall, color = InkMuted)
                            Spacer(Modifier.height(5.dp))
                            Text(q.reason, style = MaterialTheme.typography.bodyMedium, color = InkSoft)

                            q.triggerTimestampSeconds?.let { ts ->
                                Spacer(Modifier.height(8.dp))
                                Surface(
                                    border = BorderStroke(1.dp, Line),
                                    shape = RoundedCornerShape(2.dp),
                                    color = Paper,
                                    modifier = Modifier.clickable { onSeek(ts) },
                                ) {
                                    Text(
                                        formatTimestamp(ts),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = InkSoft,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    )
                                }
                            }

                            if (q.answerFramework.isNotEmpty()) {
                                Spacer(Modifier.height(14.dp))
                                Text("HOW TO STRUCTURE THE ANSWER", style = MaterialTheme.typography.labelSmall, color = InkMuted)
                                Spacer(Modifier.height(6.dp))
                                q.answerFramework.forEachIndexed { i, step ->
                                    Row(Modifier.padding(bottom = 4.dp)) {
                                        Text(
                                            "${i + 1}.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = InkMuted,
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(step, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VerificationSection(
    analysis: PitchAnalysis,
    verification: VerifiedClaim?,
    loading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onSeek: (Int) -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    SectionLabel("Claim verification")
    Spacer(Modifier.height(4.dp))
    Text(
        "Checkable claims are searched with Google Search grounding. Sources are the actual citations returned by the search tool.",
        style = MaterialTheme.typography.bodySmall,
        color = InkMuted,
    )
    Spacer(Modifier.height(12.dp))

    val claim = analysis.claimsToVerify.firstOrNull()
    if (claim == null) {
        Surface(color = PaperRaised, border = BorderStroke(1.dp, Line), shape = RoundedCornerShape(2.dp)) {
            CenteredNote("No externally verifiable claims were detected in this pitch.")
        }
        return
    }

    Surface(color = PaperRaised, border = BorderStroke(1.dp, Line), shape = RoundedCornerShape(2.dp)) {
        Column {
            Column(Modifier.padding(14.dp)) {
                Surface(
                    border = BorderStroke(1.dp, Line),
                    shape = RoundedCornerShape(2.dp),
                    color = Paper,
                    modifier = Modifier.clickable { onSeek(claim.timestampSeconds) },
                ) {
                    Text(
                        formatTimestamp(claim.timestampSeconds),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSoft,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "“${claim.claim}”",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Ink,
                )
            }
            HorizontalDivider(color = Line)

            when {
                loading -> Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(Modifier.size(15.dp), strokeWidth = 2.dp, color = Accent)
                    Spacer(Modifier.width(10.dp))
                    Text("Searching for evidence…", style = MaterialTheme.typography.bodyMedium, color = InkMuted)
                }

                error != null -> Column(Modifier.padding(16.dp)) {
                    Text("External verification unavailable.", style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                    Spacer(Modifier.height(4.dp))
                    Text(error, style = MaterialTheme.typography.bodySmall, color = InkMuted)
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onRetry,
                        shape = RoundedCornerShape(2.dp),
                        border = BorderStroke(1.dp, Ink),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink),
                    ) {
                        Text("Retry verification", style = MaterialTheme.typography.bodySmall)
                    }
                }

                verification != null -> Column(Modifier.padding(14.dp)) {
                    val tone = when (verification.status) {
                        "supported" -> SignalLow
                        "partially_supported" -> SignalMedium
                        "contradicted" -> SignalHigh
                        else -> InkMuted
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = tone.copy(alpha = 0.12f), shape = RoundedCornerShape(2.dp)) {
                            Text(
                                Rubric.statusLabel(verification.status),
                                style = MaterialTheme.typography.labelSmall,
                                color = tone,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            )
                        }
                        Spacer(Modifier.width(9.dp))
                        Text(
                            "Evidence strength: ${verification.evidenceStrength}",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted,
                        )
                    }

                    Spacer(Modifier.height(11.dp))
                    Text(verification.explanation, style = MaterialTheme.typography.bodyMedium, color = InkSoft)

                    if (verification.missingContext.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        Text("MISSING CONTEXT", style = MaterialTheme.typography.labelSmall, color = InkMuted)
                        Spacer(Modifier.height(5.dp))
                        verification.missingContext.forEach {
                            Text("•  $it", style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                        }
                    }

                    if (verification.saferWording.isNotBlank()) {
                        Spacer(Modifier.height(14.dp))
                        Surface(
                            color = AccentSoft,
                            border = BorderStroke(1.dp, Accent.copy(alpha = 0.25f)),
                            shape = RoundedCornerShape(2.dp),
                        ) {
                            Column(Modifier.padding(13.dp)) {
                                Text("SAFER WORDING", style = MaterialTheme.typography.labelSmall, color = Accent)
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "“${verification.saferWording}”",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Ink,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text("SOURCES", style = MaterialTheme.typography.labelSmall, color = InkMuted)
                    Spacer(Modifier.height(6.dp))
                    if (verification.sources.isEmpty()) {
                        Text(
                            "No grounded citations were returned for this claim.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkMuted,
                        )
                    } else {
                        verification.sources.forEach { source ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { runCatching { uriHandler.openUri(source.url) } }
                                    .padding(vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = null,
                                    tint = InkMuted,
                                    modifier = Modifier.size(13.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    source.publisher ?: source.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = InkSoft,
                                )
                            }
                        }
                    }
                }

                else -> CenteredNote("Verification has not run for this claim.")
            }
        }
    }
}
