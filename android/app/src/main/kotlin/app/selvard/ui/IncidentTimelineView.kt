package app.selvard.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.selvard.SelvardApplication
import app.selvard.core.domain.event.EventQuery
import app.selvard.core.domain.event.SecurityEvent
import app.selvard.core.domain.incident.CausalLanguage
import app.selvard.core.domain.incident.CorrelationEngine
import app.selvard.core.domain.incident.CorrelationResult
import app.selvard.core.domain.incident.Incident
import app.selvard.core.domain.ux.StatusMapper
import app.selvard.core.domain.ux.UiStatus
import app.selvard.ui.design.SelvardIcons
import app.selvard.ui.design.SelvardStatusChip
import app.selvard.ui.design.SelvardSurface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Recorded signals grouped by entity and time only; singles remain independently visible. */
@Composable
fun IncidentTimelineView() {
    val context = LocalContext.current
    val app = context.applicationContext as SelvardApplication
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf<CorrelationResult?>(null) }
    var eventsById by remember { mutableStateOf<Map<String, SecurityEvent>>(emptyMap()) }
    var analyzing by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var selected by remember { mutableStateOf<Incident?>(null) }
    var selectedSingle by remember { mutableStateOf<String?>(null) }

    suspend fun loadTimeline() {
        analyzing = true
        loadError = null
        try {
            runCatching {
                val (events, correlated) = withContext(Dispatchers.IO) {
                    val recorded = app.eventStore.query(EventQuery(limit = EventQuery.MAX_LIMIT))
                    recorded to CorrelationEngine.correlate(recorded)
                }
                eventsById = events.associateBy { it.eventId }
                result = correlated
            }.onFailure { failure ->
                if (failure is CancellationException || failure !is Exception) throw failure
                loadError = failure.message ?: "Recorded events could not be read."
            }
        } finally {
            analyzing = false
        }
    }

    LaunchedEffect(app) { loadTimeline() }
    val refresh: () -> Unit = {
        if (!analyzing) {
            // Set this before launching to prevent duplicate refreshes from rapid taps.
            analyzing = true
            scope.launch { loadTimeline() }
        }
    }
    val current = result
    val singles = remember(current, eventsById) {
        current?.uncorrelatedEventIds.orEmpty().sortedByDescending { eventsById[it]?.timestampMillis ?: 0L }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "header") {
            TimelineHeader(analyzing, refresh)
        }
        item(key = "context") {
            TimelineSummary(current, singles.size, eventsById.size, analyzing)
        }
        loadError?.let { error ->
            item(key = "error") {
                TimelineLoadError(error, hasResults = current != null, analyzing = analyzing, onRetry = refresh)
            }
        }
        timelineSignalItems(
            current = current,
            singles = singles,
            eventsById = eventsById,
            analyzing = analyzing,
            onSelectGroup = { selected = it },
            onSelectSingle = { selectedSingle = it },
        )
    }

    selected?.let { incident ->
        TimelineEvidenceSheet(
            title = "Temporal group",
            detail = incident.summaryLine(),
            eventIds = incident.eventIds,
            eventsById = eventsById,
            onDismiss = { selected = null },
        )
    }
    selectedSingle?.let { id ->
        TimelineEvidenceSheet(
            title = "Single signal",
            detail = temporalCopy("No temporal match in this set of recorded events."),
            eventIds = listOf(id),
            eventsById = eventsById,
            onDismiss = { selectedSingle = null },
        )
    }
}

@Composable
private fun TimelineHeader(analyzing: Boolean, onRefresh: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(SelvardIcons.Timeline, null, tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f)) {
            Text("Timeline", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Recorded signals, in context",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onRefresh, enabled = !analyzing) {
            if (analyzing) {
                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Icon(SelvardIcons.Refresh, "Refresh timeline")
            }
        }
    }
}

