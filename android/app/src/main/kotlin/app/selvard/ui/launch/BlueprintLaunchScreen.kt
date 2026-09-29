package app.selvard.ui.launch

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * BlueprintLaunchScreen:
 * A cinematic architectural blueprint animation requested by the owner.
 *
 * Sequence:
 * 1. Blueprint Grid & CAD drafting lines sketch out in technical cyan/teal.
 * 2. The Keystone / Hammer emblem is constructed with measurement markers and technical ticks.
 * 3. Energy charges at the summit keystone diamond.
 * 4. Radiant Bloom: The wireframe ignites into radiant emerald green & stillwater teal.
 * 5. Smooth scale and fade transition hands off to the main app dashboard.
 */
@Composable
fun BlueprintLaunchScreen(
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Stage 1: Blueprint drafting progress (0f -> 1f)
    val blueprintProgress = remember { Animatable(0f) }
    // Stage 2: Technical glow / spark sweep (0f -> 1f)
    val sparkProgress = remember { Animatable(0f) }
    // Stage 3: Radiant color bloom (0f -> 1f)
    val colorBloom = remember { Animatable(0f) }
    // Stage 4: Overall screen alpha for exit (1f -> 0f)
    val exitAlpha = remember { Animatable(1f) }

    // Ambient blueprint scanline pulse
    val infiniteTransition = rememberInfiniteTransition(label = "blueprint_pulse")
    val gridPulse by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "grid_alpha",
    )

    LaunchedEffect(Unit) {
        // Step 1: Draw blueprint wireframe lines (1.6s)
        blueprintProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
        )
        // Step 2: Spark travels to the keystone diamond (0.6s)
        sparkProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = LinearEasing),
        )
        // Step 3: Color bloom & ignition into full emerald glow (0.8s)
        colorBloom.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        )
        delay(600)
        // Step 4: Smooth fade out into main app
        exitAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        )
        onAnimationComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = exitAlpha.value }
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F2636),
                        Color(0xFF07131F),
                        Color(0xFF040B13),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Blueprint CAD Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBlueprintGrid(gridAlpha = gridPulse)
            drawCadTechnicalAnnotations(blueprintProgress.value)
        }

        // Center Emblem & Animation
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawKeystoneBlueprint(
                        drawProgress = blueprintProgress.value,
                        sparkProgress = sparkProgress.value,
                        colorBloom = colorBloom.value,
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Subtitle & Status
            Text(
                text = "SELVARD",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                letterSpacing = 6.sp,
                color = Color.White.copy(alpha = 0.9f * (0.3f + 0.7f * colorBloom.value)),
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (colorBloom.value > 0.5f) {
                    "SYSTEM READY // LOCAL-FIRST SHIELD ACTIVE"
                } else {
                    "INITIALIZING BLUEPRINT ARCHITECTURE…"
                },
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp,
                color = if (colorBloom.value > 0.5f) {
                    Color(0xFF54E454)
                } else {
                    Color(0xFF3E8E9E).copy(alpha = 0.8f)
                },
            )
        }
    }
}

/**
 * Draws the technical CAD blueprint coordinate grid.
 */
private fun DrawScope.drawBlueprintGrid(gridAlpha: Float) {
    val step = 32.dp.toPx()
    val gridColor = Color(0xFF143B52).copy(alpha = gridAlpha * 0.45f)
    val accentGridColor = Color(0xFF236080).copy(alpha = gridAlpha * 0.65f)

    var x = 0f
    var countX = 0
    while (x <= size.width) {
        val color = if (countX % 4 == 0) accentGridColor else gridColor
        val stroke = if (countX % 4 == 0) 1.2f else 0.6f
        drawLine(
            color = color,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = stroke,
        )
        x += step
        countX++
    }

    var y = 0f
    var countY = 0
    while (y <= size.height) {
        val color = if (countY % 4 == 0) accentGridColor else gridColor
        val stroke = if (countY % 4 == 0) 1.2f else 0.6f
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = stroke,
        )
        y += step
        countY++
    }
}

