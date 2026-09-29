package app.selvard.ui.launch

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import app.selvard.R
import kotlin.math.roundToInt

/** The owner's logo split into three layers on one shared canvas (scripts/brand/build_launch_assets.py). */
internal class EmblemLayers(resources: Resources) {
    val upper: ImageBitmap = ImageBitmap.imageResource(resources, R.drawable.logo_ribbon_upper)
    val lower: ImageBitmap = ImageBitmap.imageResource(resources, R.drawable.logo_ribbon_lower)
    val core: ImageBitmap = ImageBitmap.imageResource(resources, R.drawable.logo_core)
}

@Composable
internal fun rememberEmblemLayers(): EmblemLayers {
    val resources = LocalContext.current.resources
    return remember(resources) { EmblemLayers(resources) }
}

private const val RIBBON_TILT_DEGREES = 14f
private const val CORE_SPIN_DEGREES = 60f

/**
 * Assembles the real logo: the two ribbons slide in from opposite corners and
 * snap into place, the core spins in and lands, then a light sweep crosses the
 * finished mark. Once every stage completes the layers sit at identity, so the
 * final frame is the unmodified logo.
 */
internal fun DrawScope.drawEmblem(t: Float, layers: EmblemLayers, frame: EmblemFrame, wire: Wireframe) {
    val ribbonSize = frame.width * 0.5f to frame.height * 0.35f
    drawLayer(
        layers.upper, frame, t.within(LaunchTimeline.AssembleUpper),
        from = Offset(-ribbonSize.first, -ribbonSize.second), tilt = -RIBBON_TILT_DEGREES,
    )
    drawLayer(
        layers.lower, frame, t.within(LaunchTimeline.AssembleLower),
        from = Offset(ribbonSize.first, ribbonSize.second), tilt = RIBBON_TILT_DEGREES,
    )
    drawCore(layers.core, frame, t.within(LaunchTimeline.AssembleCore))
    drawGlint(frame, wire, t.within(LaunchTimeline.Glint, LaunchTimeline.Settle))
    drawIgnition(frame, t.within(LaunchTimeline.Ignite, LaunchTimeline.Settle))
}

private fun DrawScope.drawLayer(image: ImageBitmap, frame: EmblemFrame, progress: Float, from: Offset, tilt: Float) {
    if (progress <= 0f) return
    val travel = 1f - LaunchTimeline.Overshoot.transform(progress)
    withTransform({
        translate(from.x * travel, from.y * travel)
        rotate(tilt * travel, pivot = frame.center)
    }) {
        drawFrameImage(image, frame, alpha = (progress / 0.45f).coerceIn(0f, 1f))
    }
}

private fun DrawScope.drawCore(image: ImageBitmap, frame: EmblemFrame, progress: Float) {
    if (progress <= 0f) return
    val landed = LaunchTimeline.Overshoot.transform(progress)
    val pivot = Offset(
        frame.topLeft.x + LogoGeometry.CORE_CENTER_X * frame.scale,
        frame.topLeft.y + LogoGeometry.CORE_CENTER_Y * frame.scale,
    )
    withTransform({
        (0.25f + 0.75f * landed).let { scale(it, it, pivot = pivot) }
        rotate(-CORE_SPIN_DEGREES * (1f - landed), pivot = pivot)
    }) {
        drawFrameImage(image, frame, alpha = (progress / 0.35f).coerceIn(0f, 1f))
    }
}

private fun DrawScope.drawFrameImage(image: ImageBitmap, frame: EmblemFrame, alpha: Float) {
    drawImage(
        image = image,
        dstOffset = IntOffset(frame.topLeft.x.roundToInt(), frame.topLeft.y.roundToInt()),
        dstSize = IntSize(frame.width.roundToInt(), frame.height.roundToInt()),
        alpha = alpha,
        filterQuality = FilterQuality.High,
    )
}

/** Diagonal light band, clipped to the logo silhouettes so it only crosses logo pixels. */
private fun DrawScope.drawGlint(frame: EmblemFrame, wire: Wireframe, progress: Float) {
    if (progress <= 0f || progress >= 1f) return
    val band = LogoGeometry.WIDTH * 0.9f
    val first = -band
    val last = LogoGeometry.WIDTH + LogoGeometry.HEIGHT
    val s = first + (last - first) * progress
    val brush = Brush.linearGradient(
        colorStops = arrayOf(0f to Color.Transparent, 0.5f to Color.White.copy(alpha = 0.5f), 1f to Color.Transparent),
        start = Offset(s / 2f, s / 2f),
        end = Offset((s + band) / 2f, (s + band) / 2f),
    )
    withTransform({
        translate(frame.topLeft.x, frame.topLeft.y)
        scale(frame.scale, frame.scale, pivot = Offset.Zero)
    }) {
        for (outline in wire.all) {
            clipPath(outline.path) { drawRect(brush) }
        }
    }
}

/** A single signal-green ring leaves the core as it locks in, then disappears. */
private fun DrawScope.drawIgnition(frame: EmblemFrame, progress: Float) {
    if (progress <= 0f || progress >= 1f) return
    val center = Offset(
        frame.topLeft.x + LogoGeometry.CORE_CENTER_X * frame.scale,
        frame.topLeft.y + LogoGeometry.CORE_CENTER_Y * frame.scale,
    )
    drawCircle(
        color = LaunchPalette.SignalGreen.copy(alpha = 0.6f * (1f - progress)),
        radius = frame.width * (0.15f + 0.6f * progress),
        center = center,
        style = Stroke(3.dp.toPx()),
    )
}
