package app.selvard.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.selvard.SelvardApplication
import app.selvard.core.domain.event.AssetType
import app.selvard.core.domain.event.EventCategory
import app.selvard.core.domain.event.EventQuery
import app.selvard.core.domain.event.SecurityEvent
import app.selvard.core.domain.risk.RiskAssessment
import app.selvard.core.domain.risk.RiskEngine
import app.selvard.core.domain.ux.AccessibilityPolicy
import app.selvard.core.domain.ux.AttentionKind
import app.selvard.core.domain.ux.AttentionPlanner
import app.selvard.core.domain.ux.EventPresentation
import app.selvard.core.domain.ux.RelativeTime
import app.selvard.core.domain.ux.StatusMapper
import app.selvard.core.domain.ux.UiStatus
import app.selvard.ui.design.SelvardIcons
import app.selvard.ui.design.SelvardStatusChip
import app.selvard.ui.design.SelvardSurface
import app.selvard.ui.launch.systemMotionScale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Home is a view of recorded evidence, never a claim of whole-device protection. */
@Composable
fun HomeView(onOpenSection: (String) -> Unit, onReplayIntro: (() -> Unit)? = null) {
    val app = LocalContext.current.applicationContext as SelvardApplication
    val scope = rememberCoroutineScope()
    val filterOn by app.networkGuardianState.running.collectAsState()
    var events by remember { mutableStateOf<List<SecurityEvent>?>(null) }
    var assessment by remember { mutableStateOf<RiskAssessment?>(null) }
    var refreshing by remember { mutableStateOf(false) }
    var loadFailed by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var readAt by remember { mutableStateOf(System.currentTimeMillis()) }

    fun refresh() {
        if (refreshing) return
        refreshing = true
        scope.launch {
            try {
                val (recorded, reading) = withContext(Dispatchers.IO) {
                    val history = app.eventStore.query(EventQuery(limit = EventQuery.MAX_LIMIT))
                    history.sortedByDescending { it.timestampMillis } to RiskEngine().assess(history)
                }
                events = recorded
                assessment = reading
                readAt = System.currentTimeMillis()
                loadFailed = false
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                assessment = null
                events = null
                loadFailed = true
            } finally {
                refreshing = false
            }
        }
    }
    LaunchedEffect(Unit) { refresh() }
    val lastScan = lastInventoryScan(events.orEmpty())
    val attention = if (events != null) AttentionPlanner.plan(filterOn, lastScan, readAt) else emptyList()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "header") {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Selvard", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("YOUR SECURITY, IN VIEW", style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.2.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (onReplayIntro != null) {
                    IconButton(onClick = onReplayIntro) { Icon(SelvardIcons.Activity, "Replay logo introduction") }
                }
                IconButton(onClick = ::refresh, enabled = !refreshing) {
                    Icon(SelvardIcons.Refresh, if (refreshing) "Reading recorded signals" else "Refresh recorded signals")
                }
            }
        }
        item(key = "posture") {
            PostureHero(assessment, events?.size, refreshing, loadFailed, expanded, { expanded = !expanded })
        }
        item(key = "check-link") {
            Surface(onClick = { onOpenSection("links") }, shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) {
                Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(SelvardIcons.Link, null, modifier = Modifier.size(28.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Before you open it", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Check a suspicious link", style = MaterialTheme.typography.bodyMedium)
                    }
                    Icon(SelvardIcons.ArrowRight, null)
                }
            }
        }
        item(key = "coverage") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeading("Your tools", "Coverage is limited in this preview")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ToolTile("Network", if (filterOn) "Sample DNS filter on" else "DNS filter off", SelvardIcons.Wifi,
                        Modifier.weight(1f), { onOpenSection("network") })
                    ToolTile("Apps", appReviewLabel(loadFailed, lastScan, readAt),
                        SelvardIcons.Apps, Modifier.weight(1f), { onOpenSection("apps") })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ToolTile("Identity", "Consent-based checks", SelvardIcons.Person,
                        Modifier.weight(1f), { onOpenSection("identity") })
                    ToolTile("Privacy", "Observable facts only", SelvardIcons.Eye,
                        Modifier.weight(1f), { onOpenSection("privacy") })
                }
            }
        }
        if (attention.isNotEmpty()) {
            item(key = "attention-heading") { SectionHeading("Worth a look", "Recommendations, not detected threats") }
            items(attention, key = { it.kind.name }) { item ->
                ActionRow(SelvardIcons.Info, item.title,
                    attentionDetail(item), onClick = { onOpenSection(attentionRoute(item.kind)) })
            }
        }
        item(key = "activity-heading") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Recent activity", style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f).semantics { heading() })
                TextButton(onClick = { onOpenSection("timeline") }) { Text("View all") }
            }
        }
        if (events.isNullOrEmpty()) {
            item(key = "activity-empty") {
                EmptyActivity(loadFailed, refreshing)
            }
        } else {
            items(events.orEmpty().take(4), key = { "activity-${it.eventId}" }) { event ->
                val line = EventPresentation.describe(event)
                ActionRow(eventIcon(event.category), line.title,
                    "${line.detail} · ${RelativeTime.format(readAt, event.timestampMillis)}",
                    onClick = { onOpenSection("timeline") })
            }
        }
        item(key = "scope") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                Icon(SelvardIcons.Lock, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Local-first preview. Links and DNS use sample intelligence. Identity lookups require separate consent.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PostureHero(
    assessment: RiskAssessment?, count: Int?, refreshing: Boolean, failed: Boolean,
    expanded: Boolean, onToggle: () -> Unit,
) {
    val duration = if (systemMotionScale(LocalContext.current) == 0f) 0 else 180
    SelvardSurface(Modifier.fillMaxWidth()) {
        Box(Modifier.background(Brush.linearGradient(listOf(Color(0xFF20382F), MaterialTheme.colorScheme.surface)))) {
            Canvas(Modifier.align(Alignment.TopEnd).size(132.dp).padding(12.dp)) {
                val contour = Color(0xFFA3C9A8).copy(alpha = 0.12f)
                listOf(0.28f, 0.40f, 0.50f).forEach { radius ->
                    drawCircle(contour, radius = size.minDimension * radius, style = Stroke(1.dp.toPx()))
                }
            }
            Column(Modifier.animateContentSize(tween(duration)).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("SECURITY POSTURE", style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.6.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(postureTitle(assessment, count, failed),
                    style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.semantics { heading() })
                if (assessment != null) {
                    SelvardStatusChip(if (count == 0) UiStatus.NOT_RUN else StatusMapper.forPosture(assessment.posture))
                }
                Text(postureDetail(count, failed), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (assessment != null && count != 0) {
                    TextButton(onClick = onToggle, contentPadding = PaddingValues(0.dp)) {
                        Text(if (expanded) "Hide the evidence" else "What this is based on")
                        Icon(if (expanded) SelvardIcons.ChevronDown else SelvardIcons.ChevronRight, null)
                    }
                    if (expanded) {
                        Text("Confidence: ${assessment.confidence.name.lowercase()}", style = MaterialTheme.typography.labelLarge)
                        assessment.reasons.forEach { reason ->
                            Text(reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("History is limited to the latest ${EventQuery.MAX_LIMIT} stored events.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (refreshing && assessment != null) Text("Refreshing…", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private fun lastInventoryScan(events: List<SecurityEvent>): Long? = events.firstOrNull {
    it.category == EventCategory.APPLICATION && it.affectedAsset?.type == AssetType.DEVICE &&
        it.affectedAsset?.ref == "app_inventory" && it.evidence.any { evidence -> evidence.kind == "scan_summary" }
}?.timestampMillis

private fun appReviewLabel(failed: Boolean, lastScan: Long?, now: Long): String = when {
    failed -> "History unavailable"
    lastScan == null -> "No review recorded"
    else -> "Reviewed ${RelativeTime.format(now, lastScan)}"
}

private fun attentionRoute(kind: AttentionKind): String = if (kind == AttentionKind.ENABLE_NETWORK_FILTER) "network" else "apps"

private fun attentionDetail(item: app.selvard.core.domain.ux.AttentionItem): String =
    if (item.kind == AttentionKind.ENABLE_NETWORK_FILTER) "Optional · sample list only, not broad protection" else item.detail

private fun postureTitle(assessment: RiskAssessment?, count: Int?, failed: Boolean): String = when {
    failed -> "Unknown"
    assessment == null -> "Reading signals"
    count == 0 -> "No signals yet"
    else -> AccessibilityPolicy.postureHeadline(assessment.posture)
}

private fun postureDetail(count: Int?, failed: Boolean): String = when {
    failed -> "The local history could not be read. There is no current assessment."
    count == null -> "Checking recorded activity on this device."
    count == 0 -> "No signals recorded yet. This is not evidence that your device is safe."
    else -> "Based on $count recorded signals, not a complete device inspection."
}

@Composable
private fun EmptyActivity(failed: Boolean, refreshing: Boolean) {
    SelvardSurface(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(if (failed) SelvardIcons.Warning else SelvardIcons.Timeline, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
            val title = when {
                failed -> "Activity unavailable"
                refreshing -> "Reading local activity…"
                else -> "A quiet starting point"
            }
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(if (failed) "Try refreshing. No assessment is available right now."
                else "Check a link or review apps to build a timeline. No events does not mean no risk.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionHeading(title: String, detail: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
        Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ToolTile(title: String, detail: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier, shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ActionRow(icon: ImageVector, title: String, detail: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(SelvardIcons.ChevronRight, null, modifier = Modifier.size(16.dp))
        }
    }
}

private fun eventIcon(category: EventCategory): ImageVector = when (category) {
    EventCategory.LINK -> SelvardIcons.Link
    EventCategory.NETWORK -> SelvardIcons.Wifi
    EventCategory.APPLICATION -> SelvardIcons.Apps
    EventCategory.IDENTITY -> SelvardIcons.Person
    EventCategory.PRIVACY -> SelvardIcons.Eye
    else -> SelvardIcons.Phone
}
