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
    const val TOTAL_MS = 3400f
    const val REDUCED_MOTION_HOLD_MS = 400L

    val Grid = Span(0f, 700f)
    val Corners = Span(100f, 600f)
    val WireUpper = Span(200f, 900f)
    val WireLower = Span(350f, 1050f)
    val WireCore = Span(550f, 1150f)
    val Measure = Span(600f, 1200f)
    val WireFade = Span(1350f, 1750f)
    val FurnitureFade = Span(1600f, 2100f)
    val Bloom = Span(1200f, 2100f)
    val AssembleUpper = Span(1250f, 1850f)
    val AssembleLower = Span(1350f, 1950f)
    val AssembleCore = Span(1600f, 2150f)
    val Glint = Span(2050f, 2600f)
    val Ignite = Span(2100f, 2700f)
    val Tagline = Span(2450f, 2850f)
    val Principles = Span(2650f, 3050f)
    val Handoff = Span(3050f, TOTAL_MS)
    val CaptionOneIn = Span(200f, 500f)
    val CaptionOneOut = Span(1100f, 1300f)
    val CaptionTwoIn = Span(1300f, 1450f)
    val CaptionTwoOut = Span(1650f, 1800f)

    const val WORDMARK_START_MS = 2150f
    const val WORDMARK_LETTER_MS = 400f

    val Settle: Easing = FastOutSlowInEasing
    val Overshoot: Easing = CubicBezierEasing(0.2f, 1.25f, 0.4f, 1f)
}

/** Blueprint tones from the owner's launch concept; field tones from docs/brand/BRAND_IDENTITY.md §4. */
internal object LaunchPalette {
    val NavyCenter = Color(0xFF17352D)
    val NavyMid = Color(0xFF0D201D)
    val NavyEdge = Color(0xFF0C1322)
    val GridMinor = Color(0xFF234B40)
    val GridMajor = Color(0xFF427360)
    val Cyan = Color(0xFFA3C9A8)
    val OffWhite = Color(0xFFEDF1F7)
    val Slate = Color(0xFF9FB0C6)
    val DeepForest = Color(0xFF2F5D50)
    val MutedGreen = Color(0xFF5A8F6E)
    val Sage = Color(0xFFA3C9A8)
    val Cream = Color(0xFFE9F5DB)
    val SignalGreen = Color(0xFF54E454)
    val DeepInk = Color(0xFF0C1322)
}
