package app.selvard.ui.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.selvard.core.domain.ux.AccessibilityPolicy
import app.selvard.core.domain.ux.BrandPalette
import app.selvard.core.domain.ux.Tone
import app.selvard.core.domain.ux.UiStatus

fun Tone.color(): Color = Color(
    when (this) {
        Tone.POSITIVE -> BrandPalette.SIGNAL_GREEN
        Tone.NEUTRAL -> BrandPalette.SLATE_LIGHT
        Tone.ATTENTION -> BrandPalette.AMBER
        Tone.DANGER -> BrandPalette.CORAL
    },
)

/** Display only: callers supply a real domain status; this never infers protection. */
@Composable
fun SelvardStatusChip(status: UiStatus, modifier: Modifier = Modifier) {
    val tint = status.tone.color()
    val fill = Color(
        AccessibilityPolicy.blend(
            when (status.tone) {
                Tone.POSITIVE -> BrandPalette.SIGNAL_GREEN
                Tone.NEUTRAL -> BrandPalette.SLATE_LIGHT
                Tone.ATTENTION -> BrandPalette.AMBER
                Tone.DANGER -> BrandPalette.CORAL
            },
            BrandPalette.GLASS,
            AccessibilityPolicy.CHIP_TINT_ALPHA,
        ),
    )
    val icon = when (status) {
        UiStatus.PROTECTED, UiStatus.NO_KNOWN_THREAT -> SelvardIcons.Check
        UiStatus.BLOCKED, UiStatus.DO_NOT_OPEN -> SelvardIcons.Blocked
        UiStatus.UNKNOWN, UiStatus.NOT_RUN -> SelvardIcons.Unknown
        UiStatus.NOT_MONITORED -> SelvardIcons.ShieldOff
        UiStatus.PERMISSION_REQUIRED, UiStatus.SUSPICIOUS,
        UiStatus.ATTENTION, UiStatus.HIGH_RISK -> SelvardIcons.Warning
        UiStatus.OBSERVED, UiStatus.OS_LIMITATION -> SelvardIcons.Info
    }
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(50),
        color = fill,
        contentColor = tint,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.24f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(status.label, style = MaterialTheme.typography.labelMedium)
        }
    }
}
