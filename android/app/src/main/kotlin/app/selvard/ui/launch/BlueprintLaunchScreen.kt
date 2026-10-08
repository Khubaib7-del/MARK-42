package app.selvard.ui.launch

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlin.coroutines.coroutineContext

private const val WORDMARK = "SELVARD"
private const val TAGLINE = "Security, with the evidence in view."
private const val PRINCIPLES = "LOCAL-FIRST · NO FALSE CONFIDENCE"

/**
 * First-run brand sequence, also available through explicit replay (tap to skip):
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
    val context = LocalContext.current
    val lifecycle = (context.findActivity() as? LifecycleOwner)?.lifecycle
    val systemScale = systemMotionScale(context)
    var time by rememberSaveable {
        mutableFloatStateOf(if (systemScale == 0f) LaunchTimeline.TOTAL_MS else 0f)
    }
    var finished by rememberSaveable { mutableStateOf(false) }
    val complete by rememberUpdatedState(onAnimationComplete)
    val finish = {
        if (!finished) {
            time = LaunchTimeline.TOTAL_MS
            finished = true
            complete()
        }
    }

    BackHandler(enabled = !finished) { finish() }

    LaunchedEffect(finished, lifecycle, systemScale) {
        if (finished) return@LaunchedEffect
        val owner = lifecycle ?: return@LaunchedEffect
        // Stop the frame clock when hidden; resume from the current frame on return.
        owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            val scale = if (systemScale == 0f) 0f else {
                coroutineContext[MotionDurationScale]?.scaleFactor ?: systemScale
            }
            if (scale == 0f) {
                time = LaunchTimeline.TOTAL_MS
                delay(LaunchTimeline.REDUCED_MOTION_HOLD_MS)
            } else {
                val startTime = time
                val origin = withFrameNanos { it }
                while (time < LaunchTimeline.TOTAL_MS) {
                    withFrameNanos { now ->
                        time = (startTime + (now - origin) / NANOS_PER_MILLI / scale)
                            .coerceAtMost(LaunchTimeline.TOTAL_MS)
                    }
                }
            }
            finish()
        }
    }

    Box(modifier.fillMaxSize().systemBarsPadding()) {
        LaunchScene(
            time = { time },
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = "Skip intro",
            ) { finish() },
        )
        TextButton(
            onClick = { finish() },
            enabled = !finished,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .systemBarsPadding()
                .padding(bottom = 16.dp),
        ) {
            Text("Skip intro", color = LaunchPalette.OffWhite)
        }
    }
}

private const val NANOS_PER_MILLI = 1_000_000f

/** Also used for the stage handoff, which must not animate when system motion is off. */
internal fun systemMotionScale(context: Context): Float = try {
    Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        .takeIf { it.isFinite() && it >= 0f } ?: 1f
} catch (_: SecurityException) {
    1f
}

/** Every frame is a pure function of [time] (milliseconds on the launch clock). */
@Composable
internal fun LaunchScene(time: () -> Float, modifier: Modifier = Modifier) {
    val layers = rememberEmblemLayers()
    val wire = remember { Wireframe() }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .semantics(mergeDescendants = true) { contentDescription = "Selvard. $TAGLINE." },
    ) {
        val density = LocalDensity.current
        val compact = maxHeight < 480.dp
        val emblemHeight = minOf(300.dp, maxHeight * if (compact) 0.28f else 0.32f)
        val frame = with(density) {
            EmblemFrame(
                center = Offset(maxWidth.toPx() / 2f, maxHeight.toPx() * if (compact) 0.28f else 0.38f),
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
            Wordmark(time, compact)
            Text(
                text = TAGLINE,
                fontSize = 15.sp,
                color = LaunchPalette.OffWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 10.dp, start = 32.dp, end = 32.dp)
                    .riseIn(time, LaunchTimeline.Tagline.start, LaunchTimeline.Tagline.end),
            )
            if (!compact) Text(
                text = PRINCIPLES,
                fontSize = 10.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp,
                color = LaunchPalette.Slate,
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
private fun Wordmark(time: () -> Float, compact: Boolean) {
    Text(
        text = WORDMARK,
        fontSize = if (compact) 28.sp else 32.sp,
        letterSpacing = 4.sp,
        fontWeight = FontWeight.Bold,
        color = LaunchPalette.OffWhite,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 24.dp)
            .riseIn(time, LaunchTimeline.WORDMARK_START_MS,
                LaunchTimeline.WORDMARK_START_MS + LaunchTimeline.WORDMARK_LETTER_MS, rise = 12.dp),
    )
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
            .systemBarsPadding()
            .padding(bottom = 88.dp)
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

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