/**
 * Technical CAD crosshairs and metadata annotations.
 */
private fun DrawScope.drawCadTechnicalAnnotations(progress: Float) {
    if (progress < 0.2f) return
    val alpha = ((progress - 0.2f) / 0.8f).coerceIn(0f, 1f)
    val color = Color(0xFF3E8E9E).copy(alpha = alpha * 0.5f)

    val margin = 24.dp.toPx()
    val arm = 16.dp.toPx()
    val rightX = size.width - margin
    val bottomY = size.height - margin

    // Top-Left
    drawLine(color, Offset(margin, margin), Offset(margin + arm, margin), 1.5f)
    drawLine(color, Offset(margin, margin), Offset(margin, margin + arm), 1.5f)

    // Top-Right
    drawLine(color, Offset(rightX, margin), Offset(rightX - arm, margin), 1.5f)
    drawLine(color, Offset(rightX, margin), Offset(rightX, margin + arm), 1.5f)

    // Bottom-Left
    drawLine(color, Offset(margin, bottomY), Offset(margin + arm, bottomY), 1.5f)
    drawLine(color, Offset(margin, bottomY), Offset(margin, bottomY - arm), 1.5f)

    // Bottom-Right
    drawLine(color, Offset(rightX, bottomY), Offset(rightX - arm, bottomY), 1.5f)
    drawLine(color, Offset(rightX, bottomY), Offset(rightX, bottomY - arm), 1.5f)
}

/**
 * Renders the Keystone/Hammer mark in wireframe blueprint mode, transitioning to radiant color.
 */
private fun DrawScope.drawKeystoneBlueprint(
    drawProgress: Float,
    sparkProgress: Float,
    colorBloom: Float,
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val scale = size.minDimension / 100f

    val blueprintCyan = Color(0xFF5CD8E8)
    val radiantEmerald = Color(0xFF54E454)
    val solidInk = Color(0xFF101A2E)
    val stillwaterTeal = Color(0xFF3E8E9E)

    val strokeColor = androidx.compose.ui.graphics.lerp(blueprintCyan, solidInk, colorBloom)
    val keystoneColor = androidx.compose.ui.graphics.lerp(blueprintCyan, stillwaterTeal, colorBloom)
    val archAccentColor = androidx.compose.ui.graphics.lerp(blueprintCyan, radiantEmerald, colorBloom)

    if (colorBloom > 0.05f) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    radiantEmerald.copy(alpha = 0.35f * colorBloom),
                    stillwaterTeal.copy(alpha = 0.20f * colorBloom),
                    Color.Transparent,
                ),
                center = Offset(cx, cy),
                radius = 110.dp.toPx(),
            ),
        )
    }

    drawKeystoneGroundAndPillars(cx, cy, scale, drawProgress, colorBloom, strokeColor)
    drawKeystoneArches(cx, cy, scale, drawProgress, colorBloom, strokeColor, archAccentColor)
    drawKeystoneCoreAndDiamond(
        cx, cy, scale, drawProgress, sparkProgress, colorBloom,
        strokeColor, keystoneColor, radiantEmerald, blueprintCyan,
    )
}

