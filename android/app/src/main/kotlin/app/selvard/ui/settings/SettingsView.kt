package app.selvard.ui.settings

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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.selvard.SelvardApplication
import app.selvard.core.domain.FoundationStatus
import app.selvard.core.domain.event.EventQuery
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
    var receipt by remember { mutableStateOf<String?>(null) }
    var deleting by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    var eventCount by remember { mutableStateOf<Int?>(null) }
    var diagError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            "What is actually running",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            "Honest status of every engine. Nothing is listed as active until it is built.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
        FoundationStatus.engines.forEach { status ->
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Text(status.engine.displayName, style = MaterialTheme.typography.titleMedium)
                Text(
                    FoundationStatus.statusLine(status),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Your recorded data",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            "Events live encrypted on this device. Deleting wipes the event store " +
                "and every declared identity, then reports exactly what was removed.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
        OutlinedButton(
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
        diagError?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(8.dp))
        Button(
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
                        deleteError = "Deletion failed: ${it.message ?: "unknown error"}. Nothing was confirmed removed."
                    }
                    deleting = false
                }
            },
            enabled = !deleting,
        ) { Text(if (deleting) "Deleting…" else "Delete all recorded data") }
        deleteError?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        receipt?.let {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp))
            }
        }
    }
}
