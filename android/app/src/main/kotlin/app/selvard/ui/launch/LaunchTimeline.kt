package app.selvard.ui.launch

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.graphics.Color

/** A time window on the launch clock, in milliseconds. */
internal class Span(val start: Float, val end: Float)

/** 0..1 progress of this clock value inside [span], shaped by [easing]. */
internal fun Float.within(span: Span, easing: Easing = LinearEasing): Float =
    easing.transform(((this - span.start) / (span.end - span.start)).coerceIn(0f, 1f))

/** Fades in over [inSpan] and back out over [outSpan]. */
internal fun Float.pulse(inSpan: Span, outSpan: Span): Float = within(inSpan) * (1f - within(outSpan))

/**
 * One master clock drives every stage, so any frame is a pure function of
 * time: draft the blueprint, assemble the mark, bloom into the brand field,
 * then set the wordmark. The mark always lands on the exact logo pixels.
 */
internal object LaunchTimeline {
    const val TOTAL_MS = 4900f
    const val REDUCED_MOTION_HOLD_MS = 800L

    val Grid = Span(0f, 1100f)
    val Corners = Span(150f, 850f)
    val WireUpper = Span(300f, 1300f)
    val WireLower = Span(500f, 1500f)
    val WireCore = Span(800f, 1600f)
    val Measure = Span(900f, 1700f)
    val WireFade = Span(1950f, 2500f)
    val FurnitureFade = Span(2300f, 3000f)
    val Bloom = Span(1750f, 3000f)
    val AssembleUpper = Span(1800f, 2650f)
    val AssembleLower = Span(1900f, 2750f)
    val AssembleCore = Span(2350f, 3050f)
    val Glint = Span(2950f, 3650f)
    val Ignite = Span(3000f, 3800f)
    val Tagline = Span(3600f, 4100f)
    val Principles = Span(3850f, 4350f)
    val CaptionOneIn = Span(300f, 700f)
    val CaptionOneOut = Span(1600f, 1850f)
    val CaptionTwoIn = Span(1850f, 2050f)
    val CaptionTwoOut = Span(2350f, 2550f)

    const val WORDMARK_START_MS = 3000f
    const val WORDMARK_STAGGER_MS = 80f
    const val WORDMARK_LETTER_MS = 420f

    val Settle: Easing = FastOutSlowInEasing
    val Overshoot: Easing = CubicBezierEasing(0.2f, 1.25f, 0.4f, 1f)
}

/** Blueprint tones from the owner's launch concept; field tones from docs/brand/BRAND_IDENTITY.md §4. */
internal object LaunchPalette {
    val NavyCenter = Color(0xFF0F2636)
    val NavyMid = Color(0xFF07131F)
    val NavyEdge = Color(0xFF040B13)
    val GridMinor = Color(0xFF143B52)
    val GridMajor = Color(0xFF236080)
    val Cyan = Color(0xFF5CD8E8)
    val DeepForest = Color(0xFF2F5D50)
    val MutedGreen = Color(0xFF5A8F6E)
    val Sage = Color(0xFFA3C9A8)
    val Cream = Color(0xFFE9F5DB)
    val SignalGreen = Color(0xFF54E454)
    val DeepInk = Color(0xFF0C1322)
}
