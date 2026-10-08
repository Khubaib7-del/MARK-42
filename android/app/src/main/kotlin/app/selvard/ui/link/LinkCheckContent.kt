package app.selvard.ui.link

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.selvard.core.domain.link.LinkGuardian
import app.selvard.core.domain.link.LinkVerdict
import app.selvard.core.domain.link.LinkVerdictState
import app.selvard.core.domain.ux.StatusMapper
import app.selvard.ui.design.SelvardIcons
import app.selvard.ui.design.SelvardStatusChip
import app.selvard.ui.glass.GlassCard
import app.selvard.ui.glass.GlassHelperText
import app.selvard.ui.glass.GlassPrimaryButton
import app.selvard.ui.glass.GlassStatusRow

/** Shared by the native Links pane and the explicit Android share target. */
@Composable
fun LinkCheckContent(
    initialUrl: String?,
    guardian: LinkGuardian,
    onAnalyzed: (url: String, verdict: LinkVerdict) -> Unit,
) {
    // URL input is intentionally not saved to disk or a saved-state bundle.
    var input by remember { mutableStateOf(initialUrl ?: "") }
    var verdict by remember { mutableStateOf<LinkVerdict?>(null) }
    var error by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard(title = "The link", icon = SelvardIcons.Link) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it; verdict = null; error = false },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Paste or type a URL") },
                placeholder = { Text("https://example.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                singleLine = true,
            )
            GlassPrimaryButton(
                label = "Check this link",
                modifier = Modifier.padding(top = 16.dp),
                enabled = input.isNotBlank(),
                onClick = {
                    try {
                        val result = guardian.analyze(input.trim())
                        verdict = result
                        error = false
                        onAnalyzed(input.trim(), result)
                    } catch (_: Exception) {
                        verdict = null
                        error = true
                    }
                },
            )
            if (error) GlassHelperText("Analysis unavailable. This link has not been assessed. Try again.", error = true)
        }
        verdict?.let { VerdictCard(it) }
        GlassStatusRow(
            icon = SelvardIcons.Info,
            iconDescription = "Preview limitations",
            headline = "Limited analysis · development preview",
            detail = "Local URL indicators and bundled sample intelligence only. No live reputation, page inspection or redirect tracing.",
        )
    }
}

private fun verdictTitle(state: LinkVerdictState): String = when (state) {
    LinkVerdictState.OPEN -> "No known threat found"
    LinkVerdictState.OPEN_WITH_WARNING -> "Pause before proceeding"
    LinkVerdictState.BLOCK -> "We recommend not opening it"
    LinkVerdictState.UNKNOWN -> "Not enough evidence"
}

private fun verdictAdvice(state: LinkVerdictState): String = when (state) {
    LinkVerdictState.BLOCK -> "This check does not block a connection in another app. Close the link and verify with the sender."
    LinkVerdictState.OPEN_WITH_WARNING -> "Verify the destination with the service before signing in or downloading anything."
    else -> "An absence of known indicators is not a safety guarantee."
}

@Composable
private fun VerdictCard(verdict: LinkVerdict) {
    var evidenceExpanded by remember(verdict) { mutableStateOf(false) }
    GlassCard(title = verdictTitle(verdict.state)) {
        SelvardStatusChip(StatusMapper.forLinkVerdict(verdict.state))
        Text("Confidence: ${verdict.confidence.name.lowercase()}", style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 12.dp))
        verdict.reasons.forEach { reason ->
            Text(reason, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
        }
        Text(verdictAdvice(verdict.state), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp))
        if (verdict.findings.isNotEmpty()) {
            TextButton(onClick = { evidenceExpanded = !evidenceExpanded }, modifier = Modifier.fillMaxWidth().semantics {
                stateDescription = if (evidenceExpanded) "Expanded" else "Collapsed"
            }) {
                Text(if (evidenceExpanded) "Hide evidence" else "Inspect ${verdict.findings.size} indicators",
                    modifier = Modifier.weight(1f))
                Icon(if (evidenceExpanded) SelvardIcons.ChevronDown else SelvardIcons.ChevronRight, null)
            }
            if (evidenceExpanded) {
                verdict.findings.forEach { finding ->
                    Column(Modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(finding.kind.replace('_', ' '), style = MaterialTheme.typography.titleSmall)
                        Text(finding.detail, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Source: ${finding.provenance}", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
