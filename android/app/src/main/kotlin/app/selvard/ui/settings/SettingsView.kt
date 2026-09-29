package app.selvard.ui.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import app.selvard.SelvardApplication
import app.selvard.core.domain.FoundationStatus
import app.selvard.core.domain.event.EventQuery
import app.selvard.ui.glass.GlassCard
import app.selvard.ui.glass.GlassHelperText
import app.selvard.ui.glass.GlassHero
import app.selvard.ui.glass.GlassPrimaryButton
import app.selvard.ui.glass.GlassSectionActions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * SETTINGS: engine status (honest lifecycle lines), recorded-data deletion
 * with explicit receipt, diagnostics (event counts). USER_FLOWS F7–F8.
 */
@Composable
fun SettingsView() {
    val context = LocalContext.current
    val app = context.applicationContext as SelvardApplication
    val scope = rememberCoroutineScope()
    // Tap-jacking guard: Compose 1.7 has no filterTouchesWhenObscured, so the
    // platform View flag is set on the hosting view (covers this screen's
    // destructive delete action against taps through overlay windows).
    val hostView = LocalView.current
    androidx.compose.runtime.SideEffect { hostView.filterTouchesWhenObscured = true }
    var receipt by remember { mutableStateOf<String?>(null) }
    var deleting by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    var eventCount by remember { mutableStateOf<Int?>(null) }
    var diagError by remember { mutableStateOf<String?>(null) }

    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        GlassHero(
            icon = Icons.Rounded.Settings,
            iconDescription = "Settings",
            title = "Settings",
            subtitle = "What is on, your recorded data, and how to delete it.",
        )
        Spacer(Modifier.height(12.dp))
        GlassCard(title = "Engines") {
            FoundationStatus.engines.forEach { status ->
                Text(
                    status.engine.displayName,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    FoundationStatus.statusLine(status),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        GlassCard(
            title = "Your recorded data",
            icon = Icons.Rounded.Delete,
            iconDescription = "Recorded data",
        ) {
            Text(
                "Events live encrypted on this device. Deleting wipes the event store " +
                    "and every declared identity, then reports exactly what was removed.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            GlassSectionActions {
                androidx.compose.material3.OutlinedButton(
                    onClick = {
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    app.eventStore.query(
                                        EventQuery(limit = 10_000),
                                    ).size
                                }
                            }.onSuccess { eventCount = it }.onFailure {
                                diagError = "Count unavailable: ${it.message ?: "unknown error"}"
                            }
                        }
                    },
                ) { Text(eventCount?.let { "$it recorded event(s)" } ?: "Count recorded events") }
            }
            diagError?.let { GlassHelperText(it, error = true) }
            Spacer(Modifier.height(8.dp))
            GlassPrimaryButton(
                label = if (deleting) "Deleting…" else "Delete all recorded data",
                enabled = !deleting,
                onClick = {
                    deleting = true
                    deleteError = null
                    scope.launch {
                        runCatching {
                            withContext(Dispatchers.IO) {
                                val events = app.eventStore.purgeAll()
                                app.identityVault.deleteAll()
                                events
                            }
                        }.onSuccess { events ->
                            eventCount = 0
                            receipt = "Deleted $events recorded event(s) and all declared identities. " +
                                "The vault file and event store are now empty."
                        }.onFailure {
                            deleteError = "Deletion failed: ${it.message ?: "unknown error"}. " +
                                "Nothing was confirmed removed."
                        }
                        deleting = false
                    }
                },
            )
            deleteError?.let { GlassHelperText(it, error = true) }
            receipt?.let {
                GlassCard(title = "Receipt") { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }
}
