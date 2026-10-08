package app.selvard.ui

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import app.selvard.ui.design.SelvardIcons
import app.selvard.ui.design.SelvardSurface
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.selvard.SelvardApplication
import app.selvard.core.domain.event.EventCategory
import app.selvard.core.domain.event.EventQuery
import app.selvard.core.domain.integrity.DeviceIntegritySource
import app.selvard.core.domain.integrity.IntegritySnapshot
import app.selvard.core.domain.integrity.PlayIntegrityState
import app.selvard.core.domain.privacy.PermissionSnapshot
import app.selvard.core.domain.privacy.PrivacyCoverage
import app.selvard.core.domain.privacy.diffSnapshots

import app.selvard.integrity.AndroidIntegritySource
import app.selvard.privacy.PrivacyEventRecorder
import app.selvard.ui.glass.GlassCard
import app.selvard.ui.glass.GlassHelperText
import app.selvard.ui.glass.GlassHero
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Phase 6 Privacy Monitor + Device Integrity (observed facts only).
 * Every area renders its coverage state 1:1; unmonitored areas say why.
 * Boot explanations are never offered; patch age is a fact, not a verdict.
 */
@Composable
fun PrivacyMonitorView() {
    val context = LocalContext.current
    val app = context.applicationContext as SelvardApplication
    Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
        GlassHero(
            icon = SelvardIcons.Eye,
            iconDescription = "Privacy facts",
            title = "Privacy Monitor",
            subtitle = "Observed facts, not a protection verdict.",
        )
        SnapshotSection(app)
        IntegritySection(context, app)
        Text("Visibility & limits", style = MaterialTheme.typography.titleMedium)
        CoverageList()
    }
}

