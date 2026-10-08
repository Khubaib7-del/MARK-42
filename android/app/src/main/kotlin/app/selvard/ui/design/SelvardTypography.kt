package app.selvard.ui.design

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.selvard.R

// Explicit axes select real variable-font weights rather than synthetic bold.
@OptIn(ExperimentalTextApi::class)
val Manrope = FontFamily(
    Font(R.font.manrope_variable, weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.manrope_variable, weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.manrope_variable, weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.manrope_variable, weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)

private fun type(size: Int, lineHeight: Int, weight: FontWeight, tracking: Float = 0f) = TextStyle(
    fontFamily = Manrope,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.sp,
)

val SelvardTypography = Typography(
    displayLarge = type(40, 48, FontWeight.SemiBold, -0.8f),
    displayMedium = type(36, 44, FontWeight.SemiBold, -0.7f),
    displaySmall = type(32, 40, FontWeight.SemiBold, -0.6f),
    headlineLarge = type(30, 38, FontWeight.SemiBold, -0.6f),
    headlineMedium = type(28, 36, FontWeight.SemiBold, -0.5f),
    headlineSmall = type(24, 32, FontWeight.SemiBold, -0.4f),
    titleLarge = type(22, 30, FontWeight.SemiBold, -0.3f),
    titleMedium = type(16, 24, FontWeight.SemiBold),
    titleSmall = type(14, 20, FontWeight.SemiBold),
    bodyLarge = type(16, 24, FontWeight.Normal),
    bodyMedium = type(14, 22, FontWeight.Normal),
    bodySmall = type(12, 18, FontWeight.Normal),
    labelLarge = type(14, 20, FontWeight.SemiBold),
    labelMedium = type(12, 18, FontWeight.SemiBold),
    labelSmall = type(11, 16, FontWeight.Medium, 0.2f),
)
