package app.selvard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.selvard.core.domain.ux.AppSection
import app.selvard.ui.home.HomeView
import app.selvard.ui.home.SecurityView
import app.selvard.ui.settings.SettingsView
import app.selvard.ui.theme.SelvardTheme

/**
 * Phase 9 information architecture: bottom tabs over the seven sections in
 * [AppSection] order. Onboarding stays a pager in front; once dismissed the
 * tabs own the whole app — no nested pager. State survives rotation via
 * [rememberSaveable] on the route (never ordinals).
 */
@Composable
fun SelvardNav() {
    SelvardTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            var route by rememberSaveable { mutableStateOf(AppSection.HOME.route) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 24.dp),
            ) {
                FoundationBanner(modifier = Modifier.padding(top = 16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    SectionContent(route = route, onOpenSection = { route = it })
                }
                NavigationBar {
                    AppSection.entries.forEach { section ->
                        NavigationBarItem(
                            selected = route == section.route,
                            onClick = { route = section.route },
                            icon = { Text(section.title.take(1), style = MaterialTheme.typography.titleMedium) },
                            label = { Text(section.title, style = MaterialTheme.typography.labelMedium) },
                            modifier = Modifier.semantics {
                                contentDescription = section.contentDescription
                                role = Role.Tab
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionContent(route: String, onOpenSection: (String) -> Unit) {
    when (route) {
        AppSection.HOME.route -> HomeView(onOpenSection = onOpenSection)
        AppSection.SECURITY.route -> SecurityView()
        AppSection.NETWORK.route -> NetworkTabWrap()
        AppSection.APPS.route -> AppGuardianView()
        AppSection.IDENTITY.route -> IdentityExposureView()
        AppSection.TIMELINE.route -> IncidentTimelineView()
        AppSection.SETTINGS.route -> SettingsView()
        else -> HomeView(onOpenSection = onOpenSection)
    }
}
