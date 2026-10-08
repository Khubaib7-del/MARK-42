package app.selvard.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import app.selvard.ui.design.SelvardIcons
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.selvard.network.SelvardVpnService
import app.selvard.ui.glass.GlassCard
import app.selvard.ui.glass.GlassHero
import app.selvard.ui.launch.BlueprintLaunchScreen
import app.selvard.ui.launch.systemMotionScale
import app.selvard.ui.theme.SelvardTheme

private const val STAGE_CROSSFADE_MS = 240
private const val ONBOARDING_PREFERENCES = "selvard_onboarding"
private const val ONBOARDING_COMPLETE = "disclosures_complete"
private const val INTRO_SEEN = "intro_seen"

private enum class AppStage { INTRO, ONBOARDING, HOME }

@Composable
fun SelvardApp() {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.applicationContext.getSharedPreferences(ONBOARDING_PREFERENCES, Context.MODE_PRIVATE)
    }
    var showBlueprintIntro by rememberSaveable {
        mutableStateOf(!preferences.getBoolean(INTRO_SEEN, preferences.getBoolean(ONBOARDING_COMPLETE, false)))
    }
    var onboardingDone by rememberSaveable {
        mutableStateOf(preferences.getBoolean(ONBOARDING_COMPLETE, false))
    }
    val transitionMs = if (systemMotionScale(context) == 0f) 0 else STAGE_CROSSFADE_MS
    val stageState = rememberSaveableStateHolder()

    val stage = when {
        showBlueprintIntro -> AppStage.INTRO
        onboardingDone -> AppStage.HOME
        else -> AppStage.ONBOARDING
    }
    SelvardTheme {
        Crossfade(targetState = stage, animationSpec = tween(transitionMs), label = "app_stage") { current ->
            stageState.SaveableStateProvider(current.name) {
                when (current) {
                    AppStage.INTRO -> BlueprintLaunchScreen(onAnimationComplete = {
                        preferences.edit().putBoolean(INTRO_SEEN, true).apply()
                        showBlueprintIntro = false
                        // Replay starts at frame zero; Home keeps saved navigation and scroll state.
                        stageState.removeState(AppStage.INTRO.name)
                    })
                    AppStage.HOME -> SelvardNav(onReplayIntro = { showBlueprintIntro = true })
                    AppStage.ONBOARDING -> Surface(modifier = Modifier.fillMaxSize()) {
                        OnboardingDisclosureJourney(onDone = {
                            // This flag records disclosure completion, never feature consent.
                            preferences.edit().putBoolean(ONBOARDING_COMPLETE, true).apply()
                            onboardingDone = true
                        })
                    }
                }
            }
        }
    }
}

/** Feature consent belongs in Network, not in first-run onboarding. */
@Composable
internal fun NetworkTabWrap() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        NetworkGuardianConsentView()
    }
}

/** Pager navigation: Back/Continue, with dismissal on the final page. */
@Composable
internal fun NavigationRow(
    page: Int,
    lastPage: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onDone: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (page > 0) {
            TextButton(onClick = onBack, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("Back") }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        Button(onClick = if (page < lastPage) onNext else onDone,
            modifier = Modifier.weight(2f).heightIn(min = 52.dp)) {
            Text(if (page < lastPage) "Continue" else "Enter Selvard")
        }
    }
}

@Composable
private fun NetworkRequestMessage(running: Boolean, message: String?) {
    if (!running || message?.startsWith("Stop requested") == true) {
        message?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

/**
 * Prominent disclosure + consent (Play VpnService policy; USER_FLOWS F3).
 * In-app, in the normal usage flow, separate from other disclosures, and
 * requiring affirmative action before the tunnel can be enabled.
 */
@Composable
internal fun NetworkGuardianConsentView() {
    val context = LocalContext.current
    val app = context.applicationContext as app.selvard.SelvardApplication
    val running by app.networkGuardianState.running.collectAsState()
    var consentPending by rememberSaveable { mutableStateOf(false) }
    var requestMessage by rememberSaveable { mutableStateOf<String?>(null) }

    fun startFilter() {
        requestMessage = try {
            androidx.core.content.ContextCompat.startForegroundService(
                context,
                Intent(context, SelvardVpnService::class.java),
            )
            "Start requested. Filtering is on only when the service reports it running."
        } catch (_: RuntimeException) {
            "Could not start filtering. Review Android VPN settings and try again."
        }
    }

    val vpnConsent = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        consentPending = false
        try {
            if (result.resultCode == Activity.RESULT_OK && VpnService.prepare(context) == null) {
                startFilter()
            } else {
                requestMessage = "VPN permission was not granted. Filtering remains off."
            }
        } catch (_: RuntimeException) {
            requestMessage = "Could not verify VPN permission. Try again from Network."
        }
    }
    LaunchedEffect(running) { requestMessage = null }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(bottom = 24.dp)) {
        GlassHero(
            icon = SelvardIcons.Wifi,
            iconDescription = "Network filter",
            title = "Network",
            subtitle = "A local filter for domain-name lookups (DNS) on this device.",
        )
        GlassCard(
            title = "What this does",
            icon = SelvardIcons.Wifi,
            iconDescription = "What this does",
        ) {
            Text(
                text = "Selvard uses Android's VPN permission to filter DNS lookups locally. " +
                    "Matches on the development sample list are refused; other lookups " +
                    "go unchanged to your configured resolver.\n\n" +
                    "Enabling this can replace another VPN. A persistent notification appears " +
                    "while filtering runs, and the background service may increase battery use.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        GlassCard(
            title = "Coverage limits",
        ) {
            Text(
                text = "Only DNS enters this tunnel. Other traffic bypasses Selvard; " +
                    "IPv6 and encrypted DNS may also bypass the filter. There is no remote " +
                    "VPN server or connection-content inspection. Block records stay on this device.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Development sample data: filtering currently uses a bundled sample " +
                    "list, not real threat intelligence.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        GlassCard(
            title = if (running) "ON — local DNS filtering" else "OFF — not filtering",
            icon = SelvardIcons.Wifi,
            iconDescription = "Filter state",
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Switch(
                    checked = running,
                    enabled = !consentPending,
                    onCheckedChange = { enabled ->
                        requestMessage = null
                        if (enabled) {
                            try {
                                val consentIntent = VpnService.prepare(context)
                                if (consentIntent == null) {
                                    startFilter()
                                } else {
                                    consentPending = true
                                    vpnConsent.launch(consentIntent)
                                }
                            } catch (_: RuntimeException) {
                                consentPending = false
                                requestMessage = "Could not request VPN permission. Try again from Network."
                            }
                        } else {
                            try {
                                context.startService(
                                    Intent(context, SelvardVpnService::class.java).apply {
                                        action = SelvardVpnService.ACTION_STOP
                                    },
                                )
                                requestMessage = "Stop requested. Waiting for the service to stop filtering."
                            } catch (_: RuntimeException) {
                                requestMessage = "Could not stop filtering. Review Android VPN settings."
                            }
                        }
                    },
                )
                Text(if (running) "ON — local DNS filtering" else "OFF — not filtering")
            }
            NetworkRequestMessage(running, requestMessage)
        }
    }
}
