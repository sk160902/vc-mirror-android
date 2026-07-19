# VC Mirror for Android

**See what investors hear.**

Native Kotlin + Jetpack Compose client for [VC Mirror](https://github.com/sk160902/vc-mirror),
which gives founders timestamped investor feedback on a short pitch video.

Backend: **https://vc-mirror-363277092393.us-central1.run.app**

---

## No API key in this project

Every Gemini call happens on the Cloud Run backend. This app holds no
credential of any kind and needs none: it posts a video and reads JSON back.

There is exactly one configurable value, in `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://vc-mirror-363277092393.us-central1.run.app\"")
```

Point it at your own deployment by editing that line.

---

## Screens

| Screen | Contents |
|---|---|
| **Home** | Logo, tagline, Record Pitch, Select Video, View Sample Analysis |
| **Upload Preview** | ExoPlayer preview, duration warning past 60s, change video, Analyze |
| **Processing** | Five honest stage labels, no fabricated percentages |
| **Results** | Player, readiness heuristic, strongest moment, biggest concern, clickable timeline, moment inspector, rubric, investor questions, grounded claim |

The core interaction is on Results: tapping a timestamp on the horizontal
timeline seeks the video to that exact second and opens the inspector for that
moment, showing what the founder said, what an investor may hear, why it
matters, what is missing, and a stronger rewrite of that specific line.

---

## Build and run

Requirements: JDK 17, Android SDK 34, minSdk 26.

```bash
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Or open the project in Android Studio and press Run.

---

## Architecture

```
MainActivity          video capture / picking, screen routing
  └── PitchViewModel  UiState, request orchestration, stage advancement
        └── VcMirrorApi   OkHttp + kotlinx.serialization
              └── Cloud Run backend (all Gemini calls)
```

- `data/Models.kt` mirrors the backend contract. Every field the server may omit
  is nullable or defaulted, so an older client never crashes on a newer response.
- `data/VcMirrorApi.kt` maps transport and HTTP failures onto one actionable
  message plus a `retryable` flag the UI uses to decide whether to offer retry.
- Read timeout is 180s: analysis takes 20 to 45s, grounded verification 25 to 60s.

### Two decisions worth explaining

**The readiness heuristic is computed on-device** from the six rubric scores,
identically to the web client. It is never a model-generated score, and it is
labelled a preparation aid rather than a prediction of funding.

**Verification is a separate call.** The report renders as soon as analysis
returns, then grounding fills in. If verification fails the report stays on
screen with a retry control, rather than the whole result being lost.

---

## Video handling

Recorded pitches are written to app cache via `FileProvider`, and picked videos
are copied there so the app owns a real `File` to upload. Both are deleted once
the flow ends. Nothing is persisted beyond the session.

---

## Status

Builds clean: `./gradlew assembleDebug` produces a 19 MB debug APK with zero
warnings.

**Not yet verified on a physical device or emulator.** The API it targets is
verified working end to end from the backend side, but the UI has not been
exercised on hardware. Treat the screens as unproven until you install it.
