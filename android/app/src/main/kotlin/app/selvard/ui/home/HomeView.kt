package app.selvard.ui.home

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.selvard.SelvardApplication
import app.selvard.core.domain.event.EventQuery
import app.selvard.core.domain.risk.RiskEngine
import app.selvard.core.domain.ux.AccessibilityPolicy
import app.selvard.ui.glass.GlassCard
import app.selvard.ui.glass.GlassHelperText
import app.selvard.ui.glass.GlassHero
import app.selvard.ui.glass.GlassPrimaryButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * HOME: posture headline in words (never color alone), what ran, honesty
 * limits. Refreshes the posture from the encrypted store on demand —
 * the same deterministic engine as the posture screen, not a second one.
 */
@Composable
fun HomeView(onOpenSection: (String) -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as SelvardApplication
    val scope = rememberCoroutineScope()
    var postureLine by remember { mutableStateOf<String?>(null) }
    var refreshing by remember { mutableStateOf(false) }
    var refreshError by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        refreshing = true
        refreshError = null
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    app.eventStore.query(EventQuery(limit = EventQuery.MAX_LIMIT))
                }
            }.onSuccess { events ->
                val assessment = RiskEngine().assess(events)
                val words = AccessibilityPolicy.postureLabel(assessment.posture)
                postureLine = "$words — over ${events.size} recorded event(s). " +
                    "Absence of signals is not evidence of safety."
            }.onFailure {
                refreshError = "Posture unavailable: ${it.message ?: "unknown error"}"
            }
            refreshing = false
        }
    }

    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        GlassHero(
            icon = Icons.Rounded.Shield,
            iconDescription = "Selvard shield mark",
            title = "Selvard",
            subtitle = "A privacy-first security environment. Analysis runs on this device; " +
                "nothing here means safe — only what was actually observed.",
        )
        Spacer(Modifier.height(12.dp))
        GlassCard(
            title = "Current posture",
            icon = Icons.Rounded.Home,
            iconDescription = "Posture status",
            actionLabel = if (refreshing) "Reading…" else "Refresh posture",
            onAction = { if (!refreshing) refresh() },
        ) {
            Text(
                postureLine ?: "Not assessed yet — refresh reads recorded events only.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.semantics { contentDescription = "Current security posture in words" },
            )
            refreshError?.let { GlassHelperText(it, error = true) }
        }
        Spacer(Modifier.height(12.dp))
        GlassCard(
            title = "What Selvard cannot do — stated honestly",
        ) {
            Text(
                "It cannot read your messages. It cannot see inside encrypted " +
                    "traffic — and never will. It cannot intercept links you tap; " +
                    "share a link to Selvard to have it checked.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(12.dp))
        SectionLinks(onOpenSection)
    }
}

@Composable
private fun SectionLinks(onOpenSection: (String) -> Unit) {
    val links = listOf(
        Triple("security", "Check a suspicious link", Icons.Rounded.Shield as ImageVector),
        Triple("network", "Network filter status", Icons.Rounded.Wifi as ImageVector),
        Triple("apps", "App inventory", Icons.Rounded.Apps as ImageVector),
        Triple("identity", "Identity exposure", Icons.Rounded.Person as ImageVector),
        Triple("timeline", "Incident timeline", Icons.Rounded.History as ImageVector),
    )
    links.forEach { (route, label, icon) ->
        GlassCard(
            title = label,
            icon = icon,
            iconDescription = label,
            modifier = Modifier.padding(vertical = 3.dp),
        ) {
            GlassPrimaryButton(label = "Open", onClick = { onOpenSection(route) })
        }
    }
}