private fun DrawScope.drawKeystoneGroundAndPillars(
    cx: Float,
    cy: Float,
    scale: Float,
    drawProgress: Float,
    colorBloom: Float,
    strokeColor: Color,
) {
    fun x(v: Float) = cx + (v - 48f) * scale
    fun y(v: Float) = cy + (v - 48f) * scale
    val strokeWidth = 8.5f * scale
    val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), (1f - drawProgress) * 40f)

    if (drawProgress > 0.05f) {
        val groundP = ((drawProgress - 0.05f) / 0.3f).coerceIn(0f, 1f)
        val x1 = x(12f)
        val x2 = x1 + (x(84f) - x1) * groundP
        drawLine(
            color = strokeColor,
            start = Offset(x1, y(79f)),
            end = Offset(x2, y(79f)),
            strokeWidth = 4.5f * scale,
            cap = StrokeCap.Round,
            pathEffect = if (colorBloom < 0.8f) dashedEffect else null,
        )
    }

    if (drawProgress > 0.2f) {
        val pillarP = ((drawProgress - 0.2f) / 0.35f).coerceIn(0f, 1f)
        val yBottom = y(74f)
        val yTop = yBottom + (y(54f) - yBottom) * pillarP

        drawLine(strokeColor, Offset(x(20f), yBottom), Offset(x(20f), yTop), strokeWidth, StrokeCap.Round)
        drawLine(strokeColor, Offset(x(76f), yBottom), Offset(x(76f), yTop), strokeWidth, StrokeCap.Round)
    }
}

private fun DrawScope.drawKeystoneArches(
    cx: Float,
    cy: Float,
    scale: Float,
    drawProgress: Float,
    colorBloom: Float,
    strokeColor: Color,
    archAccentColor: Color,
) {
    if (drawProgress <= 0.45f) return
    fun x(v: Float) = cx + (v - 48f) * scale
    fun y(v: Float) = cy + (v - 48f) * scale
    val strokeWidth = 8.5f * scale
    val arcP = ((drawProgress - 0.45f) / 0.4f).coerceIn(0f, 1f)
    val archRect = Rect(x(20f), y(26f), x(76f), y(82f))
    val chosenColor = if (colorBloom > 0.5f) archAccentColor else strokeColor
    val archStroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)

    val leftArc = Path().apply {
        arcTo(archRect, 180f, 65f * arcP, true)
    }
    drawPath(leftArc, chosenColor, style = archStroke)

    val rightArc = Path().apply {
        arcTo(archRect, 0f, -65f * arcP, true)
    }
    drawPath(rightArc, chosenColor, style = archStroke)
}

private fun DrawScope.drawKeystoneCoreAndDiamond(
    cx: Float,
    cy: Float,
    scale: Float,
    drawProgress: Float,
    sparkProgress: Float,
    colorBloom: Float,
    strokeColor: Color,
    keystoneColor: Color,
    radiantEmerald: Color,
    blueprintCyan: Color,
) {
    fun x(v: Float) = cx + (v - 48f) * scale
    fun y(v: Float) = cy + (v - 48f) * scale

    if (drawProgress > 0.6f) {
        val coreP = ((drawProgress - 0.6f) / 0.35f).coerceIn(0f, 1f)
        val coreSize = 12f * scale * coreP
        val coreRect = Rect(
            offset = Offset(cx - coreSize / 2f, y(62f) - coreSize / 2f),
            size = Size(coreSize, coreSize),
        )
        drawRoundRect(
            color = strokeColor,
            topLeft = coreRect.topLeft,
            size = coreRect.size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.5f * scale),
        )
    }

    if (drawProgress > 0.8f) {
        val diamondP = ((drawProgress - 0.8f) / 0.2f).coerceIn(0f, 1f)
        val diamondPath = Path().apply {
            moveTo(x(48f), y(17f))
            lineTo(x(48f + 8f * diamondP), y(25.5f))
            lineTo(x(48f), y(17f + 17f * diamondP))
            lineTo(x(48f - 8f * diamondP), y(25.5f))
            close()
        }

        drawPath(diamondPath, color = keystoneColor)
        drawPath(
            path = diamondPath,
            color = if (colorBloom > 0.3f) radiantEmerald else blueprintCyan,
            style = Stroke(width = 2.5f * scale),
        )

        if (sparkProgress > 0f && colorBloom < 0.9f) {
            val sparkRadius = (6f + 12f * sparkProgress) * scale
            drawCircle(
                color = Color.White.copy(alpha = 1f - sparkProgress),
                radius = sparkRadius,
                center = Offset(x(48f), y(25.5f)),
            )
        }
    }
}

