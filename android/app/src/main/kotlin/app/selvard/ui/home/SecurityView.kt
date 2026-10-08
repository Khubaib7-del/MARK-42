package app.selvard.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.selvard.SelvardApplication
import app.selvard.link.LinkEventRecorder
import app.selvard.ui.design.SelvardIcons
import app.selvard.ui.glass.GlassHero
import app.selvard.ui.link.LinkCheckContent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** One scroll owner; posture belongs on Home, not inside the URL form. */
@Composable
fun SecurityView() {
    val app = LocalContext.current.applicationContext as SelvardApplication
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GlassHero(
            icon = SelvardIcons.Link,
            iconDescription = "Link check",
            title = "Check before you open",
            subtitle = "A second look at a suspicious URL. On this device.",
        )
        LinkCheckContent(initialUrl = null, guardian = app.linkGuardian, onAnalyzed = { url, verdict ->
            val event = LinkEventRecorder.toEvent(url, verdict, System.currentTimeMillis())
            scope.launch(Dispatchers.IO) {
                try {
                    app.eventBus.publish(event)
                    app.eventStore.append(event)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    // The visible verdict remains valid even if audit persistence is unavailable.
                }
            }
        })
        Text("You choose what to check. Selvard cannot intercept every link in other apps.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
