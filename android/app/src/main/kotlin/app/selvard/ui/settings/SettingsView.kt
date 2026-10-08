package app.selvard.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import app.selvard.SelvardApplication
import app.selvard.core.domain.FoundationStatus
import app.selvard.core.domain.event.EventQuery
import app.selvard.core.domain.store.LocalDataDeletion
import app.selvard.ui.design.SelvardIcons
import app.selvard.ui.glass.GlassCard
import app.selvard.ui.glass.GlassHelperText
import app.selvard.ui.glass.GlassHero
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Honest engine lifecycle, local-data diagnostics and confirmed deletion with a receipt. */
@Composable
fun SettingsView() {
    val app = LocalContext.current.applicationContext as SelvardApplication
    val scope = rememberCoroutineScope()
    // Keep the platform tap-jacking guard on the hosting Compose view.
    val hostView = LocalView.current
    SideEffect { hostView.filterTouchesWhenObscured = true }
    var receipt by remember { mutableStateOf<String?>(null) }
    var deleting by remember { mutableStateOf(false) }
    var confirmingDelete by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    var eventCount by remember { mutableStateOf<Int?>(null) }
    var counting by remember { mutableStateOf(false) }
    var diagError by remember { mutableStateOf<String?>(null) }


    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GlassHero(
            icon = SelvardIcons.Settings,
            iconDescription = "Settings",
            title = "Settings",
            subtitle = "Local data and engine status. You're in control.",
        )
        GlassCard(title = "Recorded data", icon = SelvardIcons.Database) {
            GlassHelperText("Events and declared addresses are stored encrypted on this device.")
            OutlinedButton(
                enabled = !counting && !deleting,
                modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                onClick = {
                    counting = true
                    diagError = null
                    scope.launch {
                        runCatching {
                            withContext(Dispatchers.IO) {
                                app.eventStore.query(EventQuery(limit = 10_000)).size
                            }
                        }.onSuccess { eventCount = it }.onFailure {
                            if (it is CancellationException || it !is Exception) throw it
                            diagError = "Recorded data could not be read. Try again."
                        }
                        counting = false
                    }
                },
            ) {
                Icon(SelvardIcons.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(if (counting) "Counting…" else "Count recorded events", modifier = Modifier.padding(start = 8.dp))
            }
            eventCount?.let {
                GlassHelperText(eventCountLabel(it))
            }
            diagError?.let { GlassHelperText(it, error = true) }
        }
        EngineStatusSection()
        RuntimeDiagnosticsSection()
        GlassCard(title = "Delete local data", icon = SelvardIcons.Trash) {
            GlassHelperText("Permanently remove every recorded event and declared identity. This cannot be undone.")
            OutlinedButton(
                onClick = { confirmingDelete = true },
                enabled = !deleting && !counting,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text(if (deleting) "Deleting…" else "Delete all recorded data")
            }
            deleteError?.let { GlassHelperText(it, error = true) }
        }
        receipt?.let {
            GlassCard(title = "Deletion receipt", icon = SelvardIcons.Check) {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            icon = { Icon(SelvardIcons.Trash, contentDescription = null) },
            title = { Text("Delete all recorded data?") },
            text = {
                // Dialogs have a separate window and Compose host view.
                val dialogView = LocalView.current
                SideEffect { dialogView.filterTouchesWhenObscured = true }
                Text("All recorded events and every declared identity will be removed from this device. " +
                    "This cannot be undone. New events may still be recorded afterward.")
            },
            dismissButton = {
                TextButton(onClick = { confirmingDelete = false }) { Text("Cancel") }
            },
            confirmButton = {
                TextButton(
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        confirmingDelete = false
                        deleting = true
                        deleteError = null
                        receipt = null
                        scope.launch {
                            try {
                                val outcome = withContext(Dispatchers.IO) {
                                    LocalDataDeletion.run({ app.eventStore.purgeAll() }, { app.identityVault.deleteAll() })
                                }
                                // A background collector may already have added new events.
                                if (outcome.deletedEvents != null) eventCount = null
                                receipt = outcome.receipt()
                                if (!outcome.complete) {
                                    deleteError = "Some local data could not be removed. See the receipt and retry."
                                }
                            } finally {
                                deleting = false
                            }
                        }
                    },
                ) { Text("Delete permanently") }
            },
        )
    }
}

private fun eventCountLabel(count: Int): String =
    if (count >= 10_000) "At least 10,000 recorded events (query limit)." else "$count recorded event(s)."

@Composable
private fun EngineStatusSection() {
    var expanded by remember { mutableStateOf(false) }
    GlassCard(title = "Engine status", icon = SelvardIcons.Activity) {
        GlassHelperText("Implementation status, not a guarantee of protection.")
        TextButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth().semantics {
                stateDescription = if (expanded) "Expanded" else "Collapsed"
            },
        ) {
            Text(if (expanded) "Hide engines" else "View ${FoundationStatus.engines.size} engines",
                modifier = Modifier.weight(1f))
            Icon(if (expanded) SelvardIcons.ChevronDown else SelvardIcons.ChevronRight,
                contentDescription = null)
        }
        if (expanded) {
            FoundationStatus.engines.forEachIndexed { index, status ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(status.engine.displayName, style = MaterialTheme.typography.titleSmall)
                        Text(FoundationStatus.statusLine(status), style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
