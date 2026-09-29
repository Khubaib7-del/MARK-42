package app.selvard.ui.link

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Search
import app.selvard.core.domain.link.LinkGuardian
import app.selvard.core.domain.link.LinkVerdict
import app.selvard.core.domain.link.LinkVerdictState
import app.selvard.ui.glass.GlassCard
import app.selvard.ui.glass.GlassHelperText
import app.selvard.ui.glass.GlassPrimaryButton
import app.selvard.ui.glass.GlassStatusRow

/** Single evidence-first verdict implementation (share target + Security tab). */
@Composable
fun LinkCheckContent(
    initialUrl: String?,
    guardian: LinkGuardian,
    onAnalyzed: (url: String, verdict: LinkVerdict) -> Unit,
) {
    var input by remember { mutableStateOf(initialUrl ?: "") }
    var verdict by remember { mutableStateOf<LinkVerdict?>(null) }
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("URL") },
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        GlassPrimaryButton(
            label = "Analyze",
            enabled = input.isNotBlank(),
            onClick = {
                val result = guardian.analyze(input)
                verdict = result
                onAnalyzed(input, result)
            },
        )
        Spacer(Modifier.height(24.dp))
        verdict?.let { VerdictCard(it) }
    }
}

@Composable
private fun VerdictCard(verdict: LinkVerdict) {
    // Words carry the verdict; tone never contradicts them (no green-means-open).
    val label = when (verdict.state) {
        LinkVerdictState.OPEN -> "OPEN — no known threat"
        LinkVerdictState.OPEN_WITH_WARNING -> "OPEN WITH WARNING — suspicious indicators"
        LinkVerdictState.BLOCK -> "BLOCKED — high-risk"
        LinkVerdictState.UNKNOWN -> "UNKNOWN — limited analysis"
    }
    GlassCard(
        title = label,
        icon = Icons.Rounded.Search,
        iconDescription = "Verdict",
    ) {
        androidx.compose.foundation.layout.Column {
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
                    GlassStatusRow(
                        icon = Icons.Rounded.Info,
                        iconDescription = finding.kind,
                        headline = finding.kind,
                        detail = finding.detail,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
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
}
