package app.selvard.ui.launch

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin

/** Where the logo sits on a canvas: centre and height in pixels. */
internal class EmblemFrame(val center: Offset, val height: Float) {
    val scale = height / LogoGeometry.HEIGHT
    val width = LogoGeometry.WIDTH * scale
    val topLeft = Offset(center.x - width / 2f, center.y - height / 2f)
}

/** A logo silhouette that can be drawn progressively, like a plotter pen. */
internal class WireStroke(pathData: String) {
    val path: Path = PathParser().parsePathString(pathData).toPath()
    private val measure = PathMeasure().apply { setPath(path, false) }
    private val length = measure.length
    private val segment = Path()

    fun trimmed(fraction: Float): Path {
        segment.reset()
        measure.getSegment(0f, length * fraction, segment, true)
        return segment
    }

    fun tip(fraction: Float): Offset = measure.getPosition(length * fraction)
}

internal class Wireframe {
    val upper = WireStroke(LogoGeometry.RIBBON_UPPER)
    val lower = WireStroke(LogoGeometry.RIBBON_LOWER)
    val core = WireStroke(LogoGeometry.CORE)
    val all = listOf(upper, lower, core)
}

/** Blueprint sheet, brand-field bloom and drafting overlays; everything behind the solid mark. */
internal fun DrawScope.drawLaunchBackdrop(t: Float, frame: EmblemFrame, wire: Wireframe) {
    drawRect(
        Brush.radialGradient(
            colors = listOf(LaunchPalette.NavyCenter, LaunchPalette.NavyMid, LaunchPalette.NavyEdge),
            center = frame.center,
            radius = max(size.width, size.height),
        ),
    )
    drawBlueprintGrid(t, frame.center)
    drawBloom(t, frame.center)
    val wireAlpha = 1f - t.within(LaunchTimeline.WireFade)
    if (wireAlpha > 0f) drawWireframe(t, frame, wire, wireAlpha)
    val furnitureAlpha = 1f - t.within(LaunchTimeline.FurnitureFade)
    if (furnitureAlpha > 0f) {
        drawCornerBrackets(t.within(LaunchTimeline.Corners, LaunchTimeline.Settle), furnitureAlpha)
        drawMeasureLines(t.within(LaunchTimeline.Measure, LaunchTimeline.Settle), frame, furnitureAlpha)
    }
}

private fun DrawScope.drawBlueprintGrid(t: Float, center: Offset) {
    val reveal = t.within(LaunchTimeline.Grid, LaunchTimeline.Settle)
    if (reveal <= 0f) return
    val reach = (reveal * hypot(size.width, size.height) * 0.55f).coerceAtLeast(1f)
    fun fadingBrush(color: Color) = Brush.radialGradient(
        colorStops = arrayOf(0f to color, 0.7f to color, 1f to Color.Transparent),
        center = center,
        radius = reach,
    )
    val minor = fadingBrush(LaunchPalette.GridMinor.copy(alpha = 0.55f))
    val major = fadingBrush(LaunchPalette.GridMajor.copy(alpha = 0.7f))
    val step = 32.dp.toPx()
    for (i in -ceil(center.x / step).toInt()..ceil((size.width - center.x) / step).toInt()) {
        val x = center.x + i * step
        drawLine(if (i % 4 == 0) major else minor, Offset(x, 0f), Offset(x, size.height), if (i % 4 == 0) 1.2f else 0.6f)
    }
    for (i in -ceil(center.y / step).toInt()..ceil((size.height - center.y) / step).toInt()) {
        val y = center.y + i * step
        drawLine(if (i % 4 == 0) major else minor, Offset(0f, y), Offset(size.width, y), if (i % 4 == 0) 1.2f else 0.6f)
    }
}

/** The brand field expands from the mark as a circle, with a fading shock ring at its edge. */
private fun DrawScope.drawBloom(t: Float, center: Offset) {
    val bloom = t.within(LaunchTimeline.Bloom, LaunchTimeline.Settle)
    if (bloom <= 0f) return
    val farthest = hypot(max(center.x, size.width - center.x), max(center.y, size.height - center.y))
    val radius = bloom * farthest * 1.02f
    val disc = Path().apply { addOval(Rect(center, radius)) }
    clipPath(disc) { drawBrandField(t) }
    val fade = 1f - bloom
    drawCircle(LaunchPalette.Cream.copy(alpha = 0.55f * fade), radius, center, style = Stroke(2.dp.toPx()))
    drawCircle(Color.White.copy(alpha = 0.25f * fade), radius * 0.88f, center, style = Stroke(1.dp.toPx()))
}

