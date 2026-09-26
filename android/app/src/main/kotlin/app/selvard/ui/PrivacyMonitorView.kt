package app.selvard.ui

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Privacy Monitor", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Text(
            "Observable privacy facts. Selvard records installs, permission snapshots and boots " +
                "while installed; everything it cannot see is labeled, with the reason.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )
        CoverageList()
        Spacer(Modifier.height(12.dp))
        SnapshotSection(app)
        Spacer(Modifier.height(12.dp))
        IntegritySection(context, app)
    }
}

@Composable
private fun CoverageList() {
    PrivacyCoverage.entries.forEach { entry ->
        Surface(
            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(entry.area.title, style = MaterialTheme.typography.titleSmall)
                Text(
                    stateLabel(entry.state, entry.coveredBy),
                    style = MaterialTheme.typography.labelMedium,
                    color = stateColor(entry),
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(
                    entry.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
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

    Text("Permission snapshots", style = MaterialTheme.typography.titleMedium)
    Text(
        "Compares requested permissions between two snapshots. Runtime grant state is excluded by platform design; " +
            "snapshots cover the main profile only (Private Space apps are invisible to queries).",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
    )
    Button(
        onClick = {
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
        },
        enabled = !scanning,
    ) { Text(if (scanning) "Snapshotting…" else if (previous == null) "Capture baseline" else "Compare now") }
    deltaLine?.let {
        Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
    }
    snapshotError?.let {
        Text(
            it,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun IntegritySection(context: android.content.Context, app: SelvardApplication) {
    val scope = rememberCoroutineScope()
    var integrity by remember { mutableStateOf<IntegritySnapshot?>(null) }
    var integrityError by remember { mutableStateOf<String?>(null) }

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

    Text("Device integrity", style = MaterialTheme.typography.titleMedium)
    Text(
        "Locally observed facts: published patch level and its age, verified-boot state where readable, " +
            "and boots Selvard itself recorded. Play Integrity verdicts need a Play-distributed build plus " +
            "backend verification (tracked to Phase 12) and are not claimed here.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
    )
    OutlinedButton(
        onClick = {
            scope.launch {
                val snap = runCatching { withContext(Dispatchers.IO) { source.integritySnapshot() } }
                snap.onSuccess { integrity = it }.onFailure {
                    integrityError = "Integrity read failed: ${it.message ?: "unknown error"}. Nothing inferred."
                }
            }
        },
    ) { Text("Read integrity signals") }
    integrity?.let { snap ->
        IntegrityResult(snap)
    }
    integrityError?.let {
        Text(
            it,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 8.dp),
        )
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
