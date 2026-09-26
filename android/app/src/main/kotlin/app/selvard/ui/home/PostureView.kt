package app.selvard.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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

/** Posture detail for the Security tab: words plus the evidence it rests on. */
@Composable
fun PostureView() {
    val context = LocalContext.current
    val app = context.applicationContext as SelvardApplication
    val scope = rememberCoroutineScope()
    var lines by remember { mutableStateOf<List<String>?>(null) }
    var assessing by remember { mutableStateOf(false) }
    var assessError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            "Security posture",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            "A deterministic reading of recorded events — the same signals the " +
                "timeline groups. Posture is stated in words, with the reasons " +
                "that produced it.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )
        Button(
            onClick = {
                assessing = true
                assessError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            app.eventStore.query(EventQuery(limit = EventQuery.MAX_LIMIT))
                        }
                    }.onSuccess { events ->
                        val assessment = RiskEngine().assess(events)
                        lines = listOf(AccessibilityPolicy.postureLabel(assessment.posture)) +
                            assessment.reasons
                    }.onFailure {
                        assessError = "Posture unavailable: ${it.message ?: "unknown error"}"
                    }
                    assessing = false
                }
            },
            enabled = !assessing,
        ) { Text(if (assessing) "Assessing…" else "Assess recorded events") }
        Spacer(Modifier.height(12.dp))
        assessError?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }
        lines?.forEach { line ->
            Text(
                "— $line",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(vertical = 3.dp)
                    .semantics { contentDescription = "Posture reason: $line" },
            )
        } ?: Text(
            "Nothing assessed yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
