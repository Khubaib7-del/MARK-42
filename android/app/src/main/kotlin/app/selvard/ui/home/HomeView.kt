package app.selvard.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.selvard.SelvardApplication
import app.selvard.core.domain.event.EventQuery
import app.selvard.core.domain.risk.RiskEngine
import app.selvard.core.domain.ux.AccessibilityPolicy
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            "Selvard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            "A privacy-first security environment. Analysis runs on this device; " +
                "nothing here means safe — only what was actually observed.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )
        Text(
            "Current posture",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            postureLine ?: "Not assessed yet — refresh reads recorded events only.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(top = 4.dp)
                .semantics { contentDescription = "Current security posture in words" },
        )
        refreshError?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = ::refresh, enabled = !refreshing) {
            Text(if (refreshing) "Reading…" else "Refresh posture")
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "What Selvard cannot do — stated honestly",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            "It cannot read your messages. It cannot see inside encrypted " +
                "traffic — and never will. It cannot intercept links you tap; " +
                "share a link to Selvard to have it checked.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )
        SectionLinks(onOpenSection)
    }
}

@Composable
private fun SectionLinks(onOpenSection: (String) -> Unit) {
    val links = listOf(
        "security" to "Check a suspicious link",
        "network" to "Network filter status",
        "apps" to "App inventory",
        "identity" to "Identity exposure",
        "timeline" to "Incident timeline",
    )
    links.forEach { (route, label) ->
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            onClick = { onOpenSection(route) },
        ) {
            Text(
                label,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(12.dp),
            )
        }
    }
}
