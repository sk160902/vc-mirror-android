package ai.vcmirror.data

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

enum class Screen { HOME, PREVIEW, PROCESSING, RESULTS }

/** Honest stage labels. No fabricated percentages. */
val PROCESSING_STAGES = listOf(
    "Uploading your pitch",
    "Understanding the company",
    "Mapping key moments",
    "Preparing investor questions",
    "Building your report",
)

data class UiState(
    val screen: Screen = Screen.HOME,
    val videoUri: Uri? = null,
    val videoFile: File? = null,
    val videoDurationSeconds: Int = 0,
    val isSample: Boolean = false,
    val sampleVideoUrl: String? = null,

    val analysis: PitchAnalysis? = null,
    val selectedMomentId: String? = null,

    val verification: VerifiedClaim? = null,
    val verificationLoading: Boolean = false,
    val verificationError: String? = null,

    val processingStage: Int = 0,
    val error: String? = null,
    val errorRetryable: Boolean = false,
    val sampleLoading: Boolean = false,
)

class PitchViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var analyzeJob: Job? = null
    private var stageJob: Job? = null

    fun onVideoSelected(uri: Uri, file: File, durationSeconds: Int) {
        _state.value = _state.value.copy(
            screen = Screen.PREVIEW,
            videoUri = uri,
            videoFile = file,
            videoDurationSeconds = durationSeconds,
            isSample = false,
            error = null,
        )
    }

    fun clearVideo() {
        _state.value.videoFile?.delete()
        _state.value = _state.value.copy(
            screen = Screen.HOME,
            videoUri = null,
            videoFile = null,
            videoDurationSeconds = 0,
            error = null,
        )
    }

    fun goHome() {
        analyzeJob?.cancel()
        stageJob?.cancel()
        _state.value.videoFile?.delete()
        _state.value = UiState()
    }

    fun dismissError() {
        _state.value = _state.value.copy(error = null)
    }

    fun analyze() {
        val file = _state.value.videoFile ?: return
        val duration = _state.value.videoDurationSeconds

        _state.value = _state.value.copy(
            screen = Screen.PROCESSING,
            processingStage = 0,
            error = null,
            verification = null,
            verificationError = null,
        )

        // Advance visible stages while the single request is in flight. Stops
        // short of the last stage so it never claims completion on its own.
        stageJob?.cancel()
        stageJob = viewModelScope.launch {
            for (i in 1 until PROCESSING_STAGES.size - 1) {
                kotlinx.coroutines.delay(5000)
                _state.value = _state.value.copy(processingStage = i)
            }
        }

        analyzeJob?.cancel()
        analyzeJob = viewModelScope.launch {
            try {
                val analysis = VcMirrorApi.analyzePitch(file, duration)
                stageJob?.cancel()
                _state.value = _state.value.copy(
                    screen = Screen.RESULTS,
                    analysis = analysis,
                    selectedMomentId = analysis.biggestConcern?.id
                        ?: analysis.timeline.firstOrNull()?.id,
                    processingStage = PROCESSING_STAGES.lastIndex,
                    isSample = false,
                )
                if (analysis.claimsToVerify.isNotEmpty()) verify()
            } catch (e: ApiException) {
                stageJob?.cancel()
                _state.value = _state.value.copy(
                    screen = Screen.PREVIEW,
                    error = e.message,
                    errorRetryable = e.retryable,
                )
            }
        }
    }

    /** Verification is a separate call so a grounding failure never clears the report. */
    fun verify() {
        val analysis = _state.value.analysis ?: return
        if (analysis.claimsToVerify.isEmpty()) return

        _state.value = _state.value.copy(verificationLoading = true, verificationError = null)
        viewModelScope.launch {
            try {
                val result = VcMirrorApi.verifyClaim(analysis.analysisId)
                _state.value = _state.value.copy(
                    verification = result,
                    verificationLoading = false,
                )
            } catch (e: ApiException) {
                _state.value = _state.value.copy(
                    verificationLoading = false,
                    verificationError = e.message,
                )
            }
        }
    }

    fun loadSample() {
        _state.value = _state.value.copy(sampleLoading = true, error = null)
        viewModelScope.launch {
            try {
                val sample = VcMirrorApi.fetchSample()
                _state.value = UiState(
                    screen = Screen.RESULTS,
                    analysis = sample.analysis,
                    verification = sample.verification.firstOrNull(),
                    selectedMomentId = sample.analysis.biggestConcern?.id
                        ?: sample.analysis.timeline.firstOrNull()?.id,
                    isSample = true,
                    sampleVideoUrl = VcMirrorApi.absoluteUrl(sample.sampleVideoUrl),
                )
            } catch (e: ApiException) {
                _state.value = _state.value.copy(
                    sampleLoading = false,
                    error = e.message,
                    errorRetryable = e.retryable,
                )
            }
        }
    }

    fun selectMoment(id: String) {
        _state.value = _state.value.copy(selectedMomentId = id)
    }
}
