package ai.vcmirror.data

import ai.vcmirror.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Talks to the VC Mirror Cloud Run backend. There is no Gemini key here and
 * none is needed: every model call happens server-side.
 */
object VcMirrorApi {

    /** The single configurable backend location, set in app/build.gradle.kts. */
    val baseUrl: String = BuildConfig.API_BASE_URL.trimEnd('/')

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    // Analysis takes 20-45s and grounded verification 25-60s, so the read
    // timeout is deliberately generous.
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .build()

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    /** Maps transport and HTTP failures onto one actionable message. */
    private inline fun <reified T> execute(request: Request): T {
        val response = try {
            client.newCall(request).execute()
        } catch (e: IOException) {
            throw ApiException(
                "Could not reach VC Mirror. Check your connection and try again.",
                retryable = true,
            )
        }

        response.use {
            val body = it.body?.string().orEmpty()
            if (!it.isSuccessful) {
                val parsed = runCatching { json.decodeFromString<ApiErrorBody>(body) }.getOrNull()
                throw ApiException(
                    parsed?.error ?: "The server returned an error (${it.code}).",
                    retryable = parsed?.retryable ?: (it.code >= 500),
                )
            }
            return runCatching { json.decodeFromString<T>(body) }.getOrElse {
                throw ApiException("We could not read the server response.", retryable = true)
            }
        }
    }

    suspend fun fetchSample(): SampleResponse = withContext(Dispatchers.IO) {
        execute<SampleResponse>(Request.Builder().url("$baseUrl/api/sample").get().build())
    }

    suspend fun analyzePitch(file: File, durationSeconds: Int): PitchAnalysis =
        withContext(Dispatchers.IO) {
            val mime = if (file.extension.lowercase() == "webm") "video/webm" else "video/mp4"
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("video", file.name, file.asRequestBody(mime.toMediaType()))
                .addFormDataPart("durationSeconds", durationSeconds.toString())
                .build()

            val request = Request.Builder().url("$baseUrl/api/analyze-pitch").post(body).build()
            execute<AnalyzeResponse>(request).analysis
        }

    /** Single-claim endpoint. Returns null when the pitch had no checkable claim. */
    suspend fun verifyClaim(analysisId: String, claimId: String? = null): VerifiedClaim? =
        withContext(Dispatchers.IO) {
            val payload = json.encodeToString(
                VerifyClaimRequest.serializer(),
                VerifyClaimRequest(analysisId, claimId),
            )
            val request = Request.Builder()
                .url("$baseUrl/api/verify-claim")
                .post(payload.toRequestBody(jsonMedia))
                .build()
            execute<VerifyClaimResponse>(request).verification
        }

    /** Resolves the relative sampleVideoUrl the server returns. */
    fun absoluteUrl(path: String?): String? =
        when {
            path.isNullOrBlank() -> null
            path.startsWith("http") -> path
            else -> "$baseUrl$path"
        }
}