@Composable
private fun TimelineSummary(current: CorrelationResult?, singleCount: Int, eventCount: Int, analyzing: Boolean) {
    SelvardSurface(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SelvardStatusChip(if (current == null) UiStatus.NOT_RUN else UiStatus.OBSERVED)
            Text(
                if (current == null) "Recorded activity" else "${current.incidents.size} groups · $singleCount single signals",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                temporalCopy(
                    "Same entity, consecutive gaps within 30 minutes. " +
                        "Temporal proximity only; no causal relationship is claimed.",
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (eventCount == EventQuery.MAX_LIMIT) {
                Text(
                    "Showing up to ${EventQuery.MAX_LIMIT} recorded events, not the full history.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (analyzing && current != null) {
                Text("Refreshing · previous results remain visible", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun TimelineLoadError(error: String, hasResults: Boolean, analyzing: Boolean, onRetry: () -> Unit) {
    SelvardSurface(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(SelvardIcons.Warning, null, tint = MaterialTheme.colorScheme.error)
                Text("Timeline unavailable", style = MaterialTheme.typography.titleMedium)
            }
            Text(error, style = MaterialTheme.typography.bodyMedium)
            if (hasResults) {
                Text(
                    "Previous results are still shown below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onRetry, enabled = !analyzing) { Text("Try again") }
        }
    }
}

private fun LazyListScope.timelineSignalItems(
    current: CorrelationResult?,
    singles: List<String>,
    eventsById: Map<String, SecurityEvent>,
    analyzing: Boolean,
    onSelectGroup: (Incident) -> Unit,
    onSelectSingle: (String) -> Unit,
) {
    if (current == null && analyzing) {
        item(key = "loading") {
            TimelineState("Loading recorded events", "Reading the event store and grouping nearby signals.")
        }
    }
    if (current != null && current.incidents.isEmpty() && singles.isEmpty()) {
        item(key = "empty") {
            TimelineState(
                "No recorded events",
                "Signals appear here after they are recorded. An empty timeline is not evidence that the device is safe.",
            )
        }
    }
    if (current != null && current.incidents.isNotEmpty()) {
        item(key = "groups-header") {
            Text("Temporal groups", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
        }
        items(current.incidents, key = { "group-${it.incidentId}" }) { incident ->
            IncidentCard(incident) { onSelectGroup(incident) }
        }
    }
    if (singles.isNotEmpty()) {
        item(key = "singles-header") {
            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Single signals", style = MaterialTheme.typography.titleMedium)
                Text(
                    temporalCopy("No temporal match · listed separately"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(singles, key = { "single-$it" }) { id ->
            SingleSignalRow(eventsById[id]) { onSelectSingle(id) }
        }
    }
}

@Composable
private fun TimelineState(title: String, detail: String) {
    Column(Modifier.padding(horizontal = 4.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun IncidentCard(incident: Incident, onClick: () -> Unit) {
    SelvardSurface(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClickLabel = "View ordered signals", onClick = onClick).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(SelvardIcons.Clock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "${incident.eventCount} signals · ${timelineLabel(incident.maxSeverity.name)} severity",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(incident.entityKey, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    "${formatTime(incident.startedAtMillis)} – ${formatTime(incident.endedAtMillis)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(SelvardIcons.ChevronRight, null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SingleSignalRow(event: SecurityEvent?, onClick: () -> Unit) {
    SelvardSurface(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClickLabel = "View signal evidence", onClick = onClick).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(SelvardIcons.Activity, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    event?.let { "${timelineLabel(it.category.name)} signal · ${timelineLabel(it.severity.name)}" } ?: "Recorded signal",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    event?.let { formatTime(it.timestampMillis) } ?: "Details unavailable",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                event?.affectedAsset?.let { asset ->
                    Text(asset.ref, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            Icon(SelvardIcons.ChevronRight, null, modifier = Modifier.size(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimelineEvidenceSheet(
    title: String,
    detail: String,
    eventIds: List<String>,
    eventsById: Map<String, SecurityEvent>,
    onDismiss: () -> Unit,
) {
    val maxHeight = LocalConfiguration.current.screenHeightDp.dp * 0.85f
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().heightIn(max = maxHeight)
                .verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(SelvardIcons.Close, "Close signal evidence") }
            }
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Recorded evidence · ${eventIds.size} signals", style = MaterialTheme.typography.titleMedium)
            eventIds.forEachIndexed { index, id ->
                val event = eventsById[id]
                var expanded by remember(id) { mutableStateOf(eventIds.size == 1) }
                SelvardSurface(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        TextButton(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "${index + 1}. ${event?.let { timelineLabel(it.category.name) } ?: "Recorded signal"}",
                                modifier = Modifier.weight(1f),
                            )
                            Icon(if (expanded) SelvardIcons.ChevronDown else SelvardIcons.ChevronRight, null)
                        }
                        event?.let {
                            Text(formatTime(it.timestampMillis), style = MaterialTheme.typography.bodySmall)
                        }
                        if (expanded) {
                            if (event == null) {
                                Text("Details unavailable for event $id", style = MaterialTheme.typography.bodyMedium)
                            } else {
                                SelvardStatusChip(StatusMapper.forEvent(event))
                                Text(
                                    "Source: ${event.source}\n" +
                                        "Severity: ${timelineLabel(event.severity.name)} · " +
                                        "confidence: ${timelineLabel(event.confidence.name)}\n" +
                                        "Action: ${timelineLabel(event.actionTaken.name)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                event.affectedAsset?.let { asset ->
                                    Text("${timelineLabel(asset.type.name)}: ${asset.ref}", style = MaterialTheme.typography.bodyMedium)
                                }
                                if (event.evidence.isEmpty()) {
                                    Text("No evidence items recorded.", style = MaterialTheme.typography.bodyMedium)
                                }
                                event.evidence.forEach { evidence ->
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(timelineLabel(evidence.kind), style = MaterialTheme.typography.labelLarge)
                                        Text(evidence.value, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            "Source: ${evidence.provenance}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                Text(
                                    "Event ID: $id",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Lint only authored correlation copy, not factual asset references or stored evidence values.
private fun temporalCopy(text: String): String = text.also(CausalLanguage::check)

private fun timelineLabel(value: String): String =
    value.lowercase().replace('_', ' ').replaceFirstChar { it.titlecase() }

private fun formatTime(millis: Long): String =
    SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault()).format(Date(millis))
