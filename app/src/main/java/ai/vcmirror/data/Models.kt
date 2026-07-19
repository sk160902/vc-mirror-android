package ai.vcmirror.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mirrors the backend contract in docs/android-api.md. Every field the server
 * may omit is nullable or defaulted so an older client never crashes on a
 * newer response.
 */

@Serializable
data class Company(
    val name: String? = null,
    val problem: String = "",
    val customer: String = "",
    val solution: String = "",
    val businessModel: String = "",
    val missingInformation: List<String> = emptyList(),
)

@Serializable
data class RubricEntry(
    val dimension: String = "",
    val score: Int = 0,
    val summary: String = "",
    val evidenceTimestamps: List<Int> = emptyList(),
)

@Serializable
data class TimelineEvent(
    val id: String = "",
    val timestampSeconds: Int = 0,
    val endTimestampSeconds: Int? = null,
    val type: String = "",
    val severity: String = "low",
    val quote: String = "",
    val observation: String = "",
    val investorInterpretation: String = "",
    val whyItMatters: String = "",
    val missingInformation: List<String> = emptyList(),
    val strongerWording: String? = null,
) {
    val isPositive: Boolean
        get() = type == "conviction_builder" || type == "strong_moment"
}

@Serializable
data class InvestorQuestion(
    val id: String = "",
    val question: String = "",
    val reason: String = "",
    val triggerTimestampSeconds: Int? = null,
    val answerFramework: List<String> = emptyList(),
)

@Serializable
data class ClaimToVerify(
    val id: String = "",
    val claim: String = "",
    val timestampSeconds: Int = 0,
    val importance: Int = 1,
    val verificationQuery: String = "",
)

@Serializable
data class OverallSummary(
    val strongestMomentId: String = "",
    val biggestConcernMomentId: String = "",
    val oneSentenceAssessment: String = "",
)

@Serializable
data class PitchAnalysis(
    val analysisId: String = "",
    val durationSeconds: Int = 0,
    val company: Company = Company(),
    val rubric: List<RubricEntry> = emptyList(),
    val timeline: List<TimelineEvent> = emptyList(),
    val investorQuestions: List<InvestorQuestion> = emptyList(),
    val claimsToVerify: List<ClaimToVerify> = emptyList(),
    val overallSummary: OverallSummary = OverallSummary(),
) {
    /**
     * Pitch readiness heuristic. Computed on-device from the rubric, exactly as
     * the web client does, so the number stays inspectable and is never a
     * model-generated score.
     */
    val readinessScore: Int
        get() {
            if (rubric.isEmpty()) return 0
            val total = rubric.sumOf { it.score }
            return Math.round(total.toFloat() / (rubric.size * 5) * 100)
        }

    val highRiskMoments: Int
        get() = timeline.count { it.severity == "high" && !it.isPositive }

    fun moment(id: String): TimelineEvent? = timeline.firstOrNull { it.id == id }

    val strongestMoment: TimelineEvent?
        get() = moment(overallSummary.strongestMomentId) ?: timeline.firstOrNull { it.isPositive }

    val biggestConcern: TimelineEvent?
        get() = moment(overallSummary.biggestConcernMomentId)
            ?: timeline.firstOrNull { it.severity == "high" && !it.isPositive }
}

@Serializable
data class Source(
    val title: String = "",
    val publisher: String? = null,
    val url: String = "",
)

@Serializable
data class VerifiedClaim(
    val claimId: String = "",
    val status: String = "insufficient_evidence",
    val explanation: String = "",
    val evidenceFor: List<String> = emptyList(),
    val evidenceAgainst: List<String> = emptyList(),
    val missingContext: List<String> = emptyList(),
    val saferWording: String = "",
    val evidenceStrength: String = "weak",
    val sources: List<Source> = emptyList(),
)

@Serializable
data class AnalyzeResponse(val analysis: PitchAnalysis)

@Serializable
data class VerifyClaimResponse(val verification: VerifiedClaim? = null)

@Serializable
data class SampleResponse(
    val analysis: PitchAnalysis,
    val verification: List<VerifiedClaim> = emptyList(),
    val sampleVideoUrl: String? = null,
)

@Serializable
data class ApiErrorBody(
    val error: String = "Something went wrong.",
    val retryable: Boolean = true,
)

@Serializable
data class VerifyClaimRequest(
    @SerialName("analysisId") val analysisId: String,
    @SerialName("claimId") val claimId: String? = null,
)

/** Surfaced to the UI so it can decide whether to offer a retry control. */
class ApiException(
    message: String,
    val retryable: Boolean,
) : Exception(message)

object Rubric {
    val LABELS = mapOf(
        "problem_urgency" to "Problem urgency",
        "solution_clarity" to "Solution clarity",
        "evidence_traction" to "Evidence and traction",
        "market_gtm" to "Market and go-to-market",
        "differentiation_defensibility" to "Differentiation and defensibility",
        "delivery_structure" to "Delivery and structure",
    )

    val MOMENT_LABELS = mapOf(
        "conviction_builder" to "Conviction builder",
        "clarity_gap" to "Clarity gap",
        "evidence_gap" to "Evidence gap",
        "investor_objection" to "Investor objection",
        "defensibility_risk" to "Defensibility risk",
        "strong_moment" to "Strong moment",
    )

    fun statusLabel(status: String): String =
        status.split('_').joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
}

fun formatTimestamp(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    return "%02d:%02d".format(safe / 60, safe % 60)
}
