package app.selvard.ui

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
import androidx.compose.material3.Switch
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.selvard.core.domain.FoundationStatus
import app.selvard.core.domain.OnboardingContent
import app.selvard.core.domain.OnboardingPage
import app.selvard.network.NetworkGuardianState
import app.selvard.network.SelvardVpnService
import app.selvard.ui.theme.SelvardTheme
import androidx.compose.runtime.collectAsState

@Composable
fun SelvardApp() {
    SelvardTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            var page by remember { mutableIntStateOf(0) }
            val lastPage = OnboardingContent.pages.size + 5 // consent, app guardian, privacy, identity, timeline, status
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 24.dp),
            ) {
                FoundationBanner(modifier = Modifier.padding(top = 16.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (page < OnboardingContent.pages.size) {
                        OnboardingPageView(page = OnboardingContent.pages[page])
                    } else if (page == OnboardingContent.pages.size) {
                        NetworkGuardianConsentView()
                    } else if (page == OnboardingContent.pages.size + 1) {
                        AppGuardianView()
                    } else if (page == OnboardingContent.pages.size + 2) {
                        PrivacyMonitorView()
                    } else if (page == OnboardingContent.pages.size + 3) {
                        IdentityExposureView()
                    } else if (page == OnboardingContent.pages.size + 4) {
                        IncidentTimelineView()
                    } else {
                        FoundationStatusView()
                    }
                }
                NavigationRow(
                    page = page,
                    lastPage = lastPage,
                    onBack = { if (page > 0) page-- },
                    onNext = { if (page < lastPage) page++ },
                )
            }
        }
    }
}

@Composable
private fun FoundationBanner(modifier: Modifier = Modifier) {
    // (existing implementation unchanged; appended composables below)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = "SELVARD PREVIEW — Link, Network (opt-in DNS filter), App, and Privacy " +
                "Monitor (installs, permission snapshots, boots, integrity facts), and Identity " +
                "Exposure (consented HIBP checks), and the Incident Timeline (temporal " +
                "grouping only — never causal) are active. All planned engines are now built.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Composable
private fun OnboardingPageView(page: OnboardingPage) {
    Column {
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(20.dp))
        page.points.forEach { point ->
            Row(modifier = Modifier.padding(vertical = 6.dp)) {
                Text(
                    text = "—",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(end = 10.dp),
                )
                Text(
                    text = point,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

@Composable
private fun FoundationStatusView() {
    Column {
        Text(
            text = "What is actually running",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Honest status of every planned engine. Phases follow the roadmap " +
                "(docs/ROADMAP.md). Nothing is listed as active until it is built.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        FoundationStatus.engines.forEach { status ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                Text(
                    text = status.engine.displayName,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = FoundationStatus.statusLine(status),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun NavigationRow(
    page: Int,
    lastPage: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (page > 0) {
            TextButton(onClick = onBack) { Text("Back") }
        } else {
            Spacer(modifier = Modifier.height(1.dp))
        }
        if (page < lastPage) {
            Button(onClick = onNext) { Text("Continue") }
        } else {
            OutlinedButton(onClick = {}) { Text("Done for now") }
        }
    }
}

/**
 * Prominent disclosure + consent (Play VpnService policy; USER_FLOWS F3).
 * In-app, in the normal usage flow, separate from other disclosures, and
 * requiring affirmative action before the tunnel can be enabled.
 */
@Composable
private fun NetworkGuardianConsentView() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as app.selvard.SelvardApplication
    val running by app.networkGuardianState.running.collectAsState()
    Column {
        Text(
            text = "Network Guardian",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "What this does",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "When enabled, Selvard sets up a local VPN tunnel that filters " +
                "domain-name lookups (DNS) on this device. Lookups for known malware " +
                "and phishing destinations are refused. Allowed lookups are forwarded, " +
                "unmodified, to your device's own configured resolver.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "What this does NOT do",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Selvard does not route your traffic to any server — filtering happens " +
                "on this device only. Only DNS lookups pass through Selvard; all other " +
                "traffic bypasses it entirely and is never seen by this app. Selvard " +
                "never inspects the content of your connections. Records of blocked " +
                "destinations stay on this device.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Development sample data: filtering currently uses a bundled sample " +
                "list, not real threat intelligence.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Switch(
                checked = running,
                onCheckedChange = { enabled ->
                    val intent = android.content.Intent(context, SelvardVpnService::class.java)
                    if (enabled) {
                        androidx.core.content.ContextCompat.startForegroundService(context, intent)
                        app.networkGuardianState.setRunning(true)
                    } else {
                        intent.action = SelvardVpnService.ACTION_STOP
                        context.startService(intent)
                        app.networkGuardianState.setRunning(false)
                    }
                },
            )
            Text(if (running) "PROTECTED (local filtering)" else "OFF — not filtering")
        }
    }
}
