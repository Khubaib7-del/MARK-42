package app.selvard.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.selvard.ui.design.SelvardIcons
import app.selvard.ui.design.SelvardSurface
import app.selvard.ui.launch.systemMotionScale

/** Disclosures are separate from feature consent. No engine starts during this journey. */
@Composable
internal fun OnboardingDisclosureJourney(onDone: () -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    val duration = if (systemMotionScale(LocalContext.current) == 0f) 0 else 240
    val colors = MaterialTheme.colorScheme
    BackHandler(enabled = step > 0) { step-- }
    Column(Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 24.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("SELVARD", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text("${step + 1} / 3", style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(3) { index ->
                Canvas(Modifier.weight(1f).height(3.dp)) {
                    drawRoundRect(if (index <= step) colors.primary else colors.outlineVariant,
                        cornerRadius = CornerRadius(3.dp.toPx()))
                }
            }
        }
        AnimatedContent(targetState = step, modifier = Modifier.weight(1f), transitionSpec = {
            val direction = if (targetState > initialState) 1 else -1
            (fadeIn(tween(duration)) + slideInHorizontally(tween(duration)) { it / 8 * direction })
                .togetherWith(fadeOut(tween(duration / 2)) +
                    slideOutHorizontally(tween(duration)) { -it / 8 * direction })
        }, label = "onboarding_step") { page ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)) {
                OnboardingIllustration(page)
                Text(when (page) {
                    0 -> "Understand your security."
                    1 -> "Privacy is the foundation."
                    else -> "Choose your coverage."
                }, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.semantics { heading() })
                Text(when (page) {
                    0 -> "Check a link, review apps, and follow the evidence in one calm workspace."
                    1 -> "Security checks should help you understand your device without collecting your private life."
                    else -> "Start with a link check. Enable other tools when you understand their scope."
                }, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
                DisclosureDetails(page)
                Spacer(Modifier.height(4.dp))
            }
        }
        Text("You decide what to enable. Getting started grants no permissions.",
            style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))
        NavigationRow(step, 2, { if (step > 0) step-- }, { if (step < 2) step++ }, onDone)
    }
}

@Composable
private fun DisclosureDetails(page: Int) {
    when (page) {
        0 -> {
            DisclosureSection("Evidence over scores",
                "Every assessment has a scope. No result guarantees that your device is safe.")
            DisclosureSection("Check before opening",
                "Share or paste a link. Selvard cannot intercept links you tap inside other apps.")
        }
        1 -> {
            DisclosureSection("Local by default",
                "Recorded events and declared identities are encrypted on this device. No onboarding data is sent.")
            DisclosureSection("External checks need your choice",
                "Identity exposure uses an external provider only with separate consent. " +
                    "Review what leaves your device before a lookup.")
            DisclosureSection("Private content stays private",
                "Selvard cannot read your messages or email, or see inside encrypted connections.")
        }
        else -> {
            DisclosureSection("Apps",
                "Review visible packages and declared permissions. Capabilities are risk signals, " +
                    "not proof of malicious behavior.")
            DisclosureSection("Network",
                "Optional local DNS filtering needs Android VPN approval and can replace another VPN. " +
                    "Other traffic bypasses Selvard.")
            DisclosureSection("Preview coverage",
                "Links and DNS use development sample lists, not live intelligence. " +
                    "Off or unavailable tools provide no coverage.")
        }
    }
}

/** Static boundary motif represents the philosophy, never scanning activity. */
@Composable
private fun OnboardingIllustration(page: Int) {
    val colors = MaterialTheme.colorScheme
    val icon = when (page) { 0 -> SelvardIcons.Link; 1 -> SelvardIcons.Lock; else -> SelvardIcons.Apps }
    SelvardSurface(Modifier.fillMaxWidth().height(144.dp)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val radius = size.height * 0.36f
                drawCircle(colors.secondary.copy(alpha = 0.10f), radius * 1.65f, style = Stroke(1.dp.toPx()))
                drawCircle(colors.secondary.copy(alpha = 0.22f), radius * 1.25f, style = Stroke(1.dp.toPx()))
                drawCircle(colors.primary.copy(alpha = 0.08f), radius)
            }
            Icon(icon, null, tint = colors.primary, modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun DisclosureSection(title: String, body: String) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.semantics { heading() })
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
