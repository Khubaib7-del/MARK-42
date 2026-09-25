package app.selvard.ui.link

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.selvard.core.domain.link.LinkGuardian
import app.selvard.core.domain.link.LinkVerdict
import app.selvard.core.domain.link.LinkVerdictState
import app.selvard.ui.theme.SelvardTheme

/** Evidence-first link check UI: verdict, why, what was found, how certain we are. */
@Composable
fun LinkCheckScreen(
    initialUrl: String?,
    guardian: LinkGuardian,
    onAnalyzed: (url: String, verdict: LinkVerdict) -> Unit,
) {
    SelvardTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            var input by remember { mutableStateOf(initialUrl ?: "") }
            var verdict by remember { mutableStateOf<LinkVerdict?>(null) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text("Check a link", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "Paste or share a URL. Analysis runs on this device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("URL") },
                    singleLine = true,
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        val result = guardian.analyze(input)
                        verdict = result
                        onAnalyzed(input, result)
                    },
                    enabled = input.isNotBlank(),
                ) { Text("Analyze") }
                Spacer(Modifier.height(24.dp))
                verdict?.let { VerdictCard(it) }
            }
        }
    }
}

@Composable
private fun VerdictCard(verdict: LinkVerdict) {
    val (label, tone) = when (verdict.state) {
        LinkVerdictState.OPEN -> "OPEN — no known threat" to MaterialTheme.colorScheme.secondary
        LinkVerdictState.OPEN_WITH_WARNING -> "OPEN WITH WARNING — suspicious indicators" to MaterialTheme.colorScheme.secondary
        LinkVerdictState.BLOCK -> "BLOCKED — high-risk" to MaterialTheme.colorScheme.primary
        LinkVerdictState.UNKNOWN -> "UNKNOWN — limited analysis" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column {
        Text(label, style = MaterialTheme.typography.titleLarge, color = tone)
        Text(
            "Confidence: ${verdict.confidence.name.lowercase()}",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 2.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text("Why", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        verdict.reasons.forEach { reason ->
            Text(
                reason,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 3.dp),
            )
        }
        if (verdict.findings.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text("Evidence", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            verdict.findings.forEach { finding ->
                Row(Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        finding.kind,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text(finding.detail, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Source: ${verdict.findings.firstOrNull()?.provenance ?: "local heuristics only"}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
