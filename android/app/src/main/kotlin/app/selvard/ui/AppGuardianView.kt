package app.selvard.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.selvard.SelvardApplication
import app.selvard.appguard.PackageInventoryScanner
import app.selvard.appguard.AppGuardianEventRecorder
import app.selvard.core.domain.appguard.AppAnalysis
import app.selvard.core.domain.appguard.AppRiskBand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * App Guardian inventory (Phase 5): lists installed packages by capability
 * score with evidence. Grant state is never claimed; no malware verdicts.
 */
@Composable
fun AppGuardianView() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as SelvardApplication
    val scope = rememberCoroutineScope()
    var analyses by remember { mutableStateOf<List<AppAnalysis>?>(null) }
    var selected by remember { mutableStateOf<AppAnalysis?>(null) }
    var scanning by remember { mutableStateOf(false) }
    var scanError by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "App Guardian",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            "Requested permissions are manifest facts. Grant state is not read and not claimed. " +
                "No result is a malware verdict.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )
        Button(
            onClick = {
                scanning = true
                scanError = null
                scope.launch {
                    val result = runCatching {
                        val facts = withContext(Dispatchers.IO) {
                            PackageInventoryScanner.from(context).scan()
                        }
                        facts.map { app.appGuardian.analyze(it) }
                            .sortedByDescending { it.score }
                    }
                    result
                        .onSuccess { results ->
                            // Show results first; audit recording must never
                            // block or clear them.
                            analyses = results
                            AppGuardianEventRecorder.record(app, results)
                        }
                        .onFailure { failure ->
                            scanError = "Scan failed: ${failure.message ?: "unknown error"}. " +
                                "Nothing was recorded; please retry."
                        }
                    scanning = false
                }
            },
            enabled = !scanning,
        ) { Text(if (scanning) "Scanning…" else "Scan installed apps") }
        Spacer(Modifier.height(12.dp))
        scanError?.let { error ->
            Text(
                error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        val list = analyses
        if (list == null) {
            Text(
                "Nothing scanned yet — nothing is being monitored in the background.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                "${list.size} apps analyzed — highest capability score first",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(list) { analysis ->
                    AppRow(analysis) { selected = analysis }
                }
            }
        }
    }

    selected?.let { analysis ->
        AppDetailDialog(analysis) { selected = null }
    }
}

@Composable
private fun AppRow(analysis: AppAnalysis, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.small,
        color = if (analysis.band == AppRiskBand.CRITICAL || analysis.band == AppRiskBand.HIGH) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surface
        },
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.padding(end = 8.dp)) {
                Text(analysis.packageName, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${analysis.findings.size} finding(s)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                "${analysis.band.name.lowercase()} · ${analysis.score}",
                style = MaterialTheme.typography.labelLarge,
                color = if (analysis.band == AppRiskBand.CRITICAL || analysis.band == AppRiskBand.HIGH) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun AppDetailDialog(analysis: AppAnalysis, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                Text(analysis.packageName, style = MaterialTheme.typography.titleLarge)
                Text(
                    "capability score ${analysis.score} · ${analysis.band.name.lowercase()}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                Text("Why", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                analysis.reasons.forEach { reason ->
                    Text(
                        reason,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 3.dp),
                    )
                }
                if (analysis.findings.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text("Evidence", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    analysis.findings.forEach { finding ->
                        Column(Modifier.padding(vertical = 4.dp)) {
                            Text(
                                finding.kind,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                            Text(finding.detail, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "source: ${finding.provenance}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
