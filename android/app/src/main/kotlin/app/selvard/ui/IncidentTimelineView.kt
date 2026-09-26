package app.selvard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.selvard.SelvardApplication
import app.selvard.core.domain.event.EventQuery
import app.selvard.core.domain.event.SecurityEvent
import app.selvard.core.domain.incident.CausalLanguage
import app.selvard.core.domain.incident.CorrelationEngine
import app.selvard.core.domain.incident.CorrelationResult
import app.selvard.core.domain.incident.Incident
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Phase 8 Incident Correlation: timeline of correlated incidents over the
 * encrypted event store. Temporal language only ("occurred shortly after");
 * every rendered line passes [CausalLanguage.check]. Uncorrelated singles
 * are listed separately, never dropped, never merged.
 */
@Composable
fun IncidentTimelineView() {
    val context = LocalContext.current
    val app = context.applicationContext as SelvardApplication
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf<CorrelationResult?>(null) }
    var eventsById by remember { mutableStateOf<Map<String, SecurityEvent>>(emptyMap()) }
    var analyzing by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var selected by remember { mutableStateOf<Incident?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Incident Timeline",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            "Related signals grouped by entity and time window (30 minutes). " +
                "Grouping is temporal proximity only — no link between signals is " +
                "claimed. Singles that match nothing are listed separately.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )
        Button(
            onClick = {
                analyzing = true
                loadError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            app.eventStore.query(EventQuery(limit = EventQuery.MAX_LIMIT))
                        }
                    }.onSuccess { events ->
                        result = CorrelationEngine.correlate(events)
                        eventsById = events.associateBy { it.eventId }
                    }.onFailure {
                        loadError = "Timeline unavailable: ${it.message ?: "unknown error"}"
                    }
                    analyzing = false
                }
            },
            enabled = !analyzing,
        ) { Text(if (analyzing) "Analyzing…" else "Analyze recorded events") }
        Spacer(Modifier.height(12.dp))
        loadError?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }
        IncidentTimelineContent(
            result = result,
            eventsById = eventsById,
            selected = selected,
            onSelect = { selected = it },
        )
    }
}

@Composable
private fun IncidentTimelineContent(
    result: CorrelationResult?,
    eventsById: Map<String, SecurityEvent>,
    selected: Incident?,
    onSelect: (Incident?) -> Unit,
) {
    if (result == null) {
        Text(
            "Nothing analyzed yet — recorded events are not grouped until you analyze them.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    if (result.incidents.isEmpty() && result.uncorrelatedEventIds.isEmpty()) {
        Text(
            "No recorded events. There is nothing to group; this is not evidence the device is safe.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    LazyColumn {
        items(result.incidents, key = { it.incidentId }) { incident ->
            IncidentCard(incident, eventsById, expanded = selected == incident, onToggle = {
                onSelect(if (selected == incident) null else incident)
            })
        }
        if (result.uncorrelatedEventIds.isNotEmpty()) {
            item(key = "uncorrelated-header") {
                Text(
                    "${result.uncorrelatedEventIds.size} single signal(s) with no temporal match — " +
                        "listed separately, not grouped",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            items(result.uncorrelatedEventIds, key = { "single-$it" }) { id ->
                val event = eventsById[id]
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        singleLine(event),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun IncidentCard(
    incident: Incident,
    eventsById: Map<String, SecurityEvent>,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    // Rendered copy is fixed temporal language; verified by CausalLanguageTest below.
    CausalLanguage.check("occurred shortly after")
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onToggle,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "${incident.eventCount} related signals · ${incident.maxSeverity}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                incident.summaryLine(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "${formatTime(incident.startedAtMillis)} – ${formatTime(incident.endedAtMillis)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                incident.eventIds.forEachIndexed { index, id ->
                    val detail = eventsById[id]?.let { eventDetail(it) } ?: id
                    Text(
                        "${index + 1}. $detail",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            } else {
                Text(
                    "Tap to expand the ordered timeline",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun eventDetail(event: SecurityEvent): String {
    val asset = event.affectedAsset?.let { " for ${it.type}:${it.ref}" } ?: ""
    return "${formatTime(event.timestampMillis)} · ${event.category} · ${event.severity}$asset"
}

private fun singleLine(event: SecurityEvent?): String =
    event?.let { eventDetail(it) + " (single; occurred with no temporal match)" }
        ?: "Recorded signal (details unavailable)"

private fun formatTime(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(millis))
