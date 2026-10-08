package app.selvard.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import app.selvard.core.domain.ux.AppSection
import app.selvard.core.domain.ux.ShieldPane
import app.selvard.ui.glass.SectionIcons
import app.selvard.ui.home.HomeView
import app.selvard.ui.home.SecurityView
import app.selvard.ui.launch.systemMotionScale
import app.selvard.ui.settings.SettingsView
import app.selvard.ui.theme.SelvardTheme

/** Five destinations, with the four guardian tools grouped under Shield. */
@Composable
fun SelvardNav(onReplayIntro: (() -> Unit)? = null) {
    SelvardTheme {
        var route by rememberSaveable { mutableStateOf(AppSection.HOME.route) }
        var pane by rememberSaveable { mutableStateOf(ShieldPane.LINKS.route) }
        val stateHolder = rememberSaveableStateHolder()
        val transitionMs = if (systemMotionScale(LocalContext.current) == 0f) 0 else 150
        BackHandler(enabled = route != AppSection.HOME.route) { route = AppSection.HOME.route }
        val open: (String) -> Unit = { target ->
            val guardian = ShieldPane.entries.firstOrNull { it.route == target }
            if (guardian != null || target == "security") {
                pane = guardian?.route ?: ShieldPane.LINKS.route
                route = AppSection.SHIELD.route
            } else {
                route = AppSection.entries.firstOrNull { it.route == target }?.route ?: AppSection.HOME.route
            }
        }
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().systemBarsPadding()) {
                Crossfade(
                    targetState = route,
                    animationSpec = tween(transitionMs),
                    modifier = Modifier.weight(1f),
                    label = "destination",
                ) { destination ->
                    stateHolder.SaveableStateProvider(destination) {
                        when (destination) {
                            AppSection.SHIELD.route -> ShieldContent(pane, { pane = it })
                            AppSection.TIMELINE.route -> IncidentTimelineView()
                            AppSection.IDENTITY.route -> ScreenInset { IdentityExposureView() }
                            AppSection.SETTINGS.route -> ScreenInset { SettingsView() }
                            else -> HomeView(onOpenSection = open, onReplayIntro = onReplayIntro)
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets(0, 0, 0, 0),
                ) {
                    AppSection.entries.forEach { section ->
                        NavigationBarItem(
                            selected = route == section.route,
                            onClick = { route = section.route },
                            icon = { Icon(SectionIcons.forKey(section.iconKey), contentDescription = null) },
                            label = {
                                Text(section.title, style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            modifier = Modifier.semantics { contentDescription = section.contentDescription },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShieldContent(pane: String, onPane: (String) -> Unit) {
    val paneState = rememberSaveableStateHolder()
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(20.dp, 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ShieldPane.entries.forEach { section ->
                val active = pane == section.route
                Surface(
                    onClick = { onPane(section.route) },
                    shape = RoundedCornerShape(24.dp),
                    color = if (active) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.background,
                    contentColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.semantics {
                        contentDescription = section.contentDescription
                        role = Role.Tab
                        selected = active
                    },
                ) {
                    Text(section.title, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(18.dp, 16.dp))
                }
            }
        }
        Column(Modifier.weight(1f)) {
            paneState.SaveableStateProvider(pane) {
            when (pane) {
                ShieldPane.APPS.route -> AppGuardianView()
                ShieldPane.NETWORK.route -> ScreenInset { NetworkTabWrap() }
                ShieldPane.PRIVACY.route -> ScreenInset { PrivacyMonitorView() }
                else -> ScreenInset { SecurityView() }
            }
            }
        }
    }
}

@Composable
private fun ScreenInset(content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) { content() }
}
