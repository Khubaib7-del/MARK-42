package app.selvard.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.selvard.SelvardApplication
import app.selvard.appguard.AppGuardianEventRecorder
import app.selvard.appguard.PackageInventoryScanner
import app.selvard.core.domain.appguard.AppAnalysis
import app.selvard.core.domain.ux.UiStatus
import app.selvard.ui.design.SelvardIcons
import app.selvard.ui.design.SelvardStatusChip
import app.selvard.ui.design.SelvardSurface
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Deliberate package inventory: capability evidence, never a malware or grant-state verdict. */
@Composable
fun AppGuardianView() {
    val context = LocalContext.current
    val app = context.applicationContext as SelvardApplication
    val scope = rememberCoroutineScope()
    var analyses by remember { mutableStateOf<List<AppAnalysis>?>(null) }
    var selected by remember { mutableStateOf<AppAnalysis?>(null) }
    var scanning by remember { mutableStateOf(false) }
    var scanError by remember { mutableStateOf<String?>(null) }

    val scan: () -> Unit = {
        if (!scanning) {
            scanning = true
            scanError = null
            scope.launch {
                try {
                    runCatching {
                        val results = withContext(Dispatchers.Default) {
                            val facts = withContext(Dispatchers.IO) { PackageInventoryScanner.from(context).scan() }
                            facts.map { app.appGuardian.analyze(it) }.sortedByDescending { it.score }
                        }
                        // Results remain available independently of best-effort audit persistence.
                        analyses = results
                        AppGuardianEventRecorder.record(app, results)
                    }.onFailure { failure ->
                        if (failure is CancellationException || failure !is Exception) throw failure
                        scanError = failure.message ?: "The package inventory could not be read."
                    }
                } finally {
                    scanning = false
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "header") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(SelvardIcons.Apps, null, tint = MaterialTheme.colorScheme.primary)
                Column {
                    Text("Apps", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Understand requested capabilities",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item(key = "scan") {
            AppScanCard(hasResults = analyses != null, scanning = scanning, onScan = scan)
        }
        scanError?.let { error ->
            item(key = "error") {
                AppScanError(error, hasResults = analyses != null, scanning = scanning, onRetry = scan)
            }
        }
        appInventoryItems(analyses, scanning, scanError) { selected = it }
    }

    selected?.let { analysis ->
        AppDetailDialog(analysis) { selected = null }
    }
}

@Composable
private fun AppScanCard(hasResults: Boolean, scanning: Boolean, onScan: () -> Unit) {
    SelvardSurface(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SelvardStatusChip(if (hasResults) UiStatus.OBSERVED else UiStatus.NOT_RUN)
            Text("App inventory", style = MaterialTheme.typography.titleLarge)
            Text(
                "On-demand scan. Requested permissions are not permission grants or a malware verdict.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onScan, enabled = !scanning, modifier = Modifier.fillMaxWidth()) {
                if (scanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Icon(SelvardIcons.Search, null, modifier = Modifier.size(18.dp))
                }
                val label = when {
                    scanning -> "Scanning apps…"
                    hasResults -> "Scan again"
                    else -> "Scan installed apps"
                }
                Text(label, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun AppScanError(error: String, hasResults: Boolean, scanning: Boolean, onRetry: () -> Unit) {
    SelvardSurface(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(SelvardIcons.Warning, null, tint = MaterialTheme.colorScheme.error)
                Text("Scan unavailable", style = MaterialTheme.typography.titleMedium)
            }
            Text(error, style = MaterialTheme.typography.bodyMedium)
            Text(
                if (hasResults) "Previous results are still shown below." else "No scan result is available.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onRetry, enabled = !scanning) { Text("Retry scan") }
        }
    }
}

private fun LazyListScope.appInventoryItems(
    analyses: List<AppAnalysis>?,
    scanning: Boolean,
    scanError: String?,
    onSelect: (AppAnalysis) -> Unit,
) {
    when {
        analyses == null && !scanning && scanError == null -> item(key = "not-scanned") {
            AppInventoryState("Ready when you are", "No apps scanned yet. Nothing is monitored in the background.")
        }
        analyses == null && scanning -> item(key = "loading") {
            AppInventoryState("Reading package inventory", "Results will appear when analysis finishes.")
        }
        analyses != null && analyses.isEmpty() -> item(key = "empty") {
            AppInventoryState("No apps returned", "The package inventory returned no apps. This is not evidence of safety.")
        }
    }
    if (!analyses.isNullOrEmpty()) {
        item(key = "results-header") {
            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${analyses.size} apps analyzed", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (scanning) {
                        "Previous scan · highest capability score first"
                    } else {
                        "Highest capability score first · tap for evidence"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(analyses, key = { it.packageName }) { analysis ->
            AppRow(analysis) { onSelect(analysis) }
        }
    }
}

@Composable
private fun AppInventoryState(title: String, detail: String) {
    Column(Modifier.padding(horizontal = 4.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AppRow(analysis: AppAnalysis, onClick: () -> Unit) {
    SelvardSurface(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClickLabel = "View app evidence", onClick = onClick).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(SelvardIcons.Apps, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    analysis.packageName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${analysis.band.name.lowercase().replaceFirstChar { it.titlecase() }} capability · score ${analysis.score}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text("${analysis.findings.size} findings", style = MaterialTheme.typography.labelMedium)
            }
            Icon(SelvardIcons.ChevronRight, null, modifier = Modifier.size(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppDetailDialog(analysis: AppAnalysis, onDismiss: () -> Unit) {
    val maxHeight = LocalConfiguration.current.screenHeightDp.dp * 0.85f
    var showReasons by remember(analysis) { mutableStateOf(false) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().heightIn(max = maxHeight)
                .verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("App evidence", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(SelvardIcons.Close, "Close app evidence") }
            }
            Text(analysis.packageName, style = MaterialTheme.typography.titleMedium)
            Text(
                "${analysis.band.name.lowercase().replaceFirstChar { it.titlecase() }} capability · score ${analysis.score}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Requested permissions only; grant state is not read. This is not a malware verdict.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SelvardSurface(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showReasons = !showReasons }, modifier = Modifier.fillMaxWidth()) {
                        Text("Analysis notes", modifier = Modifier.weight(1f))
                        Icon(if (showReasons) SelvardIcons.ChevronDown else SelvardIcons.ChevronRight, null)
                    }
                    if (showReasons) {
                        analysis.reasons.forEach { reason ->
                            Text(reason, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            Text("Findings · ${analysis.findings.size}", style = MaterialTheme.typography.titleMedium)
            if (analysis.findings.isEmpty()) {
                Text(
                    "No findings in this analysis. Permission absence is not evidence of safety.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            analysis.findings.forEach { finding ->
                var expanded by remember(analysis, finding) { mutableStateOf(false) }
                SelvardSurface(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                finding.kind.replace('_', ' ').replaceFirstChar { it.titlecase() },
                                modifier = Modifier.weight(1f),
                            )
                            Icon(if (expanded) SelvardIcons.ChevronDown else SelvardIcons.ChevronRight, null)
                        }
                        if (expanded) {
                            Text(finding.detail, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Source: ${finding.provenance} · weight ${finding.weight}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
