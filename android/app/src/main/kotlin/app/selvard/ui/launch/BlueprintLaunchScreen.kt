package app.selvard.ui.launch

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import kotlin.coroutines.coroutineContext

private const val WORDMARK = "SELVARD"
private const val TAGLINE = "A privacy-first Android security environment"
private const val PRINCIPLES = "PROPRIETARY · LOCAL-FIRST\nEVIDENCE-BASED · NO FALSE CONFIDENCE"

/**
 * Launch sequence, played once per cold start (tap to skip):
 * 1. A blueprint sheet grids in and a plotter pen draws the current Selvard
 *    logo's outline, with drafting marks around it.
 * 2. The real logo assembles: ribbons slide in and snap together, the core
 *    spins in and locks, a light sweep crosses the finished mark.
 * 3. The blueprint blooms into the brand field while the wordmark sets.
 * With animations disabled in system settings it shows the finished frame briefly.
 */
@Composable
fun BlueprintLaunchScreen(
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var time by remember { mutableFloatStateOf(0f) }
    var skipped by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    val complete by rememberUpdatedState(onAnimationComplete)

    if (!finished) ImmersiveWhileVisible()

    LaunchedEffect(Unit) {
        if (coroutineContext[MotionDurationScale]?.scaleFactor == 0f) {
            time = LaunchTimeline.TOTAL_MS
            delay(LaunchTimeline.REDUCED_MOTION_HOLD_MS)
        } else {
            val origin = withFrameNanos { it }
            while (!skipped && time < LaunchTimeline.TOTAL_MS) {
                withFrameNanos { now ->
                    time = ((now - origin) / NANOS_PER_MILLI).coerceAtMost(LaunchTimeline.TOTAL_MS)
                }
            }
            time = LaunchTimeline.TOTAL_MS
        }
        finished = true
        complete()
    }

    LaunchScene(
        time = { time },
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClickLabel = "Skip intro",
        ) { skipped = true },
    )
}

private const val NANOS_PER_MILLI = 1_000_000f

/** Every frame is a pure function of [time] (milliseconds on the launch clock). */
@Composable
internal fun LaunchScene(time: () -> Float, modifier: Modifier = Modifier) {
    val layers = rememberEmblemLayers()
    val wire = remember { Wireframe() }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clearAndSetSemantics { contentDescription = "Selvard. $TAGLINE." },
    ) {
        val density = LocalDensity.current
        val emblemHeight = minOf(300.dp, maxHeight * 0.32f)
        val frame = with(density) {
            EmblemFrame(
                center = Offset(maxWidth.toPx() / 2f, maxHeight.toPx() * 0.38f),
                height = emblemHeight.toPx(),
            )
        }

        Canvas(Modifier.fillMaxSize()) { drawLaunchBackdrop(time(), frame, wire) }

        Canvas(Modifier.fillMaxSize()) { drawEmblem(time(), layers, frame, wire) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = with(density) { (frame.center.y + frame.height / 2f).toDp() } + 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Wordmark(time)
            Text(
                text = TAGLINE,
                fontSize = 15.sp,
                color = LaunchPalette.DeepInk.copy(alpha = 0.88f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 10.dp, start = 32.dp, end = 32.dp)
                    .riseIn(time, LaunchTimeline.Tagline.start, LaunchTimeline.Tagline.end),
            )
            Text(
                text = PRINCIPLES,
                fontSize = 10.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp,
                color = LaunchPalette.DeepInk.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 18.dp, start = 24.dp, end = 24.dp)
                    .riseIn(time, LaunchTimeline.Principles.start, LaunchTimeline.Principles.end),
            )
        }

        Caption("01 · BLUEPRINT", time) { it.pulse(LaunchTimeline.CaptionOneIn, LaunchTimeline.CaptionOneOut) }
        Caption("02 · ASSEMBLY", time) { it.pulse(LaunchTimeline.CaptionTwoIn, LaunchTimeline.CaptionTwoOut) }
    }
}

@Composable
private fun Wordmark(time: () -> Float) {
    Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.padding(start = 9.dp)) {
        WORDMARK.forEachIndexed { index, letter ->
            val start = LaunchTimeline.WORDMARK_START_MS + index * LaunchTimeline.WORDMARK_STAGGER_MS
            Text(
                text = letter.toString(),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = LaunchPalette.DeepInk,
                modifier = Modifier.riseIn(time, start, start + LaunchTimeline.WORDMARK_LETTER_MS, rise = 16.dp),
            )
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.Caption(
    text: String,
    time: () -> Float,
    visibility: (Float) -> Float,
) {
    Text(
        text = text,
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 2.sp,
        color = LaunchPalette.Cyan,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 64.dp)
            .graphicsLayer { alpha = visibility(time()) },
    )
}

/** Fades and lifts an element into place between two clock values. */
private fun Modifier.riseIn(time: () -> Float, startMs: Float, endMs: Float, rise: Dp = 10.dp) =
    graphicsLayer {
        val p = LaunchTimeline.Settle.transform(((time() - startMs) / (endMs - startMs)).coerceIn(0f, 1f))
        alpha = p
        translationY = (1f - p) * rise.toPx()
    }

/** Hides the system bars for the intro and always restores them, including at hand-off. */
@Composable
private fun ImmersiveWhileVisible() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = view.context.findActivity()?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        onDispose { controller?.show(WindowInsetsCompat.Type.systemBars()) }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
