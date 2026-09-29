package app.selvard.ui.glass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.selvard.ui.theme.SelvardTheme

/**
 * Shared "calm glass" card system for the non-technical UI refresh.
 * One rounded surface + one tonal variant, plain-language headings, and an
 * optional rounded icon. The icon is a navigation affordance only: verdict
 * and posture meaning always stays in words (AccessibilityPolicy).
 * Colors come from [SelvardTheme]; translucency is restrained (alpha 0.55 on
 * the tonal wash) so the WCAG AA pairs still hold on light surfaces.
 */
object Glass {
    val CardShape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
    val SheetShape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
    const val TONAL_WASH_ALPHA = 0.55f
    const val HERO_ICON_SIZE_DP = 56
    const val ROW_ICON_SIZE_DP = 40
}

@Composable
fun GlassHero(
    icon: ImageVector,
    iconDescription: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = Glass.CardShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = Glass.TONAL_WASH_ALPHA),
                modifier = Modifier.size(Glass.HERO_ICON_SIZE_DP.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconDescription,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(12.dp)
                        .size((Glass.HERO_ICON_SIZE_DP - 24).dp),
                )
            }
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .semantics { heading() },
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
fun GlassCard(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconDescription: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = Glass.CardShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (icon != null) {
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = Glass.TONAL_WASH_ALPHA),
                        modifier = Modifier.size(Glass.ROW_ICON_SIZE_DP.dp),
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = iconDescription,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(8.dp)
                                .size((Glass.ROW_ICON_SIZE_DP - 16).dp),
                        )
                    }
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() },
                )
            }
            Column(modifier = Modifier.padding(top = 10.dp)) { content() }
            if (actionLabel != null && onAction != null) {
                OutlinedButton(
                    onClick = onAction,
                    modifier = Modifier.padding(top = 12.dp),
                ) { Text(actionLabel) }
            }
        }
    }
}

@Composable
fun GlassStatusRow(
    icon: ImageVector,
    iconDescription: String,
    headline: String,
    detail: String,
    modifier: Modifier = Modifier,
) {
    // Tonal wash background; words carry the meaning, never the tint.
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = Glass.SheetShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = Glass.TONAL_WASH_ALPHA),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = iconDescription,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Column {
                Text(
                    headline,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

@Composable
fun GlassPrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
        ),
        modifier = modifier.fillMaxWidth(),
    ) { Text(label) }
}

@Composable
fun GlassHelperText(text: String, modifier: Modifier = Modifier, error: Boolean = false) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(top = 4.dp),
    )
}

@Composable
fun GlassSectionActions(actions: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) { actions() }
}

/** Preview-free sanity: every value used above must exist on the light theme. */
@Composable
@Suppress("unused")
private fun GlassThemeSmoke() {
    SelvardTheme {
        Text(
            "smoke",
            modifier = Modifier
                .alpha(Glass.TONAL_WASH_ALPHA)
                .semantics { contentDescription = "smoke" },
        )
    }
}