@Composable
private fun CoverageList() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PrivacyCoverage.entries.forEach { entry ->
            var expanded by remember(entry.area) { mutableStateOf(false) }
            SelvardSurface(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .semantics { stateDescription = if (expanded) "Expanded" else "Collapsed" }
                            .clickable(role = Role.Button, onClickLabel = "Show or hide coverage explanation") {
                                expanded = !expanded
                            }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(SelvardIcons.Info, contentDescription = null, modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.area.title, style = MaterialTheme.typography.titleSmall)
                            Text(stateLabel(entry.state, entry.coveredBy),
                                style = MaterialTheme.typography.bodySmall, color = stateColor(entry))
                        }
                        Icon(if (expanded) SelvardIcons.ChevronDown else SelvardIcons.ChevronRight,
                            contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                    if (expanded) {
                        Text(entry.note, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SnapshotSection(app: SelvardApplication) {
    val scope = rememberCoroutineScope()
    var previous by remember { mutableStateOf<PermissionSnapshot?>(null) }
    var deltaLine by remember { mutableStateOf<String?>(null) }
    var scanning by remember { mutableStateOf(false) }
    var snapshotError by remember { mutableStateOf<String?>(null) }

    GlassCard(
        title = "Permission snapshots",
        actionLabel = if (scanning) "Snapshotting…" else if (previous == null) "Capture baseline" else "Compare now",
        onAction = {
            if (!scanning) {
                scanning = true
                snapshotError = null
                scope.launch {
                    // Snapshots are computed off the main thread; failures render, never crash.
                    val result = runCatching {
                        withContext(Dispatchers.IO) { PrivacyEventRecorder.currentSnapshot(app) }
                    }
                    result
                        .onSuccess { current ->
                            // Render first; recording is best-effort.
                            deltaLine = if (previous == null) {
                                "${current.entries.size} package(s) captured as baseline; next scan compares against it."
                            } else {
                                diffSnapshots(previous, current).summaryLine()
                            }
                            val diff = previous?.let { diffSnapshots(it, current) }
                            previous = current
                            if (diff != null) PrivacyEventRecorder.recordSnapshotDiff(app, diff)
                        }
                        .onFailure { failure ->
                            snapshotError = "Snapshot failed: ${failure.message ?: "unknown error"}. Nothing recorded."
                        }
                    scanning = false
                }
            }
        },
    ) {
        Text(
            "Requested permissions · main profile only. Runtime grants are not visible.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        PrivacyExplanation("Snapshot limits", "Compares manifest permissions between two snapshots, not runtime grants. " +
            "Private Space apps are invisible to queries. The baseline lasts while this screen is open.")
        if (previous == null && !scanning && snapshotError == null) {
            GlassHelperText("No baseline captured yet.")
        }
        deltaLine?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
        }
        snapshotError?.let { GlassHelperText(it, error = true) }
    }
}

@Composable
private fun IntegritySection(context: android.content.Context, app: SelvardApplication) {
    val scope = rememberCoroutineScope()
    var integrity by remember { mutableStateOf<IntegritySnapshot?>(null) }
    var integrityError by remember { mutableStateOf<String?>(null) }
    var reading by remember { mutableStateOf(false) }

    val source = remember {
        AndroidIntegritySource(
            context,
            bootCount7d = AndroidIntegritySource.bootCounter(
                query = { cutoff: Long ->
                    val events = app.eventStore.query(
                        EventQuery(categories = setOf(EventCategory.DEVICE), sinceMillis = cutoff, limit = 500),
                    )
                    events.filter { it.source == "device_integrity" }.map {
                        app.cachedBoot(it.timestampMillis)
                    }
                },
            ),
        ) as DeviceIntegritySource
    }

    GlassCard(
        title = "Device integrity",
        icon = SelvardIcons.Phone,
        actionLabel = if (reading) "Reading…" else "Read device facts",
        onAction = {
            if (!reading) scope.launch {
                reading = true
                integrityError = null
                val snap = runCatching { withContext(Dispatchers.IO) { source.integritySnapshot() } }
                snap.onSuccess { integrity = it }.onFailure {
                    integrityError = "Integrity read failed: ${it.message ?: "unknown error"}. Nothing inferred."
                }
                reading = false
            }
        },
    ) {
        Text(
            "Patch level, readable boot state and recorded boots. No Play Integrity verdict.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        PrivacyExplanation("What these facts mean", "Patch age is not a vulnerability verdict. Boot records never explain why " +
            "a restart happened. Play Integrity needs a Play-distributed build and backend verification; it is not claimed here.")
        if (integrity == null && !reading && integrityError == null) GlassHelperText("No device facts read yet.")
        integrity?.let { snap ->
            IntegrityResult(snap)
        }
        integrityError?.let { GlassHelperText(it, error = true) }
    }
}

@Composable
private fun IntegrityResult(snap: IntegritySnapshot) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        snap.reasons.forEach { line ->
            Text("— $line", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
        }
        if (snap.playIntegrity == PlayIntegrityState.NOT_SUPPORTED) {
            Text(
                "Play Integrity verdicts: not claimed (see note above).",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Text(
            "This device reports patch ${snap.patchLevel ?: "unreadable"}; " +
                "OS build ${Build.VERSION.RELEASE ?: "unknown"}. Age is a fact, not a verdict.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun PrivacyExplanation(title: String, explanation: String) {
    var expanded by remember { mutableStateOf(false) }
    TextButton(
        onClick = { expanded = !expanded },
        modifier = Modifier.semantics { stateDescription = if (expanded) "Expanded" else "Collapsed" },
    ) {
        Text(title, modifier = Modifier.weight(1f))
        Icon(if (expanded) SelvardIcons.ChevronDown else SelvardIcons.ChevronRight, contentDescription = null)
    }
    if (expanded) GlassHelperText(explanation)
}

@Composable
private fun stateColor(entry: app.selvard.core.domain.privacy.CoverageEntry) =
    if (entry.state == app.selvard.core.domain.DisclosureState.DETECTED) {
        // Words ("Observed…") carry the state; ink keeps AA on light surfaces.
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

private fun stateLabel(
    state: app.selvard.core.domain.DisclosureState,
    coveredBy: String?,
): String = when (state) {
    app.selvard.core.domain.DisclosureState.DETECTED -> "Observed${coveredBy?.let { " · $it" } ?: ""}"
    app.selvard.core.domain.DisclosureState.NOT_MONITORED -> "Not monitored"
    app.selvard.core.domain.DisclosureState.OS_LIMITATION -> "OS limitation"
    else -> state.name
}
