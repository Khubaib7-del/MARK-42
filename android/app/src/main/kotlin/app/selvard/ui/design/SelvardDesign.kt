package app.selvard.ui.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

object SelvardDesign {
    val CardShape = RoundedCornerShape(28.dp)
    val SheetShape = RoundedCornerShape(24.dp)
    val ControlShape = RoundedCornerShape(24.dp)
    val MinimumControlSize = 48.dp
}

val SelvardShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(16.dp),
    medium = SelvardDesign.ControlShape,
    large = SelvardDesign.SheetShape,
    extraLarge = SelvardDesign.CardShape,
)

/** Opaque glass endpoints keep text contrast independent of whatever is behind the panel. */
@Composable
fun SelvardSurface(
    modifier: Modifier = Modifier,
    shape: Shape = SelvardDesign.CardShape,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = shape,
        color = colors.surface,
        contentColor = colors.onSurface,
        border = BorderStroke(1.dp, Brush.linearGradient(listOf(
            colors.onSurface.copy(alpha = 0.16f), colors.outlineVariant,
            colors.onSurface.copy(alpha = 0.04f),
        ))),
        tonalElevation = 0.dp,
        shadowElevation = 3.dp,
    ) {
        Box(
            modifier = Modifier.background(
                Brush.linearGradient(listOf(colors.surfaceVariant, colors.surface)),
            ),
            content = content,
        )
    }
}
