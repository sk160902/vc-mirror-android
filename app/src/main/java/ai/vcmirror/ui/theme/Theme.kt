package ai.vcmirror.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Editorial palette, matching the web client. Warm paper, near-black ink,
 * a single restrained rust accent. Deliberately not a gradient-heavy AI look.
 */
val Paper = Color(0xFFFAF9F6)
val PaperRaised = Color(0xFFFFFFFF)
val Ink = Color(0xFF17160F)
val InkSoft = Color(0xFF4A483D)
val InkMuted = Color(0xFF78756A)
val Line = Color(0xFFE4E1D7)
val LineStrong = Color(0xFFC9C5B6)
val Accent = Color(0xFFA3441C)
val AccentSoft = Color(0xFFF6ECE6)

val SignalHigh = Color(0xFFB03A1F)
val SignalMedium = Color(0xFFA97514)
val SignalLow = Color(0xFF3F6B4F)

private val VcColors = lightColorScheme(
    primary = Ink,
    onPrimary = Paper,
    secondary = Accent,
    onSecondary = Paper,
    background = Paper,
    onBackground = Ink,
    surface = PaperRaised,
    onSurface = Ink,
    surfaceVariant = AccentSoft,
    onSurfaceVariant = InkSoft,
    outline = Line,
    outlineVariant = LineStrong,
    error = SignalHigh,
)

/** Serif display faces give the report an editorial rather than dashboard feel. */
private val VcTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 34.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        lineHeight = 32.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 21.sp,
        lineHeight = 27.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.8.sp,
    ),
)

@Composable
fun VCMirrorTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // The editorial paper look is intentional in both system themes.
    MaterialTheme(
        colorScheme = VcColors,
        typography = VcTypography,
        content = content,
    )
}

/** Marker colour driven by whether a moment helps or hurts, then by severity. */
fun momentColor(isPositive: Boolean, severity: String): Color = when {
    isPositive -> SignalLow
    severity == "high" -> SignalHigh
    else -> SignalMedium
}