/** Deep forest to cream with two slowly drifting light pools, as in the README banner. */
private fun DrawScope.drawBrandField(t: Float) {
    val drift = t / 9000f * 2f * PI.toFloat()
    drawRect(
        Brush.linearGradient(
            colorStops = arrayOf(
                0f to LaunchPalette.DeepForest,
                0.3f to LaunchPalette.MutedGreen,
                0.62f to LaunchPalette.Sage,
                1f to LaunchPalette.Cream,
            ),
            start = Offset.Zero,
            end = Offset(size.width, size.height),
        ),
    )
    val light = Offset(size.width * (0.75f + 0.12f * cos(drift)), size.height * (0.32f + 0.10f * sin(drift)))
    drawRect(
        Brush.radialGradient(
            listOf(LaunchPalette.Cream.copy(alpha = 0.55f), Color.Transparent),
            center = light,
            radius = size.width * 0.9f,
        ),
    )
    val shade = Offset(size.width * (0.12f + 0.10f * sin(drift * 1.3f)), size.height * (0.14f + 0.08f * cos(drift)))
    drawRect(
        Brush.radialGradient(
            listOf(LaunchPalette.DeepForest.copy(alpha = 0.7f), Color.Transparent),
            center = shade,
            radius = size.width * 0.8f,
        ),
    )
}

private fun DrawScope.drawWireframe(t: Float, frame: EmblemFrame, wire: Wireframe, alpha: Float) {
    val stroke = Stroke(width = 1.6.dp.toPx() / frame.scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val penRadius = 14.dp.toPx() / frame.scale
    val plots = listOf(
        wire.upper to LaunchTimeline.WireUpper,
        wire.lower to LaunchTimeline.WireLower,
        wire.core to LaunchTimeline.WireCore,
    )
    withTransform({
        translate(frame.topLeft.x, frame.topLeft.y)
        scale(frame.scale, frame.scale, pivot = Offset.Zero)
    }) {
        for ((outline, span) in plots) {
            val p = t.within(span, LaunchTimeline.Settle)
            if (p <= 0f) continue
            drawPath(outline.trimmed(p), LaunchPalette.Cyan.copy(alpha = alpha), style = stroke)
            if (p < 1f) {
                val tip = outline.tip(p)
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color.White.copy(alpha = 0.9f * alpha), LaunchPalette.Cyan.copy(alpha = 0.35f * alpha), Color.Transparent),
                        center = tip,
                        radius = penRadius,
                    ),
                    radius = penRadius,
                    center = tip,
                )
            }
        }
    }
}

private fun DrawScope.drawCornerBrackets(progress: Float, alpha: Float) {
    if (progress <= 0f) return
    val color = LaunchPalette.Cyan.copy(alpha = 0.6f * alpha)
    val margin = 24.dp.toPx()
    val arm = 22.dp.toPx() * progress
    val stroke = 1.5.dp.toPx()
    for (sx in listOf(-1f, 1f)) {
        for (sy in listOf(-1f, 1f)) {
            val corner = Offset(if (sx < 0) margin else size.width - margin, if (sy < 0) margin else size.height - margin)
            drawLine(color, corner, corner.copy(x = corner.x - sx * arm), stroke)
            drawLine(color, corner, corner.copy(y = corner.y - sy * arm), stroke)
        }
    }
}

/** Dashed crosshair through the mark plus drafting-style dimension lines with end ticks. */
private fun DrawScope.drawMeasureLines(progress: Float, frame: EmblemFrame, alpha: Float) {
    if (progress <= 0f) return
    val color = LaunchPalette.Cyan.copy(alpha = 0.5f * alpha)
    val stroke = 1.dp.toPx()
    val tick = 7.dp.toPx()
    val dash = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 6.dp.toPx()))
    val c = frame.center
    val reachX = frame.width * 0.9f * progress
    val reachY = frame.height * 0.7f * progress
    drawLine(color, Offset(c.x - reachX, c.y), Offset(c.x + reachX, c.y), stroke, pathEffect = dash)
    drawLine(color, Offset(c.x, c.y - reachY), Offset(c.x, c.y + reachY), stroke, pathEffect = dash)

    val dimX = frame.topLeft.x - 22.dp.toPx()
    val halfH = frame.height / 2f * progress
    drawLine(color, Offset(dimX, c.y - halfH), Offset(dimX, c.y + halfH), stroke)
    for (y in listOf(c.y - halfH, c.y + halfH)) drawLine(color, Offset(dimX - tick, y), Offset(dimX + tick, y), stroke)

    val dimY = frame.topLeft.y + frame.height + 22.dp.toPx()
    val halfW = frame.width / 2f * progress
    drawLine(color, Offset(c.x - halfW, dimY), Offset(c.x + halfW, dimY), stroke)
    for (x in listOf(c.x - halfW, c.x + halfW)) drawLine(color, Offset(x, dimY - tick), Offset(x, dimY + tick), stroke)
}
